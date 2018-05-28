package controllers

import java.lang.annotation.Annotation

import akka.http.scaladsl.model.headers.Authorization
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.api.repositories.{AuthInfoRepository, AuthenticatorRepository}
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.json.{PasswordData, ResponseMessage, UserData}
import io.swagger.annotations._
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.UserService
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "User")
class UserController @Inject()(components: ControllerComponents,
                               userService: UserService,
                               configuration: Configuration,
                               silhouette: Silhouette[DefaultEnv],
                               clock: Clock,
                               credentialsProvider: CredentialsProvider,
                               authInfoRepository: AuthInfoRepository,
                               passwordHasherRegistry: PasswordHasherRegistry,
                               messagesApi: MessagesApi)
                              (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {


  @ApiOperation(value = "Logout user from the system", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def logout = silhouette.SecuredAction.async { implicit request =>
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, Ok("User logged out"))
  }

  @ApiOperation(value = "Updates user information", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "UserData",
        required = true,
        dataType = "formatters.json.UserData",
        paramType = "body"
      ),
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def update = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[UserData].map { data =>
      if(request.authenticator.loginInfo.providerID != CredentialsProvider.ID)
        userService.update(data, request.authenticator.loginInfo).flatMap(_ => Future.successful(Ok("User updated")))
      else
        Future.successful(BadRequest(Json.toJson(Bad(code= Some(401), message= "Unauthorized. Change your personal information with your social provider"))))
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code= Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Change user password", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Password",
        required = true,
        dataType = "formatters.json.PasswordData",
        paramType = "body"
      ),
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def resetPassword = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[PasswordData].map { data =>
      val authInfo = passwordHasherRegistry.current.hash(data.password)
      authInfoRepository.update(request.authenticator.loginInfo, authInfo).flatMap(_ => Future.successful(Ok("Password changed")))
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Get user information", response = classOf[UserData])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def user = silhouette.SecuredAction.async { implicit request =>
    userService.retrieve(request.authenticator.loginInfo).flatMap{
      case Some(user) => Future.successful(Ok(Json.toJson(user.extractUserData)))
      case None => Future.successful(BadRequest(Json.toJson(Bad(message = "Bad request"))))
    }
  }
}
