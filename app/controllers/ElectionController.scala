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
    def values = ElectionForm.form.bindFromRequest.data
    val name = values("name").toString
    val description = values("description").toString
    val creatorName = values("creatorName").toString
    val creatorEmail = values("creatorEmail").toString
    val format = new java.text.SimpleDateFormat("MM/dd/yyyy")
    val start =format.parse(values("start").toString)
    val end = format.parse(values("end").toString)
    val realtimeResult  = values("realtimeResult").toBoolean
    val votingAlgo = values("votingAlgo").toString
    val candidates = values("candidates").split(",").toList
    val isPublic = values("isPublic").toBoolean
    val isInvite = values("isInvite").toBoolean
    val isCompleted = false
    val createdTime = new java.util.Date
    val adminLink = ""
    val inviteLink = ""
    val election = new Election(new ObjectId,name,description,creatorName, creatorEmail,start,end,realtimeResult,votingAlgo
    ,candidates,isPublic,isInvite,isCompleted,createdTime,adminLink,inviteLink)
    electionDAOImpl.save(election)
    Future.successful(Ok(views.html.profile(Option(request.identity), electionDAOImpl.userElectionList(request.identity.email))))
  }

  def createGuestView = silhouette.UnsecuredAction.async( implicit request => {
    Future.successful(Ok(views.html.election.addElection(None)))
  })


  def createUserView = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.election.addElection(Option(request.identity))))
  }

  def viewElection(id: String) = silhouette.UnsecuredAction.async( implicit request =>{
      val objectId = new ObjectId(id);

      Future.successful(Ok(views.html.election.election(None,electionDAOImpl.view(objectId) )))
  })

  def viewElectionSecured(id: String) = silhouette.SecuredAction.async( implicit request =>{
      val objectId = new ObjectId(id);

      Future.successful(Ok(views.html.election.election(Option(request.identity),  electionDAOImpl.view(objectId) )))
  })


  def voteGuest(id: String) =  silhouette.UnsecuredAction.async( implicit request =>{
  val objectId = new ObjectId(id);
      Future.successful(Ok(views.html.ballot.approval(None,electionDAOImpl.viewCandidate(objectId))))

  })

  def voteUser(id: String) = silhouette.SecuredAction.async( implicit request =>{
  val objectId = new ObjectId(id);
      Future.successful(Ok(views.html.ballot.approval(Option(request.identity),  electionDAOImpl.viewCandidate(objectId: ObjectId))))
  })

  def vote() =  silhouette.UnsecuredAction.async( implicit request =>{
        println(request)
        Future.successful(Ok(views.html.home(None)))
  })

}
