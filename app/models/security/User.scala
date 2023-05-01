package models.security

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import formatters.json.UserData
import play.api.libs.json.{Json, _}
import models.{Election, Question}
import reactivemongo.api.bson.BSONDocumentHandler
import utils.BSONUtils.JsonToBSONDocumentHandler

import scala.util.{Failure, Success, Try}

case class User(id: Option[String], loginInfo: LoginInfo, username: String, email: String,
                firstName: String, lastName: String, avatarURL: Option[String], twoFactorAuthentication: Boolean, securityQuestion: Question, activated: Boolean) extends Identity {
  def extractUserData : UserData = UserData(username, email, firstName, lastName, avatarURL, twoFactorAuthentication, None, None, None)
}

object User {

  implicit val loginInfoReader: Reads[LoginInfo] = Json.reads[LoginInfo]
  implicit val loginInfoWriter: OWrites[LoginInfo] = Json.writes[LoginInfo]

  implicit val reads: Reads[User] = Json.reads[User]
  implicit val writes: OWrites[User] = Json.writes[User]


  implicit val handler: BSONDocumentHandler[User] = JsonToBSONDocumentHandler(writes, reads)

}
