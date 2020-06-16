package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.json.{BallotData, ElectionData, VoteVerificationData}
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import javax.inject.Inject
import models.swagger.ResponseMessage
import models.{Ballot, PassCodeGenerator, Voter, md5HashString}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.{ElectionService, UserService}
import utils.auth.{CustomSilhouette, DefaultEnv}
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Vote")
class VoteController @Inject()(components: ControllerComponents,
                               userService: UserService,
                               configuration: Configuration,
                               silhouette: CustomSilhouette[DefaultEnv],
                               electionService: ElectionService,
                               clock: Clock,
                               credentialsProvider: CredentialsProvider,
                               authInfoRepository: AuthInfoRepository,
                               passwordHasherRegistry: PasswordHasherRegistry,
                               messagesApi: MessagesApi)
                              (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  @ApiOperation(value = "Cast your vote", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Ballot data",
        required = true,
        paramType = "body",
        dataType = "formatters.json.BallotData"

      )
    )
  )
  def vote(id: String) = silhouette.UserAwareAction.async(parse.json) { implicit request =>
    request.body.validate[BallotData].map {data =>
      electionService.retrieve(id).flatMap {
        case Some(election) =>
        if(election.electionType == "Private"){
          val hashedEmail = PassCodeGenerator.decrypt(election.inviteCode,data.passCode)
          if (isVoterInList(hashedEmail, election.voterList)) {
            val ballot = Ballot(data.ballotInput, hashedEmail)
            electionService.vote(id, ballot).flatMap(_ =>
              electionService.removeVoter(id, hashedEmail).flatMap(_ => Future.successful(Ok(Json.toJson("message" -> "Thank you for voting")))))
          } else Future.successful(NotFound(Json.toJson("message" -> "Invalid pass code.")))
        }
        else{
          val ipAddress = request.remoteAddress
          val hashedIP = md5HashString.hashString(ipAddress.concat(election.inviteCode))
          if(!isVoterInBallot(hashedIP, election.ballot)){
            val ballot = Ballot(data.ballotInput, hashedIP)
            electionService.vote(id, ballot).flatMap(_ =>
              Future.successful(Ok(Json.toJson("message" -> "Thank you for voting"))))
          }
          else Future.successful(NotFound(Json.toJson("message" -> "Already Voted")))
        }
        case None => Future.successful(NotFound(Json.toJson("message" -> "Election with specified id not found")))
      }
    }.recoverTotal{
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Verify Private Election voters link", response = classOf[ElectionData])
  def getElectionData(id: String, pass: String) = silhouette.UserAwareAction.async { implicit request =>
    electionService.retrieve(id).flatMap {
      case Some(election) =>
        val email = PassCodeGenerator.decrypt(election.inviteCode, pass)
        if (isVoterInList(email, election.voterList)) {
          Future.successful(Ok(Json.toJson(election.getElectionData)))
        } else Future.successful(NotFound)

      case None => Future.successful(NotFound(Json.toJson("message" -> "Election with specified id not found")))
    }
  }

  @ApiOperation(value = "Verify Public Election voters link", response = classOf[ElectionData])
  def verifyVotersPoll(id: String) = silhouette.UserAwareAction.async { implicit request =>
    electionService.retrieve(id).flatMap {
      case Some(election) =>
        val ipAddress = request.remoteAddress
        val hashedIP = md5HashString.hashString(ipAddress.concat(election.inviteCode))
        if (!isVoterInBallot(hashedIP, election.ballot)) {
          Future.successful(Ok(Json.toJson(election.getElectionData)))
        } else Future.successful(NotFound)

      case None => Future.successful(NotFound(Json.toJson("message" -> "Election with specified id not found")))
    }
  }

  private def isVoterInList(hashedEmail: String, list: List[Voter]): Boolean = {
    for (voterD <- list) {
      if (voterD.hash == hashedEmail)
        return true
    }
    return false
  }

  private def isVoterInBallot(hashedIP: String, list: List[Ballot]): Boolean = {
    for (voterD <- list) {
      if (voterD.hash == hashedIP)
        return true
    }
    return false
  }
}
