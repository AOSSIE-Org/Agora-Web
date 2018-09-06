package controllers

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.ExtractableRequest
import com.mohiva.play.silhouette.impl.exceptions.OAuth2StateException
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, OAuth2Info, SocialProvider, SocialProviderRegistry}
import formatters.json.Token
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import javax.inject.Inject
import models.security.User
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import service.UserService
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Authentication")
class SocialAuthController @Inject()(components: ControllerComponents,
                               silhouette: Silhouette[DefaultEnv],
                               userService: UserService,
                               authInfoRepository: AuthInfoRepository,
                               socialProviderRegistry: SocialProviderRegistry,
                               messagesApi: MessagesApi)
                              (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {
  /**
  * OAuthInfoFromToken extracts the token from the request
  * It then returns optionally an OAuth2Info with the info
  */
  @ApiOperation(value = "Login with social provider and get token", response = classOf[Token])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "Access-Token",
        value = "User's social provider access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def authenticate(provider: String) = Action.async { implicit request =>
    ( (socialProviderRegistry.get[SocialProvider](provider), OAuthInfoFromToken()) match {
      case ( Some(p: SocialProvider with CommonSocialProfileBuilder), Some(authInfo) ) =>
        p.retrieveProfile(authInfo.asInstanceOf[p.A]).flatMap{
          profile => userService.retrieve(profile.loginInfo).flatMap {
            case Some(user) =>
              //just return token
              for {
                authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
                token <- silhouette.env.authenticatorService.init(authenticator)
                result <- silhouette.env.authenticatorService.embed(token,
                  Ok(Json.toJson(Token(token = token, expiresOn = authenticator.expirationDateTime))))
              } yield {
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                result
              }
            case None =>
              for {
                _ <- userService.save(User(None, profile.loginInfo, s"${profile.firstName.get}.${profile.lastName.get}",
                  profile.email.get, profile.firstName.get, profile.lastName.get, profile.avatarURL, true))
                authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
                authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
                token <- silhouette.env.authenticatorService.init(authenticator)
                result <- silhouette.env.authenticatorService.embed(token,
                  Ok(Json.toJson(Token(token = token, expiresOn = authenticator.expirationDateTime))))
              } yield {
                val savedUser = User(None, profile.loginInfo, s"${profile.firstName.get}.${profile.lastName.get}",
                  profile.email.get, profile.firstName.get, profile.lastName.get, profile.avatarURL, true)
                silhouette.env.eventBus.publish(LoginEvent(savedUser, request))
                silhouette.env.eventBus.publish(SignUpEvent(savedUser, request))
                result
              }
          }
        }
      case (_, None) =>
        Future.failed(
          new OAuth2StateException(s"No token found in the request while authenticating with $provider")
        )
      case _ =>
        Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: OAuth2StateException =>
        NotFound(Json.obj("message" -> "Could not authenticate"))
      case e: ProviderException =>
        NotFound(Json.obj("message" -> "Could not authenticate"))
    }
  }

  def OAuthInfoFromToken[B]()(implicit request: ExtractableRequest[B]): Option[OAuth2Info] = {
    request.extractString("Access-Token") match {
      case Some(token) =>
        Some(
          OAuth2Info(
            accessToken = token,
            tokenType = request.extractString(OAuth2Info.TokenType),
            expiresIn = request.extractString(OAuth2Info.ExpiresIn).map(_.toInt),
            refreshToken = request.extractString(OAuth2Info.RefreshToken)
          )
        )
      case _ => None
    }
  }
}