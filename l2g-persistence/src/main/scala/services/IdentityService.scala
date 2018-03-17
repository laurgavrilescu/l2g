package services

import model.{Identity, LoginEntity}

import scala.concurrent.Future

trait IdentityService[T <: Identity] {

  def retrieve(loginEntity: LoginEntity): Future[Option[T]]
}
