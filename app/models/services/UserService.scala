package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User

import scala.concurrent.Future

/**
 * Handles actions to users. use hashmap instead of database to show the functionality
 */
trait UserService extends IdentityService[User] {

  /**
   * Retrieves a user that matches the specified ID.
   */
  def retrieve(id: UUID): Future[Option[User]]

  /**
   * Saves a user.
   */
  def save(user: User): Future[User]

  /**
   * Saves the social profile for a user.
   */
  def save(profile: CommonSocialProfile): Future[User]
}
