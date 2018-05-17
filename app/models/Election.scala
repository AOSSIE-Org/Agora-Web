package models

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._
import ai.x.play.json.Jsonx
import scala.util.{Failure, Success, Try}

/**
 * Election model which is created by user or guest
 *
 * @param id The name of the election
 * @param name The name of the election
 * @param description The short description about the election
 * @param creatorName The name of the creator of the election
 * @param creatorEmail The email of the creator of the election
 * @param start The start date of the election
 * @param end The end date of the election
 * @param realtimeResult Specify whether show the results in real time or not
 * @param votingAlgo The voting alogorithm for the election
 * @param candidates The canditate list for the election
 * @param ballotVisibility Specify  the ballot visibility level
 * @param voterListVisibility Specify  the voter list visibility level
 * @param isInvite Specify Whether the election is invitable or not
 * @param isCompleted Specify the election is completed or not
 * @param isStarted Specify  the election is started or not
 * @param createdTime created time of election
 * @param adminLink  admin link for the election
 * @param inviteCode secret code for the election
 * @param ballot ballot list of the election
 * @param voterList voter list of the election
 * @param winners winner list
 * @param isCounted is the election is counted or not
 */

case class Election(
  id: Option[String],
  name: String,
  description: String,
  creatorName: String,
  creatorEmail: String,
  start: Date,
  end: Date,
  realtimeResult: Boolean,
  votingAlgo: String,
  candidates: List[String],
  ballotVisibility: String,
  voterListVisibility : Boolean,
  isInvite: Boolean,
  isCompleted: Boolean,
  isStarted : Boolean,
  createdTime: Date,
  adminLink: String,
  inviteCode: String,
  ballot: List[Ballot],
  voterList : List[Voter],
  winners : List[Winner],
  isCounted : Boolean,
  noVacancies : Int
) {
  def status(): String = {
    if (!isStarted) "Not yet started"
    else if (isStarted && !isCompleted) "Active"
    else "Completed"
  }
}

object Election {

  implicit object DateFormat extends Format[Date] {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    def reads(json:JsValue): JsResult[Date] = JsSuccess(format.parse(json.as[String]))
    def writes(date:Date) = JsString(format.format(date))
  }

  implicit val electionFormat : Format[Election]  = Jsonx.formatCaseClass[Election]

  implicit object ElectionWrites extends OWrites[Election] {
    def writes(election: Election): JsObject =
      election.id match {
        case Some(id) =>
          Json.obj(
            "_id" -> election.id,
            "name" -> election.name,
            "description" -> election.description,
            "creatorName" -> election.creatorName,
            "creatorEmail" -> election.creatorEmail,
            "start" -> election.start,
            "end" -> election.end,
            "realtimeResult" -> election.realtimeResult,
            "votingAlgo" -> election.votingAlgo,
            "candidates" -> Json.arr(election.candidates),
            "ballotVisibility" -> election.ballotVisibility,
            "voterListVisibility" -> election.voterListVisibility,
            "isInvite" -> election.isInvite,
            "isCompleted" -> election.isCompleted,
            "isStarted" -> election.isStarted,
            "createdTime" -> election.createdTime,
            "adminLink" -> election.adminLink,
            "inviteCode" -> election.inviteCode,
            "ballot" -> Json.arr(election.ballot),
            "voterList" -> Json.arr(election.voterList),
            "winners" -> Json.arr(election.winners),
            "isCounted" -> election.isCounted,
            "noVacancies" -> election.noVacancies
          )
        case _ =>
          Json.obj(
            "name" -> election.name,
            "description" -> election.description,
            "creatorName" -> election.creatorName,
            "creatorEmail" -> election.creatorEmail,
            "start" -> election.start,
            "end" -> election.end,
            "realtimeResult" -> election.realtimeResult,
            "votingAlgo" -> election.votingAlgo,
            "candidates" -> Json.arr(election.candidates),
            "ballotVisibility" -> election.ballotVisibility,
            "voterListVisibility" -> election.voterListVisibility,
            "isInvite" -> election.isInvite,
            "isCompleted" -> election.isCompleted,
            "isStarted" -> election.isStarted,
            "createdTime" -> election.createdTime,
            "adminLink" -> election.adminLink,
            "inviteCode" -> election.inviteCode,
            "ballot" -> Json.arr(election.ballot),
            "voterList" -> Json.arr(election.voterList),
            "winners" -> Json.arr(election.winners),
            "isCounted" -> election.isCounted,
            "noVacancies" -> election.noVacancies
          )
      }

    implicit object ElectionReads extends Reads[Election] {
      def reads(json: JsValue): JsResult[Election] = json match {
        case election: JsObject =>
          Try {
            val id = (election \ "_id" \ "$oid").asOpt[String]

            val name = (election \ "name").as[String]
            val description = (election \ "description").as[String]
            val creatorName = (election \ "creatorName").as[String]
            val creatorEmail = (election \ "creatorEmail").as[String]
            val start = (election \ "start").as[Date]
            val end = (election \ "end").as[Date]
            val realtimeResult = (election \ "realtimeResult").as[Boolean]
            val votingAlgo = (election \ "votingAlgo").as[String]
            val candidates = (election \ "candidates").as[List[String]]
            val ballotVisibility = (election \ "ballotVisibility").as[String]
            val voterListVisibility = (election \ "voterListVisibility").as[Boolean]
            val isInvite = (election \ "isInvite").as[Boolean]
            val isCompleted = (election \ "isCompleted").as[Boolean]
            val isStarted = (election \ "isStarted").as[Boolean]
            val createdTime = (election \ "createdTime").as[Date]
            val adminLink = (election \ "adminLink").as[String]
            val inviteCode = (election \ "inviteCode").as[String]
            val ballot = (election \ "ballot").as[List[Ballot]]
            val voterList = (election \ "voterList").as[List[Voter]]
            val winners = (election \ "winners").as[List[Winner]]
            val isCounted = (election \ "isCounted").as[Boolean]
            val noVacancies = (election \ "noVacancies").as[Int]

            JsSuccess(
              new Election(
                id,
                name,
                description,
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
                noVacancies
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
}
