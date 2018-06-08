package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.json.BallotData
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import javax.inject.Inject
import models.swagger.ResponseMessage
import models.{Ballot, PassCodeGenerator, Voter}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.{ElectionService, UserService}
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Vote")
class VoteController @Inject()(components: ControllerComponents,
                               userService: UserService,
                               configuration: Configuration,
                               silhouette: Silhouette[DefaultEnv],
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
          val email = PassCodeGenerator.decrypt(election.inviteCode,data.passCode)
          if (isVoterInList(email, election.voterList)) {
            val ballot = Ballot(data.ballotInput, email)
            electionService.vote(id, ballot).flatMap(_ =>
              electionService.removeVoter(id, email).flatMap(_ => Future.successful(Ok("Thank you for voting"))))
          } else Future.successful(NotFound("Invalid pass code."))

        case None => Future.successful(NotFound("Election with specified id not found"))
      }
    }.recoverTotal{
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  private def isVoterInList(email: String, list: List[Voter]): Boolean = {
    for (voterD <- list) {
      if (voterD.email == email)
        return true
    }
    return false
  }
}
