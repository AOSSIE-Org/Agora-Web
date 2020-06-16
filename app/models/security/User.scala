package models.security

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import formatters.json.UserData
import play.api.libs.json.{Json, _}
import models.Question

import scala.util.{Failure, Success, Try}

case class User(id: Option[String], loginInfo: LoginInfo, username: String, email: String,
                firstName: String, lastName: String, avatarURL: Option[String], twoFactorAuthentication: Boolean, securityQuestion: Question, activated: Boolean) extends Identity {
  def extractUserData : UserData = UserData(username, email, firstName, lastName, avatarURL, twoFactorAuthentication, None, None, None)
}

object User {

  implicit val reader = Json.reads[User]
  implicit val writer = Json.writes[User]

  implicit val loginInfoReader = Json.reads[LoginInfo]
  implicit val loginInfoWriter = Json.writes[LoginInfo]

  implicit object UserWrites extends OWrites[User] {
    def writes(user: User): JsObject =
      user.id match {
        case Some(id) =>
          Json.obj(
            "_id" -> user.id,
            "loginInfo" -> Json.obj(
              "providerID" -> user.loginInfo.providerID,
              "providerKey" -> user.loginInfo.providerKey
            ),
            "username" -> user.username,
            "email" -> user.email,
            "firstName" -> user.firstName,
            "lastName" -> user.lastName,
            "avatarURL" -> user.avatarURL,
            "twoFactorAuthentication" -> user.twoFactorAuthentication,
            "activated" -> user.activated,
            "securityQuestion" -> user.securityQuestion
          )
        case _ =>
          Json.obj(
            "loginInfo" -> Json.obj(
              "providerID" -> user.loginInfo.providerID,
              "providerKey" -> user.loginInfo.providerKey
            ),
            "username" -> user.username,
            "email" -> user.email,
            "firstName" -> user.firstName,
            "lastName" -> user.lastName,
            "avatarURL" -> user.avatarURL,
            "twoFactorAuthentication" -> user.twoFactorAuthentication,
            "activated" -> user.activated,
            "securityQuestion" -> user.securityQuestion
          )
      }

    implicit object UserReads extends Reads[User] {
      def reads(json: JsValue): JsResult[User] = json match {
        case user: JsObject =>
          Try {
            val id = (user \ "_id" \ "$oid").asOpt[String]

            val providerId = (user \ "loginInfo" \ "providerID").as[String]
            val providerKey = (user \ "loginInfo" \ "providerKey").as[String]

            val username = (user \ "userName").as[String]
            val email = (user \ "email").as[String]
            val firstName = (user \ "firstName").as[String]
            val lastName = (user \ "lastName").as[String]
            val avatarURL = (user \ "avatarURL").asOpt[String]
            val twoFactorAuthentication = (user \ "twoFactorAuthentication").as[Boolean]
            val activated = (user \ "activated").as[Boolean]
            val securityQuestion = (user \ "securityQuestion").as[Question]

            JsSuccess(
              new User(
                id,
                new LoginInfo(providerId, providerKey),
                username,
                email,
                firstName,
                lastName,
                avatarURL,
                twoFactorAuthentication,
                securityQuestion,
                activated
              )
            )
          } match {
            case Success(value) => value
            case Failure(cause) => JsError(cause.getMessage)
          }
        case _ => JsError("expected.jsobject")
      }
    }

  }

}
