package api


import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

import scala.concurrent.duration.{Duration, FiniteDuration}

class AppConfiguration(config: Config) {

  val host: String = config.getString("service.host")
  val port: Int = config.getInt("service.port")
  val secretKey: String = config.getString("service.secretKey")
  val tokenValidity: FiniteDuration = {
    val finite = Duration(config.getString("service.tokenValidity"))
    Some(finite).collect { case d: FiniteDuration => d }.getOrElse(FiniteDuration(1, TimeUnit.HOURS))
  }

  val authServiceName: String = config.getString("service.authServiceName")

  val mongoUri: String = config.getString("service.mongo.uri")
  val mongoDBName: String = config.getString("service.mongo.dbName")

}
