package controllers

import java.net.URLDecoder

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import formatters.json.{CredentialFormat, Crypto, QuestionData, Token, UserData}
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.TotpToken
import models.swagger.ResponseMessage
import models.security.SignUp
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.mailer.{Email, MailerClient}
import service.{AuthTokenService, TwoFactorAuthService, UserService}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import org.joda.time.DateTimeZone
import org.joda.time.DateTime
import utils.responses.rest.Bad
import utils.auth.{CustomSilhouette, DefaultEnv}
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Two Factor Authentication")
class TwoFactorAuthController @Inject()(components: ControllerComponents,
                                          userService: UserService,
                                          configuration: Configuration,
                                          silhouette: CustomSilhouette[DefaultEnv],
                                          clock: Clock,
                                          mailerClient: MailerClient,
                                          authTokenService: AuthTokenService,
                                          twoFactorAuthService: TwoFactorAuthService,
                                          credentialsProvider: CredentialsProvider,
                                          authInfoRepository: AuthInfoRepository,
                                          passwordHasherRegistry: PasswordHasherRegistry,
                                          messagesApi: MessagesApi)
                                         (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  implicit val credentialFormat = CredentialFormat.restFormat

  implicit val signUpFormat = Json.format[SignUp]

  @ApiOperation(value = "Verify Otp", response = classOf[UserData])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Crypto",
        required = true,
        dataType = "formatters.json.Crypto",
        paramType = "body"
      )
    )
  )
  def verifyOtp = Action.async(parse.json[Crypto]) { implicit request =>
     twoFactorAuthService.find(request.body.crypto).flatMap {
       case Some(totpToken) =>
        twoFactorAuthService.validate(request.body.otp, request.body.crypto).flatMap{ result =>
          if(result) {
            userService.retrieve(totpToken.userLoginInfo).flatMap {
              case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
                if(request.body.trustedDevice){
                  twoFactorAuthService.addTrustedDevice(user.loginInfo).flatMap { trustedDevice =>
                    for {
                      authenticator <- silhouette.env.authenticatorService.create(totpToken.userLoginInfo)
                      token <- silhouette.env.authenticatorService.init(authenticator)
                      result <- silhouette.env.authenticatorService.embed(token,
                        Ok(
                          Json.toJson(
                            user.extractUserData.copy(authToken = Some(Token(
                              token,
                              expiresOn = authenticator.expirationDateTime)),
                              trustedDevice = Some(trustedDevice.trustedDevice)
                            )
                          )
                        )
                      )
                    } yield {
                      silhouette.env.eventBus.publish(LoginEvent(user, request))
                      result
                    }
                  }
                }
                else {
                  for {
                    authenticator <- silhouette.env.authenticatorService.create(totpToken.userLoginInfo)
                    token <- silhouette.env.authenticatorService.init(authenticator)
                    result <- silhouette.env.authenticatorService.embed(token,
                      Ok(
                        Json.toJson(
                          user.extractUserData.copy(authToken = Some(Token(
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
                }
              case _ => Future.successful(BadRequest(Json.toJson("message" -> Messages("invalid.crypto"))))
            }
          }
          else Future.successful(BadRequest(Json.toJson("message" -> Messages("invalid.otp"))))
        }  
        case _=>  Future.successful(BadRequest(Json.toJson("message" -> Messages("invalid.crypto"))))
     }
     
  }

  @ApiOperation(value = "Resend Otp to user email", response = classOf[ResponseMessage])
  def resendOtp(userName: String) = Action.async {
    implicit request: Request[AnyContent] =>
    val loginInfo = LoginInfo(CredentialsProvider.ID, userName)
    twoFactorAuthService.removeExpired.flatMap { deleted =>
      userService.retrieve(loginInfo).flatMap {
      case Some(user) if user.activated && user.twoFactorAuthentication && user.loginInfo.providerID == CredentialsProvider.ID =>
          twoFactorAuthService.createTotpToken(loginInfo).flatMap { totpToken =>
            twoFactorAuthService.totp( System.currentTimeMillis / 300000, 6, totpToken.crypto).flatMap { totp =>
            mailerClient.send(Email(
              subject = Messages("email.two.factor.authentication.subject"),
              from = Messages("email.from"),
              to = Seq(URLDecoder.decode(user.email, "UTF-8")),
              bodyText = Some(views.txt.emails.totp(user.username, totp, 300).body),
              bodyHtml = Some(views.html.emails.totp(user.username, totp, 300).body)
            ))
            Future.successful(Ok(Json.obj(
              "username" -> userName,
              "crypto" -> totpToken.crypto,
              "twoFactorAuthentication" -> true )))
            } 
          }
      case None => Future.successful(BadRequest(Json.toJson("message" -> Messages("two.factor.authentication.email.not.sent"))))
      }
    }

  }

  @ApiOperation(value = "Toggle Two Factor Authentication", response = classOf[ResponseMessage])
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
  def toggleTwoFactorAuth = silhouette.SecuredAction.async { implicit request =>
      userService.retrieve(request.authenticator.loginInfo).flatMap {
        case Some(user) =>
          if(request.authenticator.loginInfo.providerID == CredentialsProvider.ID)
            userService.update(user.copy(twoFactorAuthentication = !user.twoFactorAuthentication),
              request.authenticator.loginInfo).flatMap(_ => Future.successful(Ok(Json.toJson("message" ->"User updated"))))
          else
            Future.successful(BadRequest(Json.toJson(Bad(code= Some(401), message= "Unauthorized. Change your personal information with your social provider"))))
        case None => Future.successful(BadRequest(Json.toJson("message" ->"User not updated")))
    }
  }

  @ApiOperation(value = "Get security question", response = classOf[ResponseMessage])
  def securityQuestion(crypto: String) = Action.async {
    implicit request: Request[AnyContent] =>
    twoFactorAuthService.find(crypto).flatMap {
      case Some(totpToken) =>
        userService.retrieve(totpToken.userLoginInfo).flatMap {
          case Some(user) if user.activated && user.twoFactorAuthentication && user.loginInfo.providerID == CredentialsProvider.ID =>
            Future.successful(Ok(Json.obj(
              "question" -> user.securityQuestion.question
            )))
          case None => Future.successful(BadRequest(Json.toJson(Bad(message = "Bad request"))))
        }
      case _=>  Future.successful(BadRequest(Json.toJson("message" -> Messages("invalid.crypto"))))
    }
    
  }

  @ApiOperation(value = "Verify Security Question", response = classOf[UserData])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Security Question",
        required = true,
        dataType = "formatters.json.QuestionData",
        paramType = "body"
      )
    )
  )
  def verifySecurityQuestion = Action.async(parse.json[QuestionData]) { implicit request =>
     twoFactorAuthService.find(request.body.crypto).flatMap {
       case Some(totpToken) =>
        userService.retrieve(totpToken.userLoginInfo).flatMap {
          case Some(user) =>
            twoFactorAuthService.validateSecurityQuestion(user, request.body.question, request.body.answer).flatMap { result =>
              if(result) {
                if(user.loginInfo.providerID == CredentialsProvider.ID) {
                  for {
                    jwtAuthenticator <- silhouette.env.authenticatorService.create(user.loginInfo)
                    refreshTokenAuthenticator <- silhouette.env.authenticatorService.createRefreshToken(user.loginInfo)
                    authToken <- silhouette.env.authenticatorService.init(jwtAuthenticator)
                    refreshToken <- silhouette.env.authenticatorService.initRefreshToken(refreshTokenAuthenticator)
                  } yield {
                    Ok(
                      Json.toJson(
                        user.extractUserData.copy(
                          authToken = Some(Token(authToken, expiresOn = jwtAuthenticator.expirationDateTime)),
                          refreshToken = Some(Token(refreshToken, expiresOn = refreshTokenAuthenticator.expirationDateTime))
                        )
                      )
                    )
                  }
                }
                else Future.successful(BadRequest(Json.toJson("message" -> Messages("wrong answer"))))
              }
              else Future.successful(BadRequest(Json.toJson("message" -> Messages("wrong answer"))))
            }
          case _=>  Future.successful(BadRequest(Json.toJson("message" -> Messages("invalid.crypto"))))
        }
      case _=>  Future.successful(BadRequest(Json.toJson("message" -> Messages("invalid.crypto"))))
    }
  }

}