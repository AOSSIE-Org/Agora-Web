package controllers

import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.json.PasswordData
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.swagger.ResponseMessage
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.{JsError, Json}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import service.{AuthTokenService, UserService}
import utils.auth.{CustomSilhouette, DefaultEnv}
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Authentication")
class ForgotPasswordController @Inject() (
  components: ControllerComponents,
  silhouette: CustomSilhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry,
  authTokenService: AuthTokenService,
  mailerClient: MailerClient
)(
  implicit
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {


  /**
   * Sends an email with password reset instructions.
   *
   * It sends an email to the given address if it exists in the database. Otherwise we do not show the user
   * a notice for not existing email addresses to prevent the leak of existing email addresses.
   */
  @ApiOperation(value = "Send reset password token to user email", response = classOf[ResponseMessage])
  def send(userName: String) = Action.async { implicit request =>
        val loginInfo = LoginInfo(CredentialsProvider.ID, userName)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            authTokenService.create(user.loginInfo).map { authToken =>
              //This should be a dynamic link that contains the reset token and can be understood by the frontend
              //The frontend should be able to extract the token from the request and require the user to enter a new password
              //This token and new password can then be sent to the backend inorder to change the password
              val url = routes.ForgotPasswordController.reset(authToken.tokenId).absoluteURL()

              mailerClient.send(Email(
                subject = Messages("email.reset.password.subject"),
                from = Messages("email.from"),
                to = Seq(user.email),
                bodyText = Some(views.txt.emails.resetPassword(user, url).body),
                bodyHtml = Some(views.html.emails.resetPassword(user, url).body)
              ))
              Ok(Json.toJson("message" -> Messages("reset.email.sent")))
            }
          case None =>
            Future.successful(PreconditionFailed(Json.toJson("message" -> Messages("reset.email.no.user"))))
        }
      }


  /**
    * Resets the password.
    *
    */
  @ApiOperation(value = "Reset password using token sent to email", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Password",
        required = true,
        dataType = "formatters.json.PasswordData",
        paramType = "body"
      )
    )
  )
  def reset(token: String) = Action.async(parse.json) { implicit request =>
    request.body.validate[PasswordData].map { passwordData =>
      authTokenService.validate(token).flatMap {
        case Some(authToken) =>
          userService.retrieve(authToken.userLoginInfo).flatMap {
            case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
              val authInfo = passwordHasherRegistry.current.hash(passwordData.password)
              authInfoRepository.update[PasswordInfo](user.loginInfo, authInfo).flatMap { _ =>
                Future.successful(Ok(Messages("password.reset")))
              }
            case _ => Future.successful(BadRequest(Json.toJson("message" -> Messages("invalid.reset.link"))))
          }
        case None => Future.successful(BadRequest(Json.toJson("message" -> Messages("invalid.reset.link"))))
      }
    }.recoverTotal{
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(message = JsError.toJson(error)))))
    }
  }
}
