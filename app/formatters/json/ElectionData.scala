package formatters.json

import java.util.Date

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import models.Ballot
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JodaReads, JodaWrites, JsPath}

@ApiModel(description = "Data required to create an election", value = "ElectionData")
case class ElectionData(
   @ApiModelProperty(value = "Election name", required = true)name: String,
   @ApiModelProperty(value = "Election description", required = true)description: String,
   @ApiModelProperty(value = "Election type", required = true)electionType: String,
   @ApiModelProperty(value = "Election candidates", required = true)candidates: List[String],
   @ApiModelProperty(value = "Election ballot visibility status", required = true)ballotVisibility: String,
   @ApiModelProperty(value = "Voters list should be made visible?", required = true)voterListVisibility:  Boolean,
   @ApiModelProperty(value = "Election start date", required = true)startingDate: DateTime,
   @ApiModelProperty(value = "Election end date", required = true)endingDate: DateTime,
   @ApiModelProperty(value = "Invite voters?", required = true)isInvite: Boolean,
   @ApiModelProperty(value = "Get real time result?", required = true)isRealTime: Boolean,
   @ApiModelProperty(value = "Voting algorithm", required = true)votingAlgo: String,
   @ApiModelProperty(value = "Number of vacancies", required = true)noVacancies : Int,
   @ApiModelProperty(value = "Ballots", required = false)ballot: List[Ballot])

object ElectionData {
  implicit val jodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit val electionDataFormat : Format[ElectionData] = (
      (JsPath \ "name").format[String] and
      (JsPath \ "description").format[String] and
      (JsPath \ "electionType").format[String] and
      (JsPath \ "candidates").format[List[String]] and
      (JsPath \ "ballotVisibility").format[String] and
      (JsPath \ "voterListVisibility").format[Boolean] and
      (JsPath \ "startingDate").format[DateTime] and
      (JsPath \ "endingDate").format[DateTime] and
      (JsPath \ "isInvite").format[Boolean] and
      (JsPath \ "isRealTime").format[Boolean] and
      (JsPath \ "votingAlgo").format[String] and
      (JsPath \ "noVacancies").format[Int] and
      (JsPath \ "ballot").format[List[Ballot]]
    )(ElectionData.apply, unlift(ElectionData.unapply))

}