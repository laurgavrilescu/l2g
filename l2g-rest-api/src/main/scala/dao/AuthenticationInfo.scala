package dao

import model.AuthenticationEntity

case class AuthenticationInfo(password: String,
                              salt: Option[String] = None) extends AuthenticationEntity
