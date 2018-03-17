package http

import javax.ws.rs.Path

import services.{AuthenticationService, SecretService, UserService}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import http.routes.{AuthRouter, SecretRouter, UserRouter}
import io.swagger.annotations.Api

import scala.concurrent.ExecutionContext

class HttpRoute(authService: AuthenticationService,
                userService: UserService,
                secretService: SecretService)(implicit executionContext: ExecutionContext) {

  private val authRouter = new AuthRouter(authService, userService)
  private val usersRouter = new UserRouter(authService, userService)
  private val secretRouter = new SecretRouter(authService, userService, secretService)

  val route: Route = pathPrefix("v1") {
    authRouter.route ~
    usersRouter.route ~
    secretRouter.route
  }

}