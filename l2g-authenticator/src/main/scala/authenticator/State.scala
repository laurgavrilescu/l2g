package authenticator

import model.Identity

trait State[I <: Identity]

class StateException(msg: String, cause: Throwable = null)
  extends Exception(msg, cause)

class Failure[I <: Identity](val cause: Throwable) extends State[I] {
  def this(s: String) = this(new StateException(s))
}
