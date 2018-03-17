package authenticator.validator

import authenticator.{Authenticator, Validator}

import scala.concurrent.{ExecutionContext, Future}

object ExpirationValidator extends Validator {

  override def isValid(authenticator: Authenticator)(implicit ec: ExecutionContext): Future[Boolean] = Future.successful {
    authenticator.isValid
  }
}