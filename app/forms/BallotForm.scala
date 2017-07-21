package forms

import play.api.data.Form
import play.api.data.Forms._
import models.Election
/**
  * The form which handles the voting process.
  */
object BallotForm {

  /**
    * A play framework form.
    */
  val form = Form(
    tuple(
      "ballotinput" -> nonEmptyText, "email" -> text, "code" -> text
    )
  )


}
