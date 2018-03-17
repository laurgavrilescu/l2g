package authenticator

import model.{Identity, LoginEntity}

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationPipeline[S, I <: Identity] extends From[S, Future[State[I]]] {
  self =>

  val validators: Set[Validator] = Set()

  val identityReader: LoginEntity => Future[Option[I]]

  def toState(authenticator: Authenticator)(implicit ec: ExecutionContext): Future[State[I]]

  final implicit class AuthenticatorFuture(value: Future[Authenticator]) {
    def toState(implicit ec: ExecutionContext): Future[State[I]] =
      self.toState(value, self.toState)
  }

  protected def toState[T](future: Future[T], transformer: T => Future[State[I]])
                          (implicit ec: ExecutionContext): Future[State[I]] =
    future.flatMap(transformer).recover { case e: Throwable => new Failure[I](e) }
}