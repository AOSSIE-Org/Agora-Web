package controllers

import java.net.URLDecoder

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import formatters.json.{CredentialFormat, Token, UserData, Crypto}
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.TotpToken
import models.security.SignUp
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.{AuthTokenService, UserService, TwoFactorAuthService}
import org.joda.time.DateTimeZone
import utils.auth.DefaultEnv

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Authentication")
class CredentialsAuthController @Inject()(components: ControllerComponents,
                                          userService: UserService,
                                          configuration: Configuration,
                                          silhouette: Silhouette[DefaultEnv],
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

  @ApiOperation(value = "Login and get authentication token", response = classOf[UserData])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Credentials",
        required = true,
        dataType = "formatters.json.CredentialFormat",
        paramType = "body"
      )
    )
  )
  def authenticate = Action.async(parse.json[CredentialFormat]) { implicit request =>
    val credentials =
      Credentials(request.body.identifier, request.body.password)
    credentialsProvider
      .authenticate(credentials)
      .flatMap { loginInfo =>
        twoFactorAuthService.removeExpired.flatMap { deleted =>
          userService.retrieve(loginInfo).flatMap {
              case Some(user) if !user.activated =>
                authTokenService.create(user.loginInfo).flatMap { authToken =>
                  val url = routes.ActivateAccountController.activate(authToken.tokenId).absoluteURL()

                  mailerClient.send(Email(
                    subject = Messages("email.activate.account.subject"),
                    from = Messages("email.from"),
                    to = Seq(URLDecoder.decode(user.email, "UTF-8")),
                    bodyText = Some(views.txt.emails.activateAccount(user, url).body),
                    bodyHtml = Some(views.html.emails.activateAccount(user, url).body)
                  ))
                  Future.successful(BadRequest(Json.toJson("message" -> "Account not activated. A message has been sent to your email, follow the link to activate your account and try again")))
                }
              case Some(user) if user.twoFactorAuthentication =>
                  twoFactorAuthService.validateTrustedDevice(user.loginInfo, request.body.trustedDevice).flatMap { result =>
                    if(result) {
                      val config = configuration.underlying
                      silhouette.env.authenticatorService
                        .create(loginInfo)
                        .map {
                          case authenticator => authenticator
                        }
                        .flatMap { authenticator =>
                          silhouette.env.eventBus.publish(LoginEvent(user, request))
                          silhouette.env.authenticatorService
                            .init(authenticator)
                            .flatMap { token =>
                              silhouette.env.authenticatorService
                                .embed(
                                  token,
                                  Ok(
                                    Json.toJson(
                                      user.extractUserData.copy(token = Some(Token(
                                        token,
                                        expiresOn = authenticator.expirationDateTime))
                                      )
                                    )
                                  )
                                )
                            }
                        }
                      }
                      else {
                        val crypto = UUID.randomUUID().toString
                        val expiry = 300
                        var expiresOn = clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry)
                        val totpToken = TotpToken(crypto, loginInfo, expiresOn, 0)
                        twoFactorAuthService.save(totpToken).flatMap { token => 
                          twoFactorAuthService.totp( System.currentTimeMillis / 300000, 6, crypto).flatMap { totp =>
                          mailerClient.send(Email(
                            subject = Messages("email.two.factor.authentication.subject"),
                            from = Messages("email.from"),
                            to = Seq(URLDecoder.decode(user.email, "UTF-8")),
                            bodyText = Some(views.txt.emails.totp(user.username, totp, expiry).body),
                            bodyHtml = Some(views.html.emails.totp(user.username, totp, expiry).body)
                          ))
                          Future.successful(Ok(Json.obj(
                            "username" -> request.body.identifier,
                            "crypto" -> crypto,
                            "twoFactorAuthentication" -> true )))
                          } 
                        }
                      }
                  }
              case Some(user) =>
                val config = configuration.underlying
                silhouette.env.authenticatorService
                  .create(loginInfo)
                  .map {
                    case authenticator => authenticator
                  }
                  .flatMap { authenticator =>
                    silhouette.env.eventBus.publish(LoginEvent(user, request))
                    silhouette.env.authenticatorService
                      .init(authenticator)
                      .flatMap { token =>
                        silhouette.env.authenticatorService
                          .embed(
                            token,
                            Ok(
                              Json.toJson(
                                user.extractUserData.copy(token = Some(Token(
                                  token,
                                  expiresOn = authenticator.expirationDateTime))
                                )
                              )
                            )
                          )
                      }
                  }
              
              case None =>
                Future.successful(NotFound(Json.toJson("message" -> s"""${new IdentityNotFoundException("Couldn't find user")}""")))
            }
        }
      }
      .recover {
        case _: ProviderException =>
          Forbidden
      }
  }
}
