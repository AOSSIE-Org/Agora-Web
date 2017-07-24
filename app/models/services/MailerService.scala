package models.services

import play.api.libs.mailer._
import java.io.File
import org.apache.commons.mail.EmailAttachment
import javax.inject.Inject

class MailerService @Inject() (mailerClient: MailerClient) {

  def sendEmail() = {
    val cid = "1234"
    val email = Email(
      "Simple email",
      "Mister FROM <from@email.com>",
      Seq("Miss TO <seena.thaana218@gmail.com>"),
      // adds attachment

      // sends text, HTML or both...
      bodyText = Some("A text message"),
      bodyHtml = Some(s"""<html><body><p>An <b>html</b> message with cid </p></body></html>""")
    )
    mailerClient.send(email)
    println("Done");
  }

}
