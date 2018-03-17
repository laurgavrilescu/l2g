package repository

import model.{AuthenticationEntity, LoginEntity}

import scala.concurrent.Future
import scala.reflect.ClassTag

trait AuthenticationRepository[T <: AuthenticationEntity] {
  
  def find(loginEntity: LoginEntity)(implicit tag: ClassTag[T]): Future[Option[T]]
  
  def add(loginEntity: LoginEntity, AuthenticationEntity: T): Future[T]
  
  def update(loginEntity: LoginEntity, AuthenticationEntity: T): Future[T]
  
  def save(loginEntity: LoginEntity, AuthenticationEntity: T): Future[T]
  
  def remove(loginEntity: LoginEntity)(implicit tag: ClassTag[T]): Future[Unit]
}
