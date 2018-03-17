package api

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import com.typesafe.config.Config
import dao.AuthenticationInfoDAO
import http.HttpRoute
import org.apache.log4j.Logger
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import repository.SimpleAuthenticationRepository
import services.{AuthenticationService, SecretServiceImpl, UserServiceImpl}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import http.routes.{AuthRouter, SecretRouter, UserRouter}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import authenticator.exception.AuthenticatorException
import io.swagger.models.ExternalDocs


class WebApi(configuration: Config)(implicit system: ActorSystem)  { self =>
  lazy val apiTypes: Set[Class[_]] = Set(classOf[AuthRouter], classOf[UserRouter], classOf[SecretRouter])
  val logger: Logger = Logger.getLogger(this.getClass)

  def swaggerServiceRoutes: Route = new SwaggerHttpService {
    override val apiClasses: Set[Class[_]] = self.apiTypes
    override val host = "" //
    override val basePath = "/"    //the basePath for the API you are exposing
    override val externalDocs = Some(new ExternalDocs("Core Docs", "http://acme.com/docs"))
    override val apiDocsPath = "api-docs" //where you want the swagger-json endpoint exposed
    override val info = Info(
      "Two factor authentication service using akka-http and spray-swagger.",
      "1.0",
      "L2G API",
      "TOC Url") //provides license and other description details
  }.routes

  implicit def generalExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case t: AuthenticatorException =>
        println(s"Unauthorized: ${t.getMessage} of (${t.getClass.getName})")
        complete(HttpResponse(StatusCodes.Unauthorized, entity = t.getMessage))
      case t: Throwable =>
        extractUri { uri =>
          println(s"Request to $uri could not be handled normally: ${t.getMessage} of (${t.getClass.getName})")
          complete(HttpResponse(StatusCodes.InternalServerError, entity = t.getMessage))
        }
    }

  def swaggerUI: Route = path("swagger") { getFromResource("swagger/index.html") } ~
    getFromResourceDirectory("swagger")

  def start(): Unit = {
    logger.info("Starting web service.")


    implicit val ec: ExecutionContext = system.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    val appConfig = new AppConfiguration(configuration)

    val driver = MongoDriver()
    val parsedUri = MongoConnection.parseURI(appConfig.mongoUri)
    val connection: Try[MongoConnection] = parsedUri.map(driver.connection)

    val futureConnection = Future.fromTry(connection)
    val database: Future[DefaultDB] = futureConnection.flatMap(_.database(appConfig.mongoDBName))

    val usersService = new UserServiceImpl(database)
    val authRepository = new SimpleAuthenticationRepository(new AuthenticationInfoDAO(database))
    val authService = new AuthenticationService(appConfig.authServiceName, usersService, authRepository, appConfig.tokenValidity, appConfig.secretKey)
    val secretService = new SecretServiceImpl(database)
    val httpRoute = new HttpRoute(authService, usersService, secretService)

    val routes = httpRoute.route ~ swaggerServiceRoutes ~ swaggerUI
    Http().bindAndHandle(routes, appConfig.host, appConfig.port)
  }

}
