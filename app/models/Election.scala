package models

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._
import com.mohiva.play.silhouette.api.LoginInfo
import formatters.json.ElectionData
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

/**
  * Election model which is created by user or guest
  *
  * @param id                  The name of the election
  * @param name                The name of the election
  * @param electionType                electionType of election
  * @param description         The short description about the election
  * @param creatorName         The name of the creator of the election
  * @param creatorEmail        The email of the creator of the election
  * @param start               The start date of the election
  * @param end                 The end date of the election
  * @param realtimeResult      Specify whether show the results in real time or not
  * @param votingAlgo          The voting alogorithm for the election
  * @param candidates          The canditate list for the election
  * @param ballotVisibility    Specify  the ballot visibility level
  * @param voterListVisibility Specify  the voter list visibility level
  * @param isInvite            Specify Whether the election is invitable or not
  * @param isCompleted         Specify the election is completed or not
  * @param isStarted           Specify  the election is started or not
  * @param createdTime         created time of election
  * @param adminLink           admin link for the election
  * @param inviteCode          secret code for the election
  * @param ballot              ballot list of the election
  * @param voterList           voter list of the election
  * @param winners             winner list
  * @param isCounted           is the election is counted or not
  */
@ApiModel(description = "Election model", value = "Election")
case class Election(
                     id: Option[String],
                     name: String,
                     description: String,
                     electionType: String,
                     creatorName: String,
                     creatorEmail: String,
                     start: DateTime,
                     end: DateTime,
                     realtimeResult: Boolean,
                     votingAlgo: String,
                     candidates: List[String],
                     ballotVisibility: String,
                     voterListVisibility: Boolean,
                     isInvite: Boolean,
                     isCompleted: Boolean,
                     isStarted: Boolean,
                     createdTime: DateTime,
                     adminLink: String,
                     inviteCode: String,
                     ballot: List[Ballot],
                     voterList: List[Voter],
                     winners: List[Winner],
                     isCounted: Boolean,
                     noVacancies: Int,
                     @ApiModelProperty(hidden = true, readOnly = true) loginInfo: Option[LoginInfo]
                   ) {
  def status(): String = {
    if (!isStarted) "Not yet started"
    else if (isStarted && !isCompleted) "Active"
    else "Completed"
  }

  def getElectionData: ElectionData = {
    var ballots: List[Ballot] = List.empty
    if(ballotVisibility.equalsIgnoreCase("public"))
      ballots = ballot
    ElectionData(name, description, electionType, candidates, ballotVisibility, voterListVisibility, start, end, isInvite, realtimeResult, votingAlgo, noVacancies, ballots)
  }


}

object Election {

  implicit val jodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit val loginInfoReader = Json.reads[LoginInfo]
  implicit val loginInfoWriter = Json.writes[LoginInfo]

  implicit object ElectionWrites extends OWrites[Election] {
    def writes(election: Election): JsObject = {
      (election.id, election.loginInfo) match {
        case (Some(id), Some(loginInfo)) =>
          Json.obj(
            "_id" -> id,
            "name" -> election.name,
            "description" -> election.description,
            "electionType" -> election.electionType,
            "creatorName" -> election.creatorName,
            "creatorEmail" -> election.creatorEmail,
            "start" -> election.start,
            "end" -> election.end,
            "realtimeResult" -> election.realtimeResult,
            "votingAlgo" -> election.votingAlgo,
            "candidates" -> election.candidates,
            "ballotVisibility" -> election.ballotVisibility,
            "voterListVisibility" -> election.voterListVisibility,
            "isInvite" -> election.isInvite,
            "isCompleted" -> election.isCompleted,
            "isStarted" -> election.isStarted,
            "createdTime" -> election.createdTime,
            "adminLink" -> election.adminLink,
            "inviteCode" -> election.inviteCode,
            "ballot" -> election.ballot,
            "voterList" -> election.voterList,
            "winners" -> election.winners,
            "isCounted" -> election.isCounted,
            "noVacancies" -> election.noVacancies,
            "loginInfo" -> Json.obj(
              "providerID" -> loginInfo.providerID,
              "providerKey" -> loginInfo.providerKey
            )
          )
        case (Some(id), None) =>
          Json.obj(
            "_id" -> id,
            "name" -> election.name,
            "description" -> election.description,
            "electionType" -> election.electionType,
            "creatorName" -> election.creatorName,
            "creatorEmail" -> election.creatorEmail,
            "start" -> election.start,
            "end" -> election.end,
            "realtimeResult" -> election.realtimeResult,
            "votingAlgo" -> election.votingAlgo,
            "candidates" -> election.candidates,
            "ballotVisibility" -> election.ballotVisibility,
            "voterListVisibility" -> election.voterListVisibility,
            "isInvite" -> election.isInvite,
            "isCompleted" -> election.isCompleted,
            "isStarted" -> election.isStarted,
            "createdTime" -> election.createdTime,
            "adminLink" -> election.adminLink,
            "inviteCode" -> election.inviteCode,
            "ballot" -> election.ballot,
            "voterList" -> election.voterList,
            "winners" -> election.winners,
            "isCounted" -> election.isCounted,
            "noVacancies" -> election.noVacancies
          )
        case (None, Some(loginInfo)) =>
          Json.obj(
            "name" -> election.name,
            "description" -> election.description,
            "electionType" -> election.electionType,
            "creatorName" -> election.creatorName,
            "creatorEmail" -> election.creatorEmail,
            "start" -> election.start,
            "end" -> election.end,
            "realtimeResult" -> election.realtimeResult,
            "votingAlgo" -> election.votingAlgo,
            "candidates" -> election.candidates,
            "ballotVisibility" -> election.ballotVisibility,
            "voterListVisibility" -> election.voterListVisibility,
            "isInvite" -> election.isInvite,
            "isCompleted" -> election.isCompleted,
            "isStarted" -> election.isStarted,
            "createdTime" -> election.createdTime,
            "adminLink" -> election.adminLink,
            "inviteCode" -> election.inviteCode,
            "ballot" -> election.ballot,
            "voterList" -> election.voterList,
            "winners" -> election.winners,
            "isCounted" -> election.isCounted,
            "noVacancies" -> election.noVacancies,
            "loginInfo" -> Json.obj(
              "providerID" -> loginInfo.providerID,
              "providerKey" -> loginInfo.providerKey
            )
          )
        case (None, None) =>
          Json.obj(
            "name" -> election.name,
            "description" -> election.description,
            "electionType" -> election.electionType,
            "creatorName" -> election.creatorName,
            "creatorEmail" -> election.creatorEmail,
            "start" -> election.start,
            "end" -> election.end,
            "realtimeResult" -> election.realtimeResult,
            "votingAlgo" -> election.votingAlgo,
            "candidates" -> election.candidates,
            "ballotVisibility" -> election.ballotVisibility,
            "voterListVisibility" -> election.voterListVisibility,
            "isInvite" -> election.isInvite,
            "isCompleted" -> election.isCompleted,
            "isStarted" -> election.isStarted,
            "createdTime" -> election.createdTime,
            "adminLink" -> election.adminLink,
            "inviteCode" -> election.inviteCode,
            "ballot" -> election.ballot,
            "voterList" -> election.voterList,
            "winners" -> election.winners,
            "isCounted" -> election.isCounted,
            "noVacancies" -> election.noVacancies
          )
      }
    }
  }

  implicit object ElectionReads extends Reads[Election] {
    def reads(json: JsValue): JsResult[Election] = json match {
      case election: JsObject =>
        Try {
          val id = (election \ "_id" \ "$oid").asOpt[String]

          val name = (election \ "name").as[String]
          val description = (election \ "description").as[String]
          val electionType = (election \ "electionType").as[String]
          val creatorName = (election \ "creatorName").as[String]
          val creatorEmail = (election \ "creatorEmail").as[String]
          val start = (election \ "start").as[DateTime]
          val end = (election \ "end").as[DateTime]
          val realtimeResult = (election \ "realtimeResult").as[Boolean]
          val votingAlgo = (election \ "votingAlgo").as[String]
          val candidates = (election \ "candidates").as[List[String]]
          val ballotVisibility = (election \ "ballotVisibility").as[String]
          val voterListVisibility = (election \ "voterListVisibility").as[Boolean]
          val isInvite = (election \ "isInvite").as[Boolean]
          val isCompleted = (election \ "isCompleted").as[Boolean]
          val isStarted = (election \ "isStarted").as[Boolean]
          val createdTime = (election \ "createdTime").as[DateTime]
          val adminLink = (election \ "adminLink").as[String]
          val inviteCode = (election \ "inviteCode").as[String]
          val ballot = (election \ "ballot").as[List[Ballot]]
          val voterList = (election \ "voterList").as[List[Voter]]
          val winners = (election \ "winners").as[List[Winner]]
          val isCounted = (election \ "isCounted").as[Boolean]
          val noVacancies = (election \ "noVacancies").as[Int]
          val loginInfo = (election \ "loginInfo").asOpt[LoginInfo]

          JsSuccess(
            new Election(
              id,
              name,
              description,
              electionType,
              creatorName,
              creatorEmail,
              start,
              end,
              realtimeResult,
              votingAlgo,
              candidates,
              ballotVisibility,
              voterListVisibility,
              isInvite,
              isCompleted,
              isStarted,
              createdTime,
              adminLink,
              inviteCode,
              ballot,
              voterList,
              winners,
              isCounted,
              noVacancies,
              loginInfo
            )
          )
        } match {
          case Success(value) => value
          case Failure(cause) => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

}
