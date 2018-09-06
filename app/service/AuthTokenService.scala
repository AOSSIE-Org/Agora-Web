package service

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.AuthToken
import models.security.User
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait AuthTokenService {

  /**
    * Creates a new auth token and saves it in the backing store.
    *
    * @param loginInfo The user ID for which the token should be created.
    * @param expiry The duration a token expires.
    * @return The saved auth token.
    */
  def create(loginInfo: LoginInfo, expiry: FiniteDuration = 5 minutes): Future[AuthToken]


  /**
    * Validates a token ID.
    *
    * @param id The token ID to validate.
    * @return The token if it's valid, None otherwise.
    */
  def validate(id: String): Future[Option[AuthToken]]

  /**
    * Cleans expired tokens.
    *
    * @return The list of deleted tokens.
    */
  def clean: Future[Seq[AuthToken]]

}
