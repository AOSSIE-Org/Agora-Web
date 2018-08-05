package controllers

import java.net.URLDecoder
import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.json.{Token, UserData}
import io.swagger.annotations.{Api, ApiOperation}
import models.swagger.ResponseMessage
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Json
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import service.{AuthTokenService, UserService}
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Verification")
class ActivateAccountController @Inject()(
                                           components: ControllerComponents,
                                           silhouette: Silhouette[DefaultEnv],
                                           userService: UserService,
                                           authTokenService: AuthTokenService,
                                           mailerClient: MailerClient
                                         )(
                                           implicit
                                           ex: ExecutionContext
                                         ) extends AbstractController(components) with I18nSupport {

  @ApiOperation(value = "Resend activation link to user email", response = classOf[ResponseMessage])
  def send(userName: String) = Action.async { implicit request: Request[AnyContent] =>
    val loginInfo = LoginInfo(CredentialsProvider.ID, userName)
    userService.retrieve(loginInfo).flatMap {
      case Some(user) if !user.activated && user.loginInfo.providerID == CredentialsProvider.ID =>
        authTokenService.create(user.loginInfo).flatMap { authToken =>
          //This should be a dynamic link that contains the account activation token and can be understood by the frontend
          //The frontend should be able to extract the token from the request
          //This token can then be sent to the backend inorder to activate the account
          val url = routes.ActivateAccountController.activate(authToken.tokenId).absoluteURL()

          mailerClient.send(Email(
            subject = Messages("email.activate.account.subject"),
            from = Messages("email.from"),
            to = Seq(URLDecoder.decode(user.email, "UTF-8")),
            bodyText = Some(views.txt.emails.activateAccount(user, url).body),
            bodyHtml = Some(views.html.emails.activateAccount(user, url).body)
          ))
          Future.successful(Ok(Json.toJson("message" -> Messages("activation.email.sent", URLDecoder.decode(user.email, "UTF-8")))))
        }
      case None => Future.successful(BadRequest(Json.toJson("message" -> Messages("activation.email.not.sent"))))
    }
  }


  @ApiOperation(value = "Activate account and get authentication token", response = classOf[UserData])
  def activate(token: String) = Action.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(token).flatMap {
      case Some(authToken) => userService.retrieve(authToken.userLoginInfo).flatMap {
        case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
          for {
            isActivated <- userService.update(user.copy(activated = true), authToken.userLoginInfo)
            authenticator <- silhouette.env.authenticatorService.create(authToken.userLoginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(token,
              Ok(
                Json.toJson(
                  user.extractUserData.copy(token = Some(Token(
                    token,
                    expiresOn = authenticator.expirationDateTime))
                  )
                )
              )
            )
          } yield {
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            result
          }
        case _ => Future.successful(BadRequest(Json.toJson("message" -> Messages("invalid.activation.link"))))
      }
      case None => Future.successful(BadRequest(Json.toJson("message" -> Messages("invalid.activation.link"))))
    }
  }
}
