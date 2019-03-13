package service

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import formatters.json.UserData
import models.security.User
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait UserService extends IdentityService[User] {

  def save(user: User): Future[WriteResult]

  def checkEmail(email : String): Future[Option[User]]
  
  def update(userData: User, loginInfo: LoginInfo) : Future[Boolean]
}
