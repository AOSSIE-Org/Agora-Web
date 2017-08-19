package models.services

import play.api.libs.mailer._
import java.io.File
import org.apache.commons.mail.EmailAttachment
import javax.inject.Inject
import models.User
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }


class MailerService @Inject() (mailerClient: MailerClient , val messagesApi: MessagesApi) {

  def sendPassCodeEmail(receiver: String , passCode : String , id : String) = {
    val link = s"https://agora-web-aossie.herokuapp.com/guest/vote/$id"
    val email = Email(
      "Simple email",
      "AGORA <aossie@gmail.com>",
      Seq("Voter TO <"+ receiver + ">") ,
      bodyText = Some("Invitation For Election"),
      bodyHtml = Some(s"""<html><body><p><b>Passcode :</b> $passCode  </p>
        <p> <b>Voting Link :</b> $link </p> <br /> <p> You have to enter above passcode in order to validate your identity before voting</p></body></html>""")
    )
    mailerClient.send(email)
  }

  def sendAdminLink(user: User , url : String , decodedEmail : String) = {
    val email = Email(
      subject = "Adminlink",
      from = "AGORA <aossie@gmail.com>",
      to = Seq(decodedEmail),
      bodyHtml = Some(s"<html><body><p>Hello user,</p><p>Please follow <a href=$url>this link</a> to confirm and activate your new account.</p></body></html>")
    )
    mailerClient.send(email)

  }

}
