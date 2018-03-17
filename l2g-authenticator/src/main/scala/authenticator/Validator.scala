package authenticator

import scala.concurrent.{ExecutionContext, Future}

trait Validator {
  def isValid(authenticator: Authenticator)(implicit ec: ExecutionContext): Future[Boolean]
}
