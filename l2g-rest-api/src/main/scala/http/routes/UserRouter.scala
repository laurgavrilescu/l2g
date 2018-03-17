package http.routes

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.AuthenticationDirectives
import io.swagger.annotations._
import json.PlayJsonSupport
import model._
import services.{AuthenticationService, UserService}

import scala.concurrent.ExecutionContext
import model.AuthenticationPermissionJson._
import model.UserJson._

@Path("/v1/user")
@Api(value = "/user")
class UserRouter(authService: AuthenticationService,
                 userService: UserService)(implicit executionContext: ExecutionContext) extends PlayJsonSupport {

  import StatusCodes._

  val authDirectives = new AuthenticationDirectives(authService)

  @Path("/permissions")
  @ApiOperation(httpMethod = "GET", response = classOf[Token], value = "Returns users that require permissions to authenticate.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "token", required = true, dataType = "string", paramType = "query", value = "Authentication token.")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "List of users that require permissions to authenticate", response = classOf[UserNameList]),
    new ApiResponse(code = 401, message = "User is not authorized to perform this action.")))
  def permissionsRoute: Route = path("permissions") {
    pathEndOrSingleSlash {
      (get & authDirectives.authenticated) { user: User =>
        complete(
          userService.findMates(user.name) map {
            users => OK -> UserNameList(users.map(_.name))
          }
        )
      }
    }
  }

  @Path("/permissions/add")
  @ApiOperation(httpMethod = "POST", response = classOf[UserMessage], value = "Add permission for the specified user to authenticate.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "token", required = true, dataType = "string", paramType = "query", value = "Authentication token."),
    new ApiImplicitParam(name = "body", required = true, dataTypeClass = classOf[AuthenticationPermission], paramType = "body", value = "User which is given the permission to authenticate.")

  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Authentication message.", response = classOf[UserMessage]),
    new ApiResponse(code = 500, message = "Internal server error message.", response = classOf[String]),
    new ApiResponse(code = 401, message = "User is not authorized to perform this action.")))
  def givePermissionRoute: Route = path("permissions" / "add") {
    pathEndOrSingleSlash {
      (post & authDirectives.authenticated) { _: User =>
        entity(as[AuthenticationPermission]) { authPermission =>
          complete(
            userService.activate(authPermission.forUserName) map {
              case Some(userName) => OK -> UserMessage(s"User ${authPermission.forUserName} was authenticated.")
              case None => NotFound -> UserMessage(s"User ${authPermission.forUserName} was not found.")
            }
          )
        }
      }
    }
  }


  val route: Route = pathPrefix("user") {
      permissionsRoute ~
        givePermissionRoute
  }
}
