package models.services

import play.api.libs.mailer._
import java.io.File
import org.apache.commons.mail.EmailAttachment
import javax.inject.Inject
import models.User
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }


class MailerService @Inject() (mailerClient: MailerClient , val messagesApi: MessagesApi) {

  def sendPassCodeEmail(receiver: String ,receiverName: String, creatorName: String, creatorEmail: String , electionName: String, linl: String, electionDescription: String, passCode : String , id : String) = {
    val link = s"http://agoravote.org/guest/vote/$id"
    val email = Email(
      "Simple email",
      "AGORA <aossie@gmail.com>",
      Seq("Voter TO <"+ receiver + ">") ,
      bodyText = Some("Invitation For Election"),
      bodyHtml = Some(s"<html><body><p>Hello $receiverName,</p><p>$creatorName $creatorEmail is inviting you to vote in the following election:</p><p>$electionName</p><p>$electionDescription</p><p>To vote, click on the link below and type the passcode when prompted:</p><p>Voting Link: <a href=$link>$link</a></p><p>Passcode: $passCode</p><p>Kind regards,The Agora Team </p></body></html>")
    )
    mailerClient.send(email)
  }

  def sendAdminLink(user: User , url : String , decodedEmail : String) = {
    val email = Email(
      subject = "Adminlink",
      from = "AGORA <aossie@gmail.com>",
      to = Seq(decodedEmail),
      bodyHtml = Some(s"<html><body><p>Hello user,</p><p>This is the <a href=$url>admin link</a> for your election.</p></body></html>")
    )
    mailerClient.send(email)

  }

}
