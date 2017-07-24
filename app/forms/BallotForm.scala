package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the voting process.
 */
object BallotForm {

  case class BallotData(id: String, ballotInput: String, email: String, code: String)

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "id"          -> text,
      "ballotinput" -> nonEmptyText,
      "email"       -> text,
      "code"        -> text
    )(BallotData.apply)(BallotData.unapply)
  )
}
