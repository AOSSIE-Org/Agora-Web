package controllers

import javax.inject._

import play.api.mvc._
import scheduler.Scheduler
import scheduler.Scheduler.scheduler
import scala.concurrent.Future
import utils.auth.DefaultEnv
import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import play.api.i18n.{ I18nSupport, MessagesApi }
import models.daos.ElectionDAOImpl
import models.daos.ResultDAOImpl
import models.services.Countvotes

import play.api.libs.mailer.{ Email, MailerClient }
import models.services.Countvotes
import org.bson.types.ObjectId


import countvotes.parsers._
import countvotes.structures._
import countvotes.algorithms._
import countvotes.methods._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (
                                 val messagesApi: MessagesApi,
                                 silhouette: Silhouette[DefaultEnv],
                                 mailerClient: MailerClient
                               ) extends Controller with I18nSupport  {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
     val electionDAOImpl = new ElectionDAOImpl();
     scheduler.start()
     Scheduler.UpdateTableDaily()

  def index = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.home(None)))
  }

  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.HomeController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

  def indexAuthorized = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.index(Option(request.identity))))
  }

  def profile = silhouette.SecuredAction.async { implicit request =>
    Future.successful(
      Ok(
        views.html.profile(
          Option(request.identity),
          electionDAOImpl.userElectionList(request.identity.email),
          electionDAOImpl.votedElectionList(request.identity.email)
        )
      )
    )
  }
}
