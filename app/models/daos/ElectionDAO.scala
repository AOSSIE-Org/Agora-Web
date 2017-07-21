package models.daos

import models.Election

import scala.concurrent.Future

/**
 * Give access to the Election object.
 */
trait ElectionDAO {

  /**
   * Saves a Election.
   *
   * @param election The election to save.
   * @return The saved election.
   */
  def save(election: Election): Future[Election]
}
