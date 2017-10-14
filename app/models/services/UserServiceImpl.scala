package models.services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User
import models.daos.UserDAO
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
class UserServiceImpl @Inject()(userDAO: UserDAO) extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   */
  def retrieve(id: UUID): Future[Option[User]] = userDAO.find(id)

  /**
   * Retrieves a user that matches the specified login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
   * Saves a user.
   */
  def save(user: User): Future[User] = userDAO.save(user)

  /**
   * Saves the social profile for a user.
   */
  def save(profile: CommonSocialProfile): Future[User] =
    userDAO.find(profile.loginInfo).flatMap {
      case Some(user) => // Update user with profile
        userDAO.save(
          user.copy(
            firstName = profile.firstName,
            lastName = profile.lastName,
            fullName = profile.fullName,
            email = profile.email,
            avatarURL = profile.avatarURL
          )
        )
      case None => // Insert a new user
        userDAO.save(
          User(
            userID = UUID.randomUUID(),
            loginInfo = profile.loginInfo,
            firstName = profile.firstName,
            lastName = profile.lastName,
            fullName = profile.fullName,
            email = profile.email,
            avatarURL = profile.avatarURL
          )
        )
    }
}
