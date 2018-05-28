package dao

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}

class OAuth2InfoDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends DelegableAuthInfoDAO[OAuth2Info] {

  /**
    * The data store for the auth info.
    */
  def oAuth2Data = reactiveMongoApi.database.map(_.collection[JSONCollection]("OAuth2Info"))

  implicit lazy val format = Json.format[OAuth2Info]

  override def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] =
    oAuth2Data.flatMap(_.find(Json.obj("loginInfoId" -> loginInfo.providerKey)).one[OAuth2Info])

  override def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    val builder = Json.toJson(authInfo).as[JsObject] ++ Json.obj("loginInfoId" -> Some(loginInfo.providerKey))
    oAuth2Data.flatMap(_.insert(builder)).flatMap {
      _ => Future.successful(authInfo)
    }
  }

  override def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    val builder = Json.toJson(authInfo).as[JsObject] ++ Json.obj("loginInfoId" -> Some(loginInfo.providerKey))
    oAuth2Data.flatMap(_.update(Json.obj("loginInfoId" -> loginInfo.providerKey), builder)).flatMap {
      _ => Future.successful(authInfo)
    }
  }

  override def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    oAuth2Data.flatMap(_.remove(Json.obj("loginInfoId" -> loginInfo.providerKey))).flatMap(_ => Future.successful(()))
  }
}
