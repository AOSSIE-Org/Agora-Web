package repository

import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import formatters.json.JWTAuthenticatorFormat._
import play.api.libs.json.Writes._
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import scala.language.postfixOps

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class RefreshTokenAuthenticatorRepositoryImpl @Inject()(reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends RefreshTokenAuthenticatorRepository[JWTAuthenticator] {

  final val maxDuration = 720 hours

  /**
   * The data store for the password info.
   */
  def refreshTokenAuthRepository = reactiveMongoApi.database.map(_.collection[JSONCollection]("refreshTokenAuthRepository"))

  /**
   * Finds the authenticator for the given ID.
   *
   * @param id The authenticator ID.
   * @return The found authenticator or None if no authenticator could be found for the given ID.
   */
  override def find(id: String): Future[Option[JWTAuthenticator]] = {
    refreshTokenAuthRepository.flatMap(_.find(Json.obj("_id" -> id)).one[JWTAuthenticator])
  }

  /**
   * Adds a new authenticator.
   *
   * @param authenticator The authenticator to add.
   * @return The added authenticator.
   */
  override def add(authenticator: JWTAuthenticator): Future[JWTAuthenticator] = {
    val passInfo = Json.obj("_id" -> authenticator.id, "authenticator" -> authenticator, "duration" -> maxDuration)
    refreshTokenAuthRepository.flatMap(_.insert(passInfo)).flatMap(_ => Future(authenticator))
  }

  /**
   * Updates an already existing authenticator.
   *
   * @param authenticator The authenticator to update.
   * @return The updated authenticator.
   */
  override def update(authenticator: JWTAuthenticator) = {
    val passInfo = Json.obj("_id" -> authenticator.id, "authenticator" -> authenticator, "duration" -> maxDuration)
    refreshTokenAuthRepository.flatMap(_.update(Json.obj("_id" -> authenticator.id), passInfo)).flatMap(_ => Future(authenticator))
  }

  /**
   * Removes the authenticator for the given ID.
   *
   * @param id The authenticator ID.
   * @return An empty future.
   */
  override def remove(id: String): Future[Unit] =
    refreshTokenAuthRepository.flatMap(_.remove(Json.obj("_id" -> id))).flatMap(_ => Future.successful(()))
}