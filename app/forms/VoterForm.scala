package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the add voter.
 */
object VoterForm {

  case class VoterData(id: String, email: String)

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "id"          -> text,
      "email"       ->  nonEmptyText
    )(VoterData.apply)(VoterData.unapply)
  )
}
