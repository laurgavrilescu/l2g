package authenticator.exception

class AuthenticatorException(msg: String, cause: Option[Throwable] = None)
  extends Exception(msg, cause.orNull)
