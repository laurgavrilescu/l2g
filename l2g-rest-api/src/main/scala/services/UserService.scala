package services

import model.User
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait UserService extends IdentityService[User] {
  def find(userName: String): Future[Option[User]]

  def findMates(userName: String): Future[Seq[User]]
  
  def activate(userName: String): Future[Option[String]]

  def save(user: User): Future[User]
}
