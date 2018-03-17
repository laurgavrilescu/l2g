package authenticator

import org.joda.time.DateTime
import model.LoginEntity
import scala.concurrent.duration.FiniteDuration

trait Authenticator {
  def loginEntity: LoginEntity

  def isValid: Boolean
}

trait ExpirableAuthenticator extends Authenticator {
  val lastUsedDateTime: DateTime

  val expirationDateTime: DateTime

  val idleTimeout: Option[FiniteDuration]

  override def isValid: Boolean = !isExpired && !isTimedOut

  def isExpired: Boolean = expirationDateTime.isBeforeNow

  def isTimedOut: Boolean = idleTimeout.isDefined && lastUsedDateTime.plusSeconds(idleTimeout.get.toSeconds.toInt).isBeforeNow
}


case class TokenAuthenticator(loginEntity: LoginEntity,
                            lastUsedDateTime: DateTime,
                            expirationDateTime: DateTime,
                            idleTimeout: Option[FiniteDuration])
  extends ExpirableAuthenticator
