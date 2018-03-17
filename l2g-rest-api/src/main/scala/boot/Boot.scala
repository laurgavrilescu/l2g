package boot

import java.io.File
import java.nio.file.{AccessDeniedException, FileSystemNotFoundException}

import akka.actor.ActorSystem
import api.WebApi
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

object Boot extends App {
  protected val logger = LoggerFactory.getLogger(getClass)

  def loadConfigFile(filePath: String): Config = {
    val configFile = new File(filePath)
    if (!configFile.exists()) {
      throw new FileSystemNotFoundException(s"Could not find configuration file $filePath")
    }

    if (!configFile.canRead) {
      throw new AccessDeniedException(s"File: $filePath is not readable")
    }

    ConfigFactory.parseFile(configFile)
  }

  def loadConfigFileWithFallbackResource(fileNameOpt: Option[String], defaultResource: String = "application.conf"): Config = {
    val defaultConfig = ConfigFactory.load(defaultResource)

    fileNameOpt.map(loadConfigFile(_).withFallback(defaultConfig)).getOrElse(defaultConfig)
  }

  try {
    val config = loadConfigFileWithFallbackResource(args.lift(0))

    logger.debug("Configuration: {}", config.root.render())
    implicit val actorSystem: ActorSystem = ActorSystem("web-rest-api")
    val webApi = new WebApi(config.resolve())
    webApi.start()

  } catch {
    case t: Throwable => {
      println(t.getMessage)
    }
  }
}
