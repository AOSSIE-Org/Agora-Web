package forms

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the create election process.
 */
object ElectionForm {

  case class ElectionData(
    name: String,
    description: String,
    creatorName: String,
    creatorEmail: String,
    candidates: String,
    isPublic: Boolean,
    startingDate: Date,
    endingDate: Date,
    isInvite: Boolean,
    isRealTime: Boolean,
    votingAlgo: String
  )

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "name"             -> nonEmptyText,
      "description"      -> nonEmptyText,
      "creatorName"      -> nonEmptyText,
      "creatorEmail"     -> email,
      "candidates"       -> nonEmptyText,
      "isPublic"         -> boolean,
      "start"            -> date("MM/dd/yyyy"),
      "end"              -> date("MM/dd/yyyy"),
      "isInvite"         -> boolean,
      "realtimeResult"   -> boolean,
      "votingAlgo"       -> nonEmptyText
    )(ElectionData.apply)(ElectionData.unapply)
  )
}
