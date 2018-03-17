package dao

import model.{AuthenticationEntity, LoginEntity}

import scala.concurrent.Future

trait AuthenticationDAO[T <: AuthenticationEntity] {
  def find(loginEntity: LoginEntity): Future[Option[T]]

  def add(loginEntity: LoginEntity, authInfo: T): Future[T]

  def update(loginEntity: LoginEntity, authInfo: T): Future[T]

  def save(loginEntity: LoginEntity, authInfo: T): Future[T]

  def remove(loginEntity: LoginEntity): Future[Unit]
}

