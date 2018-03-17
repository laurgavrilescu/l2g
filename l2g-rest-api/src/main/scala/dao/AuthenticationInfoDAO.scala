package dao

import model.LoginEntity
import play.api.libs.json.{JsObject, Json, OFormat}
import reactivemongo.api.DefaultDB
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationInfoDAO(val database: Future[DefaultDB])
                           (implicit ex: ExecutionContext) extends AuthenticationDAO[AuthenticationInfo] {

  def authData: Future[JSONCollection] = database.map(_.collection[JSONCollection]("auth"))

  implicit lazy val format: OFormat[AuthenticationInfo] = Json.format[AuthenticationInfo]

  override def find(loginEntity: LoginEntity): Future[Option[AuthenticationInfo]] =
    authData.flatMap(_.find(Json.obj("loginEntityId" -> loginEntity.uid)).one[AuthenticationInfo])

  override def add(loginEntity: LoginEntity, authInfo: AuthenticationInfo): Future[AuthenticationInfo] = {
    val passwordBuilder = Json.toJson(authInfo).as[JsObject] ++ Json.obj("loginEntityId" -> Some(loginEntity.uid))
    authData.flatMap(_.insert(passwordBuilder)).flatMap {
      _ => Future.successful(authInfo)
    }
  }

  override def update(loginEntity: LoginEntity, authInfo: AuthenticationInfo): Future[AuthenticationInfo] = {
    val passwordBuilder = Json.toJson(authInfo).as[JsObject] ++ Json.obj("loginEntityId" -> Some(loginEntity.uid))
    authData.flatMap(_.update(Json.obj("loginEntityId" -> loginEntity.uid), passwordBuilder)).flatMap {
      _ => Future.successful(authInfo)
    }
  }

  override def save(loginEntity: LoginEntity, authInfo: AuthenticationInfo): Future[AuthenticationInfo] = {
    find(loginEntity).flatMap {
      case Some(_) => update(loginEntity, authInfo)
      case None => add(loginEntity, authInfo)
    }
  }

  override def remove(loginEntity: LoginEntity): Future[Unit] = {
    authData.flatMap(_.remove(Json.obj("loginEntityId" -> loginEntity.uid))).flatMap(_ => Future.successful(()))
  }
}
