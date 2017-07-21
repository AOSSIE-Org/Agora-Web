package forms

import play.api.data.Form
import play.api.data.Forms._
import models.Election
/**
  * The form which handles the create election process.
  */
object ElectionForm {

  /**
    * A play framework form.
    */
  val form = Form(
    tuple(
      "Name" -> nonEmptyText, "Description" -> nonEmptyText, "Creator name" -> nonEmptyText,
      "Creator email" -> email, "Starting Date" -> date, "Ending Date" -> date, "Realtime Result" -> boolean,
      "Voting Algo" -> nonEmptyText, "Candidates" -> nonEmptyText, "Voting Preference" -> boolean,
      "Invite Voters" -> boolean
    )
  )


}
