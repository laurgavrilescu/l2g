package security

object ActivationCode {
  def valid(code: String): Boolean = {
    code.length == 10
  }
}
