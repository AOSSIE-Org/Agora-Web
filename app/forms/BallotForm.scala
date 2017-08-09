package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the voting process.
 */
object BallotForm {

  case class BallotData(id: String, ballotInput: String , passCode : String )

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "id"          -> nonEmptyText,
      "ballotinput" -> nonEmptyText,
      "passCode" -> nonEmptyText
    )(BallotData.apply)(BallotData.unapply)
  )
}
