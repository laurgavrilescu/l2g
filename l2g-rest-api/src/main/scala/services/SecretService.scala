package services

import model.Secret

import scala.concurrent.Future

trait SecretService {

  def find(secretId: String): Future[Option[Secret]]

  def add(secret: Secret): Future[Secret]

  def addUser(secretId: String, userName: String): Future[Option[Secret]]

  def view(userName: String): Future[Seq[Secret]]

  def viewAsAllowed(userName: String): Future[Seq[Secret]]

  def update(secret: Secret): Future[Secret]

  def save(secret: Secret): Future[Secret]

  def remove(secret: Secret): Future[Unit]

}
