package services

import model.Secret
import play.api.libs.json.{Json, OFormat}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor.Fail
import reactivemongo.api.{Cursor, DefaultDB, ReadPreference}

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._


class SecretServiceImpl(val database: Future[DefaultDB])
(implicit ex: ExecutionContext) extends SecretService {

  implicit lazy val format: OFormat[Secret] = Json.format[Secret]

  def secrets: Future[JSONCollection] = database.map(_.collection[JSONCollection]("secrets"))

  override def find(secretText: String): Future[Option[Secret]] = {
    secrets.flatMap(_.find(Json.obj("text" -> secretText)).one[Secret])
  }

  override def add(secret: Secret): Future[Secret] = {
    secrets.flatMap(_.insert(secret)).flatMap {
      _ => Future.successful(secret)
    }
  }

  override def update(secret: Secret): Future[Secret] = {
    secrets.flatMap(_.update(Json.obj("text" -> secret.text), secret)).flatMap {
      _ => Future.successful(secret)
    }
  }

  override def save(secret: Secret): Future[Secret] = {
    find(secret.text).flatMap {
      case Some(_) => update(secret)
      case None => add(secret)
    }
  }

  override def remove(secret: Secret): Future[Unit] = {
    secrets.flatMap(_.remove(Json.obj("text" -> secret.text))).flatMap(_ => Future.successful(()))
  }

  override def view(userName: String): Future[Seq[Secret]] = {
    secrets.map(_.find(Json.obj("userName" -> userName)).cursor[Secret](ReadPreference.primary)).flatMap {
      cursor => cursor.collect[List](1000, Cursor.FailOnError[List[Secret]]())
    }
  }

  override def viewAsAllowed(userName: String): Future[Seq[Secret]] = {
    secrets.map(_.find(Json.obj("allowedUsers" -> userName)).cursor[Secret](ReadPreference.primary)).flatMap {
      cursor => cursor.collect[List](1000, Cursor.FailOnError[List[Secret]]())
    }
  }

  override def addUser(secretId: String, userName: String): Future[Option[Secret]] = {
    find(secretId) flatMap {
      case None => Future.successful(None)
      case Some(secret) =>
        val allowed = secret.allowedUsers + userName
        val updatedSecret = secret.copy(allowedUsers = allowed)
        update(updatedSecret).map(Some(_))
    }
  }
}
