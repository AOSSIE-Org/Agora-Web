package controllers

import java.util.Date
import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import forms._
import models.Election
import models.daos.ElectionDAOImpl
import org.bson.types.ObjectId
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

@Singleton
class ElectionController @Inject()(
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv]
) extends Controller with I18nSupport {
  val electionDAOImpl = new ElectionDAOImpl()

  def create = silhouette.SecuredAction.async(parse.form(ElectionForm.form)) { implicit request =>
    def electionData = request.body
    val election = new Election(
      new ObjectId,
      electionData.name,
      electionData.description,
      electionData.creatorName,
      electionData.creatorEmail,
      electionData.startingDate,
      electionData.endingDate,
      electionData.isRealTime,
      electionData.votingAlgo,
      electionData.candidates.split(",").toList,
      electionData.isPublic,
      electionData.isInvite,
      isCompleted = false,
      createdTime = new Date(),
      adminLink = "",
      inviteLink = "",
      ballot = List.empty[String]
    )
    electionDAOImpl.save(election)
    Future.successful(
      Ok(
        views.html.profile(
          Option(request.identity),
          electionDAOImpl.userElectionList(request.identity.email)
        )
      )
    )
  }

  def createGuestView = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.election.addElection(None)))
  }

  def createUserView = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.election.addElection(Some(request.identity))))
  }

  def viewElection(id: String) = silhouette.UnsecuredAction.async { implicit request =>
    val objectId = new ObjectId(id)
    Future.successful(Ok(views.html.election.election(None, electionDAOImpl.view(objectId))))
  }

  def viewElectionSecured(id: String) = silhouette.SecuredAction.async { implicit request =>
    val objectId = new ObjectId(id)
    Future.successful(
      Ok(views.html.election.election(Option(request.identity), electionDAOImpl.view(objectId)))
    )
  }

  def voteGuest(id: String) = silhouette.UnsecuredAction.async { implicit request =>
    val objectId = new ObjectId(id)
    Future.successful(
      Ok(
        views.html.ballot.preferential(
          None,
          Option(electionDAOImpl.viewCandidate(objectId)),
          Option(id)
        )
      )
    )
  }

  def voteUser(id: String) = silhouette.SecuredAction.async { implicit request =>
    val objectId = new ObjectId(id)
    Future.successful(
      Ok(
        views.html.ballot.ranked(
          Option(request.identity),
          Option(electionDAOImpl.viewCandidate(objectId: ObjectId)),
          Option(id)
        )
      )
    )
  }

  def vote = silhouette.UnsecuredAction.async(parse.form(BallotForm.form)) { implicit request =>
    val ballotData = request.body
    electionDAOImpl.vote(new ObjectId(ballotData.id), ballotData.ballotInput)
    Future.successful(Ok(views.html.home(None)))
  }
}
