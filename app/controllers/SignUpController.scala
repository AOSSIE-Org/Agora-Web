package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.json.{CredentialFormat, Token}
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.security.{SignUp, User}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.{AuthTokenService, UserService}
import utils.auth.{CustomSilhouette, DefaultEnv}
import utils.responses.rest.Bad

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Api(value = "Authentication")
class SignUpController @Inject()(components: ControllerComponents,
                                 userService: UserService,
                                 configuration: Configuration,
                                 silhouette: CustomSilhouette[DefaultEnv],
                                 clock: Clock,
                                 credentialsProvider: CredentialsProvider,
                                 authInfoRepository: AuthInfoRepository,
                                 authTokenService: AuthTokenService,
                                 passwordHasherRegistry: PasswordHasherRegistry,
                                 avatarService: AvatarService,
                                 mailerClient: MailerClient,
                                 messagesApi: MessagesApi)
                                (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  implicit val credentialFormat = CredentialFormat.restFormat

  implicit val signUpFormat = Json.format[SignUp]

  @ApiOperation(value = "Register and get authentication token", response = classOf[Token])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "SignUp",
        required = true,
        dataType = "models.security.SignUp",
        paramType = "body"
      )
    )
  )
  def signUp = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUp].map { signUp =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, signUp.identifier)
      userService.checkEmail(signUp.email).flatMap {
        case None => /* User not already exists */
          userService.retrieve(loginInfo).flatMap {
            case None => /* UserName not already exists */ 
            val user = User(None, loginInfo, loginInfo.providerKey, signUp.email, signUp.firstName, signUp.lastName, None, false, signUp.securityQuestion, false)
            val authInfo = passwordHasherRegistry.current.hash(signUp.password)
            for {
              avatar <- avatarService.retrieveURL(signUp.email)
              userToSave <- userService.save(user.copy(avatarURL = avatar))
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              result <- Future.successful(Ok(Json.toJson("A message has been sent to your email. Please follow the link provided in the email to activate your account")))
              authToken <- authTokenService.create(loginInfo, 10 minutes)

            } yield {

              //This should be a dynamic link that contains the account activation token and can be understood by the frontend
              //The frontend should be able to extract the token from the request
              //This token can then be sent to the backend inorder to activate the account
              val url = routes.ActivateAccountController.activate(authToken.tokenId).absoluteURL()
              mailerClient.send(Email(
                subject = Messages("email.sign.up.subject"),
                from = Messages("email.from"),
                to = Seq(user.email),
                bodyText = Some(views.txt.emails.signUp(user, url).body),
                bodyHtml = Some(views.html.emails.signUp(user, url).body)
              ))
              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              result
            }
            case Some(_) => /* username already exists! */
            Future(Conflict(Json.toJson(Bad(message = "username already exists"))))
          }
          case Some(_) => /* user already exists! */
          Future(Conflict(Json.toJson(Bad(message = "user already exists"))))
        }
 
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(message = JsError.toJson(error)))))
    }
  }
}
