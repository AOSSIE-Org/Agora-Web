package controllers

import java.util.Date
import javax.inject._


import forms._
import models.Election
import models.daos.ElectionDAOImpl
import models.services.MailerService
import models.PassCodeGenerator
import utils.auth.DefaultEnv

import com.mohiva.play.silhouette.api.Silhouette
import org.bson.types.ObjectId

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString

import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc._
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc.MultipartFormData.FilePart
import play.core.parsers.Multipart.FileInfo
import play.api.libs.streams._

import java.io.File
import java.nio.file.attribute.PosixFilePermission._
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{Files, Path}
import java.util
import javax.inject._

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.FileInputStream

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

@Singleton
class ElectionController @Inject()(
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  mailerClient: MailerClient,
  ec: ExecutionContext
) extends Controller with I18nSupport {

  val electionDAOImpl = new ElectionDAOImpl()
  val mailerService = new MailerService(mailerClient)


  def create = silhouette.SecuredAction.async(parse.form(ElectionForm.form)) { implicit request =>
    def electionData = request.body
    if(electionDAOImpl.userElectionListCount(Option(electionData.creatorEmail))<3){
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
        inviteCode = s"${Random.alphanumeric take 10 mkString("")}",
        ballot = List.empty[String],
        voterList = List.empty[String]
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
    }else{
      Future.successful(
        Redirect(routes.HomeController.profile).flashing("error" -> Messages("maximum.election"))
      )
    }
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

  def voteGuest(id: String) = Action { implicit request =>
    val objectId = new ObjectId(id)
    val election = electionDAOImpl.view(objectId).head

    election.votingAlgo match {
      case "Range Voting" =>{
          Ok(
            views.html.ballot.scored(
              None,
              Option(electionDAOImpl.viewCandidate(objectId)),
              Option(id),
              Option(election.name),
              Option(election.description)

          )
        )
      }
      case "Instant Runoff 2-round" | "Nanson" | "Borda" | "Kemeny-Young" | "Schulze" | "Copeland" | "SMC" |  "Random Ballot" | "Coomb’s"
      | "Contingent Method" | "Minimax Condorcet" | "Top Cycle" | "Uncovered Set" | "Warren STV" | "Meek STV" | "Oklahoma Method" | "Baldwin" 
      | "Exhaustive ballot" | "Exhaustive ballot with dropoff" | "Scottish STV" | "Preferential block voting" | "Contingent Method" => {
      Ok(
          views.html.ballot.preferential(
            None,
            Option(electionDAOImpl.viewCandidate(objectId)),
            Option(id),
            Option(election.name),
            Option(election.description)

        )
      )
      }

      case "Approval" | "Proportional Approval voting" | "Satisfaction Approval voting" | "Sequential Proportional Approval voting"  => {
        Ok(
            views.html.ballot.approval(
              None,
              Option(electionDAOImpl.viewCandidate(objectId)),
              Option(id),
              Option(election.name),
              Option(election.description)

          )
        )
      }
      case "Majority" => {
        Ok(
            views.html.ballot.singleCandidate(
              None,
              Option(electionDAOImpl.viewCandidate(objectId)),
              Option(id),
              Option(election.name),
              Option(election.description)

          )
        )
      }
      case "Ranked Pairs" => {
        Ok(
            views.html.ballot.ranked(
              None,
              Option(electionDAOImpl.viewCandidate(objectId)),
              Option(id),
              Option(election.name),
              Option(election.description)

          )
        )
      }
      case "Cumulative voting" => {
        Ok(
            views.html.ballot.scored(
              None,
              Option(electionDAOImpl.viewCandidate(objectId)),
              Option(id),
              Option(election.name),
              Option(election.description)

          )
        )
      }
    }
  }

  def vote = Action (parse.form(BallotForm.form)) { implicit request =>
    val ballotData = request.body
    val objectId = new ObjectId(ballotData.id)
    if(electionDAOImpl.removeVoter(objectId ,PassCodeGenerator.decrypt(electionDAOImpl.getInviteCode(objectId),ballotData.passCode))){
      println(ballotData)
    electionDAOImpl.vote(new ObjectId(ballotData.id), ballotData.ballotInput)
    Redirect(routes.ElectionController.thankVoter()).flashing("success" -> Messages("thank.voting"))

    }
    else{
      Redirect(routes.ElectionController.voteGuest(ballotData.id)).flashing("error" -> Messages("could.not.verify"))
    }
  }

  def addVoter() = silhouette.SecuredAction.async( parse.form(VoterForm.form) ) { implicit request =>
    def voterData = request.body
    val objectId = new ObjectId(voterData.id)
    val con = electionDAOImpl.addVoter(objectId , voterData.email)
    if(con){
      mailerService.sendEmail(voterData.email, PassCodeGenerator.encrypt(electionDAOImpl.getInviteCode(objectId),voterData.email))
      Future.successful(
        Ok
          (
            views.html.election.election(Option(request.identity), electionDAOImpl.view(objectId))
          )
      )
    }
    else{
    Future.successful(
      Redirect(routes.ElectionController.viewElectionSecured(voterData.id)).flashing("error" -> Messages("error.voter"))
    )
    }
  }

  def thankVoter = Action { implicit request =>
    {
      Ok(views.html.election.thankVoting(None))
    }
  }

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  /**
   * Uses a custom FilePartHandler to return a type of "File" rather than
   * using Play's TemporaryFile class.
   */
  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) =>
      val attr = PosixFilePermissions.asFileAttribute(util.EnumSet.of(OWNER_READ, OWNER_WRITE))
      val path: Path = Files.createTempFile("multipartBody", "tempFile", attr)
      val file = path.toFile
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          FilePart(partName, filename, contentType, file)
      }
  }

  def addVoter( email : String , id : String) : Boolean = {
    val objectId = new ObjectId(id)
    val con = electionDAOImpl.addVoter(objectId ,email)
    if(con){
      println(electionDAOImpl.getInviteCode(objectId))
      mailerService.sendEmail(email, PassCodeGenerator.encrypt(electionDAOImpl.getInviteCode(objectId),email))
    }
    con
  }

  /**
   * A generic operation on the temporary file that deletes the temp file after completion.
   */
  private def operateOnTempFile(file: File) = {
    val size = Files.size(file.toPath)
    Files.deleteIfExists(file.toPath)
    size
  }

  /**
   * Uploads a multipart file as a POST request.
   * @return
   */
  def upload = silhouette.SecuredAction.async(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val id : String = request.body.dataParts.get("id").head.mkString
    println(id)
    val fileOption = request.body.file("name").map {
      case FilePart(key, filename, contentType, file) =>
      val inStream = new FileInputStream(file)
      val reader = new BufferedReader(new InputStreamReader(inStream));
      var  line : String = reader.readLine();
      while(line != null && line != ""){
            addVoter(line,id)
            line = reader.readLine();
      }
      val data = operateOnTempFile(file)

    }
    Future.successful(
      Ok
        (
          views.html.election.election(Option(request.identity), electionDAOImpl.view( new ObjectId(id)))
        )
    )
  }




}
