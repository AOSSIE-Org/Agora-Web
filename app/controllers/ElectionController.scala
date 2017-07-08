package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import utils.auth.DefaultEnv

import models.Election
import play.api.data._
import play.api.data.Forms._
import forms.ElectionForm
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import scala.concurrent.Future

import scala.concurrent._
import ExecutionContext.Implicits.global

import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry

import models.daos.ElectionDAOImpl
import org.bson.types.ObjectId;

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */

@Singleton
class ElectionController @Inject()(val messagesApi: MessagesApi , silhouette: Silhouette[DefaultEnv] ) extends Controller with I18nSupport  {


    val electionDAOImpl = new ElectionDAOImpl();




  def create = silhouette.SecuredAction.async { implicit request =>
    ElectionForm.form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(views.html.addElection(request.identity ,formWithErrors))),
      {

        election => {
          electionDAOImpl.save(election);
          Future.successful(Ok(views.html.profile(request.identity, electionDAOImpl.userElectionList(request.identity.email))))
        }
      })
  }

  def createGuestView = silhouette.UnsecuredAction.async( implicit request => {
    Future.successful(Ok(views.html.addElection(null, ElectionForm.form)))
  })


  def createUserView = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.addElection(request.identity,ElectionForm.form)))
  }

  def viewElection(id: String) = silhouette.UnsecuredAction.async( implicit request =>{
      val objectId = new ObjectId(id);

      Future.successful(Ok(views.html.election(null,  electionDAOImpl.view(objectId: ObjectId) )))
  })

  def viewElectionSecured(id: String) = silhouette.SecuredAction.async( implicit request =>{
      val objectId = new ObjectId(id);

      Future.successful(Ok(views.html.election(request.identity,  electionDAOImpl.view(objectId: ObjectId) )))
  })

}
