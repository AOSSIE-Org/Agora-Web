package models.services

import play.api.libs.mailer._
import java.io.File
import org.apache.commons.mail.EmailAttachment
import javax.inject.Inject

class MailerService @Inject() (mailerClient: MailerClient) {

  def sendEmail(email: String , passCode : String) = {
    val emailSend = Email(
      "Simple email",
      "AGORA <aossie@gmail.com>",
      Seq("Voter TO <"+ email + ">") ,
      // adds attachment

      // sends text, HTML or both...
      bodyText = Some("Invitation For Election"),
      bodyHtml = Some(s"""<html><body><p><b>Passcode :</b> $passCode  </p> <br /> <p> You have to enter above passcode in order to validate your identity before voting</p></body></html>""")
    )
    mailerClient.send(emailSend)
    println(emailSend);
  }

}
