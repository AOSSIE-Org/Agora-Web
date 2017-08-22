package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the election delete process.
 */
object DeleteForm {

  case class DeleteData(id: String)

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "id"          -> nonEmptyText
    )(DeleteData.apply)(DeleteData.unapply)
  )
}
