package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers._
import play.api.i18n.{ I18nSupport, MessagesApi, Messages }
import play.api.mvc.Controller
import utils.auth.DefaultEnv
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.providers._
import models.services.UserService
import scala.concurrent.Future
import scala.language.postfixOps
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.api.exceptions.ProviderException

/**
 * The `Sign In` controller.
 *
 * @param messagesApi            The Play messages API.
 * @param silhouette             The Silhouette stack.
 * @param socialProviderRegistry The social provider registry.
 */
class SignInController @Inject()(
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  socialProviderRegistry: SocialProviderRegistry,
  userService: UserService,
  credentialsProvider: CredentialsProvider
) extends Controller with I18nSupport {

  /**
   * Views the `Sign In` page.
   *
   * @return The result to display.
   */
  def view = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.signIn(socialProviderRegistry)))
  }

  def submit(email: String , id: String) = silhouette.UnsecuredAction.async { implicit request =>
    val password = email
    val credentials = Credentials(email, password)
    credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
      val result = Redirect(routes.ElectionController.viewElectionSecured(id))
      userService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          silhouette.env.authenticatorService.create(loginInfo).map {
            case authenticator => authenticator
          }.flatMap { authenticator =>
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
              silhouette.env.authenticatorService.embed(v, result)
            }
          }
        case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
      }
    }.recover {
      case e: ProviderException =>
        Redirect(routes.SignInController.view()).flashing("error" -> Messages("invalid.credentials"))
    }
  }
}
