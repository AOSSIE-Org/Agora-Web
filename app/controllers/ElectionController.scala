package controllers

import java.util.Date
import javax.inject._


import forms._
import models.Election
import models.Ballot
import models.Voter
import models.Winner
import models.daos.ElectionDAOImpl
import models.daos.ResultDAOImpl
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
import java.util.UUID

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.services.{ AuthTokenService, UserService }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository

import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry


import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.FileInputStream
import java.net.URLDecoder

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

import models.services.Countvotes
import models.User



/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

@Singleton
class ElectionController @Inject()(
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  mailerClient: MailerClient,
  userService: UserService,
  authTokenService: AuthTokenService,
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry,
  ec: ExecutionContext
) extends Controller with I18nSupport {

  val electionDAOImpl = new ElectionDAOImpl()
  val resultFileDAOImpl = new ResultDAOImpl()
  val mailerService = new MailerService(mailerClient, messagesApi)

  def result(id : String) = Action { implicit request =>
    Ok.sendFile(resultFileDAOImpl.getResult(id))
  }


  def create = silhouette.SecuredAction.async(parse.form(ElectionForm.form)) { implicit request =>
    def electionData = request.body
    if(electionDAOImpl.userElectionListCount(Option(electionData.creatorEmail))<3){
      val objectId = new ObjectId()
      val election = new Election(
        objectId,
        electionData.name,
        electionData.description,
        electionData.creatorName,
        electionData.creatorEmail,
        electionData.startingDate,
        electionData.endingDate,
        electionData.isRealTime,
        electionData.votingAlgo,
        electionData.candidates.split(",").toList,
        electionData.ballotVisibility,
        electionData.voterListVisibility,
        electionData.isInvite,
        isCompleted = false,
        isStarted = false,
        createdTime = new Date(),
        adminLink = "",
        inviteCode = s"${Random.alphanumeric take 10 mkString("")}",
        ballot = List.empty[Ballot],
        voterList = List.empty[Voter],
        winners = List.empty[Winner],
        isCounted = false,
        noVacancies = electionData.noVacancies
      )
      electionDAOImpl.save(election)
      Future.successful(
        Ok(
          views.html.election.adminElectionView(Option(request.identity), electionDAOImpl.view(objectId))
        )
      )
    }else{
      Future.successful(
        Redirect(routes.HomeController.profile).flashing("error" -> Messages("maximum.election"))
      )
    }
  }

  def createUserView = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.election.addElection(Some(request.identity))))
  }

  def viewElection(id: String) = silhouette.UnsecuredAction.async { implicit request =>
    val objectId = new ObjectId(id)
    Future.successful(Ok(views.html.election.userElectionView(None, electionDAOImpl.view(objectId))))
  }

  def viewElectionSecured(id: String) = silhouette.SecuredAction.async { implicit request =>
    val objectId = new ObjectId(id)
    val electionList = electionDAOImpl.view(objectId)
    if(electionList.size>0){
    val election = electionList.head
    if(request.identity.email==electionDAOImpl.getCreatorEmail(objectId)){
      Future.successful(
        Ok(views.html.election.adminElectionView(Option(request.identity), electionDAOImpl.view(objectId)))
      )
    }
    else{
      Future.successful(
        Ok(views.html.election.userElectionView(Option(request.identity), electionDAOImpl.view(objectId)))
      )
    }
  }
  else{
    Future.successful(
      Redirect(routes.HomeController.profile()).flashing("error" -> Messages("invalid.id"))
    )
  }
}

  def voteGuest(id: String) = Action { implicit request =>
    val objectId = new ObjectId(id)
    if(electionDAOImpl.view(objectId).size != 0){
      val election = electionDAOImpl.view(objectId).head
      if(election.isStarted){
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
            case _ => {
              Redirect(routes.HomeController.index()).flashing("error" -> Messages("Your link is invalid"))
            }
          }
        }
        else{
          Redirect(routes.ElectionController.redirectVoter()).flashing("error" -> Messages("Election is not yet started"))
        }
      }else{
        Redirect(routes.ElectionController.redirectVoter()).flashing("error" -> Messages("invalid.id"))
      }
  }

  def vote = Action (parse.form(BallotForm.form)) { implicit request =>
    val ballotData = request.body
    val objectId = new ObjectId(ballotData.id)
    val electionList = electionDAOImpl.view(objectId)
    if(electionList.size>0){
      var election = electionList.head
      if(electionDAOImpl.removeVoter(objectId ,PassCodeGenerator.decrypt(election.inviteCode,ballotData.passCode))){
        val ballot  =  new Ballot(ballotData.ballotInput,PassCodeGenerator.decrypt(election.inviteCode,ballotData.passCode))
        if(electionDAOImpl.vote(new ObjectId(ballotData.id), ballot)){
          if(election.realtimeResult){
            resultFileDAOImpl.saveResult(electionDAOImpl.getBallots(objectId),election.votingAlgo,election.candidates,election.id)
          }
        }
        Redirect(routes.ElectionController.redirectVoter()).flashing("success" -> Messages("thank.voting"))
      }
      else{
        Redirect(routes.ElectionController.voteGuest(ballotData.id)).flashing("error" -> Messages("could.not.verify"))
      }
    }
    else{
      Redirect(routes.ElectionController.voteGuest(ballotData.id)).flashing("error" -> Messages("invalid.id"))
    }
  }

  def addVoter() = silhouette.SecuredAction.async( parse.form(VoterForm.form) ) { implicit request =>
    def voterData = request.body
    try{
      val splitVoter = voterData.email.split(",")
      val voter = new Voter(splitVoter(0),splitVoter(1))
      val objectId = new ObjectId(voterData.id)
      val con = electionDAOImpl.addVoter(objectId , voter)
      val electionList = electionDAOImpl.view(objectId)
      if (electionList.size > 0) {
        if (con) {
          val link = routes.ElectionController.voteGuest(voterData.id).absoluteURL()
          mailerService.sendPassCodeEmail(voter.email,voter.name,electionList.head.creatorName,electionList.head.creatorEmail,electionList.head.name,link,electionList.head.description, PassCodeGenerator.encrypt(electionDAOImpl.getInviteCode(objectId).get,voter.email),voterData.id)
          Future.successful(
            Ok(views.html.election.adminElectionView(Option(request.identity), electionDAOImpl.view(objectId)))
          )
        }
        else{
          Future.successful(
            Redirect(routes.ElectionController.viewElectionSecured(voterData.id)).flashing("error" -> Messages("error.voter"))
          )
        }
      }
      else{
        Future.successful(
          Redirect(routes.HomeController.profile()).flashing("error" -> Messages("invalid.id"))
        )
      }
    }
    catch {
      case e: Exception => {
        Future.successful(
          Redirect(routes.ElectionController.viewElectionSecured(voterData.id)).flashing("error" -> Messages("format.voter"))
        )
      }
    }
  }

  def redirectVoter = Action { implicit request =>
    {
      Ok(views.html.election.redirectVoting(None))
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

  def addVoter( email : String , id : String , link: String) : Boolean = {
    val objectId = new ObjectId(id)
    val voter = new Voter(email.split(",")(0),email.split(",")(1))
    val con = electionDAOImpl.addVoter(objectId ,voter)
    val electionList = electionDAOImpl.view(objectId)
    if(electionList.size>0){
      if(con){
        mailerService.sendPassCodeEmail(voter.email,voter.name,electionList.head.creatorName,electionList.head.creatorEmail,electionList.head.name,link,electionList.head.description, PassCodeGenerator.encrypt(electionDAOImpl.getInviteCode(objectId).get,voter.email),id)
      }
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
    try{
      val fileOption = request.body.file("emailFile").map {
        case FilePart(key, filename, contentType, file) =>
        val inStream = new FileInputStream(file)
        val reader = new BufferedReader(new InputStreamReader(inStream));
        var  line : String = reader.readLine();
        val link = routes.ElectionController.voteGuest(id).absoluteURL()
        while(line != null && line != ""){
              addVoter(line,id,link)
              line = reader.readLine();
        }
        val data = operateOnTempFile(file)

      }
      Future.successful(
        Ok
          (
            views.html.election.adminElectionView(Option(request.identity), electionDAOImpl.view( new ObjectId(id)))
          )
      )
    }
  catch {
    case e:  Exception =>
        Future.successful(
          Redirect(routes.ElectionController.viewElectionSecured(id)).flashing("error" -> Messages("file.error"))
        )

    }
  }


  def viewBallot(id : String) = Action {  implicit request =>
    val objectId = new ObjectId(id)
    if(electionDAOImpl.getBallotVisibility(objectId)!=None){
      if(electionDAOImpl.getBallotVisibility(objectId).get=="Public"){
        Ok(
          views.html.ballot.viewPublic(None,electionDAOImpl.getBallots(objectId))
        )
      }
      else if(electionDAOImpl.getBallotVisibility(objectId).get=="Visible"){
        Ok(
          views.html.ballot.viewPrivate(None,electionDAOImpl.getBallots(objectId))
        )
      }
      else{
        Redirect(routes.HomeController.index()).flashing("error" -> Messages("dont.access"))
      }
    }
    else{
      Redirect(routes.HomeController.index()).flashing("error" -> Messages("invalid.id"))
    }
  }

  def updateElection(id: String) = silhouette.SecuredAction.async { implicit request =>
    val objectId = new ObjectId(id)
    val electionList = electionDAOImpl.view(objectId)
    if(electionList.size>0){
      val election = electionList.head
      if(!election.isStarted && request.identity.email.get == election.creatorEmail ){
        Future.successful(
          Ok(views.html.election.editElection(Option(request.identity),election))
        )
      }else{
        Future.successful(
          Redirect(routes.HomeController.profile()).flashing("error" -> Messages("dont.access"))
        )

      }
    }
    else{
      Future.successful(
        Redirect(routes.HomeController.profile()).flashing("error" -> Messages("invalid.id"))
      )
    }
  }

  def update = silhouette.SecuredAction.async(parse.form(EditElectionForm.form)) { implicit request =>
    def electionData = request.body
    val objectId = new ObjectId(electionData.id)
    val electionList = electionDAOImpl.view(objectId)
    if(electionList.size>0){
      val oldElection = electionList.head
      val election = new Election(
          objectId,
          electionData.name,
          electionData.description,
          electionData.creatorName,
          electionData.creatorEmail,
          electionData.startingDate,
          electionData.endingDate,
          electionData.isRealTime,
          electionData.votingAlgo,
          electionData.candidates.split(",").toList,
          electionData.ballotVisibility,
          electionData.voterListVisibility,
          electionData.isInvite,
          isCompleted = false,
          isStarted = false,
          createdTime = oldElection.createdTime,
          adminLink = oldElection.adminLink,
          inviteCode = oldElection.inviteCode,
          ballot = oldElection.ballot,
          voterList = oldElection.voterList,
          winners = oldElection.winners,
          isCounted = false,
          noVacancies = electionData.noVacancies
        )
        if(electionDAOImpl.update(election)){
        Future.successful(
          Ok(
            views.html.election.adminElectionView(Option(request.identity), electionDAOImpl.view(objectId))
          )
        )
      }else{
        Future.successful(
          Redirect(routes.HomeController.profile()).flashing("error" -> Messages("dont.access"))
        )
      }
    }else{
      Future.successful(
        Redirect(routes.HomeController.profile()).flashing("error" -> Messages("invalid.id"))
      )
    }
  }


  def deleteElection() = silhouette.SecuredAction.async(parse.form(DeleteForm.form)) { implicit request =>
    def electionData = request.body
    val objectId = new ObjectId(electionData.id)
    val result = electionDAOImpl.delete(objectId)
    if(result!=None){
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
    else{
      Future.successful(
        Redirect(routes.HomeController.profile()).flashing("error" -> Messages("invalid.id"))
      )
    }
  }
}
