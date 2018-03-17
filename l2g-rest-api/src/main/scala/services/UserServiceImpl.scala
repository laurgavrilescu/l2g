package services

import model.{LoginEntity, User}
import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.{Cursor, DefaultDB, ReadPreference}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl(val database: Future[DefaultDB])
                     (implicit ex: ExecutionContext) extends UserService {

  def users: Future[JSONCollection] = database.map(_.collection[JSONCollection]("user"))

  implicit lazy val format: OFormat[User] = Json.format[User]


  override def retrieve(loginEntity: LoginEntity): Future[Option[User]] =
    users.flatMap(_.find(Json.obj("name" -> loginEntity.uid)).one[User])


  private def add(user: User): Future[User] = {
    users.flatMap(_.insert(user)).flatMap {
      _ => Future.successful(user)
    }
  }

  private def update(user: User): Future[User] = {
    users.flatMap(_.update(Json.obj("name" -> user.name), user)).flatMap {
      _ => Future.successful(user)
    }
  }

  override def save(user: User): Future[User] =
    find(user.name).flatMap {
      case Some(_) => update(user)
      case None => add(user)
    }

  override def find(userName: String): Future[Option[User]] =
    users.flatMap(_.find(Json.obj("name" -> userName)).one[User])

  override def activate(userName: String): Future[Option[String]] = {
    find(userName).flatMap {
      case Some(user) =>
        val activated = user.copy(activated = true)
        users.flatMap(_.update(Json.obj("name" -> activated.name), activated)).flatMap {
          _ => Future.successful(Some(activated.name))
        }
      case None => Future.successful(None)
    }
  }

  override def findMates(userName: String): Future[Seq[User]] = {
    users.map(_.find(Json.obj("mateName" -> userName)).cursor[User](ReadPreference.primary)).flatMap {
      cursor => cursor.collect[List](1000, Cursor.FailOnError[List[User]]())
    }
  }
}