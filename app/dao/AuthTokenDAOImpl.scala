package dao

import java.util.UUID

import javax.inject.Inject
import models.AuthToken
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class AuthTokenDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends  AuthTokenDAO {

  /**
    * The data store for the Auth tokens.
    */
  private def authTokenCollection = reactiveMongoApi.database.map(_.collection[JSONCollection]("authTokens"))

  /**
    * Finds a token by its ID.
    *
    * @param id The unique token ID.
    * @return The found token or None if no token for the given ID could be found.
    */
   def find(id: String) = authTokenCollection.flatMap(_.find(Json.obj("tokenId" -> id))
     .one[AuthToken])

  /**
    * Finds expired tokens.
    *
    * @param dateTime The current date time.
    */
   def findExpired(dateTime: DateTime) =
     authTokenCollection.flatMap(_.find(Json.obj()).cursor[AuthToken]().collect[Seq]())
       .flatMap(tokens => Future.successful(tokens.filter(token => token.expiry.isBefore(dateTime))))

  /**
    * Saves a token.
    *
    * @param token The token to save.
    * @return The saved token.
    */
   def save(token: AuthToken) = {
    authTokenCollection.flatMap(_.insert(token)).flatMap(_ => Future.successful(token))
  }

  /**
    * Removes the token for the given ID.
    *
    * @param id The ID for which the token should be removed.
    * @return A future to wait for the process to be completed.
    */
   def remove(id: String) = {
     authTokenCollection.flatMap(_.remove(Json.obj("tokenId" -> id))).flatMap(_ => Future.successful(()))
  }
}

