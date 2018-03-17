package repository

import dao.AuthenticationDAO
import model.{AuthenticationEntity, LoginEntity}

import scala.concurrent.Future
import scala.reflect.ClassTag

class SimpleAuthenticationRepository[T <: AuthenticationEntity: ClassTag](dao: AuthenticationDAO[T]) extends AuthenticationRepository[T] {

  def find(loginEntity: LoginEntity)(implicit tag: ClassTag[T]): Future[Option[T]] = {
    dao.find(loginEntity)
  }

  def add(loginEntity: LoginEntity, AuthenticationEntity: T): Future[T] = {
    dao.add(loginEntity, AuthenticationEntity)
  }

  def update(loginEntity: LoginEntity, AuthenticationEntity: T): Future[T] = {
    dao.update(loginEntity, AuthenticationEntity)
  }

  def save(loginEntity: LoginEntity, AuthenticationEntity: T): Future[T] = {
    dao.save(loginEntity, AuthenticationEntity)
  }

  def remove(loginEntity: LoginEntity)(implicit tag: ClassTag[T]): Future[Unit] = {
    dao.remove(loginEntity)
  }

}
