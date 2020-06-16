package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import javax.inject.Inject
import models.{Score, Winner}
import models.swagger.{ResponseMessage, ResultList}
import play.api.Configuration
import org.joda.time
import org.joda.time.DateTime
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import service.{CountVotes, ElectionService, UserService}
import utils.auth.{CustomSilhouette, DefaultEnv}

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Result")
class ResultController @Inject()(components: ControllerComponents,
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

  @ApiOperation(value = "Get election result", response = classOf[ResultList])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def result(id: String) = silhouette.SecuredAction.async { implicit request =>
    userService.retrieve(request.authenticator.loginInfo).flatMap {
      case Some(_) => electionService.retrieve(id).flatMap {
        case Some(election) if (election.loginInfo.get.providerID == request.authenticator.loginInfo.providerID
          && election.loginInfo.get.providerKey == request.authenticator.loginInfo.providerKey) =>
          if (election.realtimeResult || !new time.DateTime(election.end).isAfterNow) {
            val result = CountVotes.countVotesMethod(election.ballot, election.votingAlgo, election.candidates, election.noVacancies)
            if (result.nonEmpty) {
              val winnerList = for ((candidate, rational) <- result) yield {
                new Winner(candidate, Score(rational.numerator.intValue, rational.denominator.intValue))
              }
              electionService.updateWinner(winnerList, election.id.get)
              Future.successful(Ok(Json.toJson(winnerList)))
            }
            else {
              electionService.updateIsCounted(election.id.get)
              Future.successful(NoContent)
            }
          } else {
            electionService.updateIsCounted(election.id.get)
            Future.successful(NoContent)
          }
        case None => Future.successful(NotFound(Json.toJson("message" ->"Election not found")))
      }
    }
  }

}
