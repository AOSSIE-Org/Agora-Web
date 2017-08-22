package forms

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the create election process.
 */
object EditElectionForm {

  case class ElectionData(
    id : String,
    name: String,
    description: String,
    creatorName: String,
    creatorEmail: String,
    candidates: String,
    ballotVisibility: String,
    voterListVisibility:  Boolean,
    startingDate: Date,
    endingDate: Date,
    isInvite: Boolean,
    isRealTime: Boolean,
    votingAlgo: String,
    noVacancies: Int
  )

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "id"                  -> nonEmptyText,
      "name"                -> nonEmptyText,
      "description"         -> nonEmptyText,
      "creatorName"         -> nonEmptyText,
      "creatorEmail"        -> email,
      "candidates"          -> nonEmptyText,
      "ballotVisibility"    -> nonEmptyText,
      "voterListVisibility" -> boolean,
      "start"               -> date("MM/dd/yyyy"),
      "end"                 -> date("MM/dd/yyyy"),
      "isInvite"            -> boolean,
      "realtimeResult"      -> boolean,
      "votingAlgo"          -> nonEmptyText,
      "noVacancies"         -> number
    )(ElectionData.apply)(ElectionData.unapply)
  )
}
