package dao

import javax.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.{BSON, BSONDocument, BSONDocumentHandler}
import reactivemongo.api.bson.collection.BSONCollection
import utils.BSONUtils.OFormatToBSONDocumentHandler

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
  * An implementation of the auth info DAO which stores the data in database.
  */
class PasswordInfoDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext, val classTag: ClassTag[PasswordInfo]) extends DelegableAuthInfoDAO[PasswordInfo] {

  /**
    * The data store for the auth info.
    */
  def passwords = reactiveMongoApi.database.map(_.collection[BSONCollection]("PasswordInfo"))

  implicit lazy val format = Json.format[PasswordInfo]

  private implicit val handler: BSONDocumentHandler[PasswordInfo] = OFormatToBSONDocumentHandler(Json.format[PasswordInfo])

  /**
    * Finds the auth info which is linked with the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
    */
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
    passwords.flatMap(_.find(BSONDocument("loginInfoId" -> loginInfo.providerKey)).one[PasswordInfo])

  /**
    * Adds new auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be added.
    * @param authInfo  The auth info to add.
    * @return The added auth info.
    */
  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val passwordBuilder = BSON.writeDocument(authInfo).get ++ BSONDocument("loginInfoId" -> Some(loginInfo.providerKey))
    passwords.flatMap(_.insert.one(passwordBuilder)).flatMap {
      _ => Future.successful(authInfo)
    }
  }

  /**
    * Updates the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be updated.
    * @param authInfo  The auth info to update.
    * @return The updated auth info.
    */
  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val passwordBuilder = BSON.writeDocument(authInfo).get ++ BSONDocument("loginInfoId" -> Some(loginInfo.providerKey))
    passwords.flatMap(_.update.one(BSONDocument("loginInfoId" -> loginInfo.providerKey), passwordBuilder)).flatMap {
      _ => Future.successful(authInfo)
    }
  }

  /**
    * Saves the auth info for the given login info.
    *
    * This method either adds the auth info if it doesn't exists or it updates the auth info
    * if it already exists.
    *
    * @param loginInfo The login info for which the auth info should be saved.
    * @param authInfo  The auth info to save.
    * @return The saved auth info.
    */
  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  /**
    * Removes the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be removed.
    * @return A future to wait for the process to be completed.
    */
  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    passwords.flatMap(_.delete.one(BSONDocument("loginInfoId" -> loginInfo.providerKey))).flatMap(_ => Future.successful(()))
  }
}
