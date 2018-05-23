package service

import javax.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import formatters.json.UserData
import models.security.User
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends UserService {

  def users = reactiveMongoApi.database.map(_.collection[JSONCollection]("user"))

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    users.flatMap(_.find(Json.obj("username" -> loginInfo.providerKey)).one[User])

  override def save(user: User): Future[WriteResult] =
    users.flatMap(_.insert(user))

  override def update(userData: UserData, loginInfo: LoginInfo): Future[Boolean] = {
    val query = Json.obj("username" -> loginInfo.providerKey)
    val modifier = Json.obj("firstName" -> userData.firstName, "lastName" -> userData.lastName, "avatarURL" -> userData.avatarURL)
    users.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
  }
}