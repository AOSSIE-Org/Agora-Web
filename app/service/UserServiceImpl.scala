package service

import com.mohiva.play.silhouette.api.LoginInfo
import models.security.User
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends UserService {
  import utils.BSONUtils.loginInfoHandler
  def users = reactiveMongoApi.database.map(_.collection[BSONCollection]("user"))

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    users.flatMap(_.find(BSONDocument("loginInfo" -> loginInfo)).one[User])

  override def checkEmail(email : String): Future[Option[User]] =
    users.flatMap(_.find(BSONDocument("email" -> email)).one[User])

  override def save(user: User): Future[WriteResult] =
    users.flatMap(_.insert.one(user))

  override def update(userData: User, loginInfo: LoginInfo): Future[Boolean] = {
    val query = BSONDocument("loginInfo" -> loginInfo)
    users.flatMap(_.update.one(query, userData)).flatMap(_ => Future.successful(true))

  }
}
