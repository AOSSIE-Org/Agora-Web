package repository

import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import formatters.json.JWTAuthenticatorFormat._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection

import javax.inject.Inject
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Implementation of the authenticator repository which uses the database layer to persist the authenticator.
  */
class AuthenticatorRepositoryImpl @Inject()(reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends AuthenticatorRepository[JWTAuthenticator] {
  import utils.BSONUtils.FiniteDurationBsonFormat
  final val maxDuration = 12 hours

  /**
    * The data store for the password info.
    */
  def authRepository = reactiveMongoApi.database.map(_.collection[BSONCollection]("authRepository"))

  /**
   * Finds the authenticator for the given ID.
   *
   * @param id The authenticator ID.
   * @return The found authenticator or None if no authenticator could be found for the given ID.
   */
  override def find(id: String): Future[Option[JWTAuthenticator]] = {
    authRepository.flatMap(_.find(BSONDocument("id" -> id)).one[JWTAuthenticator])
  }

  /**
   * Adds a new authenticator.
   *
   * @param authenticator The authenticator to add.
   * @return The added authenticator.
   */
  override def add(authenticator: JWTAuthenticator): Future[JWTAuthenticator] = {
    val passInfo = BSONDocument("id" -> authenticator.id, "authenticator" -> authenticator, "duration" -> maxDuration)
    authRepository.flatMap(_.insert.one(passInfo)).flatMap(_ => Future(authenticator))
  }

  /**
   * Updates an already existing authenticator.
   *
   * @param authenticator The authenticator to update.
   * @return The updated authenticator.
   */
  override def update(authenticator: JWTAuthenticator) = {
    val passInfo = BSONDocument("id" -> authenticator.id, "authenticator" -> authenticator, "duration" -> maxDuration)
    authRepository.flatMap(_.update.one(BSONDocument("id" -> authenticator.id), BSONDocument("$set" -> passInfo))).flatMap(_ => Future(authenticator))
  }

  /**
   * Removes the authenticator for the given ID.
   *
   * @param id The authenticator ID.
   * @return An empty future.
   */
  override def remove(id: String): Future[Unit] =
    authRepository.flatMap(_.delete.one(BSONDocument("id" -> id))).flatMap(_ => Future.successful(()))
}