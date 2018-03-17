package http.routes

import javax.ws.rs.Path

import services._

import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.AuthenticationDirectives
import io.swagger.annotations._
import json.PlayJsonSupport
import model._
import model.SecretJson._

import scala.util.{Failure => FF, Success => SS}

@Path("v1/secrets")
@Api(value = "secrets")
class SecretRouter(authService: AuthenticationService,
                   userService: UserService,
                   secretService: SecretService)(implicit executionContext: ExecutionContext) extends PlayJsonSupport {


  import StatusCodes._

  val authDirectives = new AuthenticationDirectives(authService)

  @ApiOperation(httpMethod = "GET", response = classOf[Token], value = "Returns all secrets for the given user.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "token", required = true, dataType = "string", paramType = "query", value = "Authentication token.")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "List of personal secrets", response = classOf[SecretList]),
    new ApiResponse(code = 401, message = "User is not authorized to perform this action.")))
  def getMySecretRoute: Route = pathEndOrSingleSlash {
    (get & authDirectives.authenticated) { user: User =>
      complete(
        secretService.view(user.name) map {
          secrets => OK -> SecretList(secrets.map(_.text))
        })
    }
  }

  @Path("/allowed")
  @ApiOperation(httpMethod = "GET", response = classOf[Token], value = "Returns all allowed secrets for the given user.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "token", required = true, dataType = "string", paramType = "query", value = "Authentication token.")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "List of allowed to see secrets", response = classOf[SecretList]),
    new ApiResponse(code = 401, message = "User is not authorized to perform this action.")))
  def getAllowedSecretRoute: Route = path("allowed") {
    pathEndOrSingleSlash {
      (get & authDirectives.authenticated) { user: User =>
        complete(
          secretService.viewAsAllowed(user.name) map {
            secrets => OK -> SecretList(secrets.map(_.text))
          }
        )
      }
    }
  }

  @Path("/add")
  @ApiOperation(httpMethod = "POST", response = classOf[SecretText], value = "Add a secret for the current user.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "token", required = true, dataType = "string", paramType = "query", value = "Authentication token."),
    new ApiImplicitParam(name = "body", required = true, dataTypeClass = classOf[SecretText], paramType = "body", value = "Secret text to add.")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Secret text that was added.", response =  classOf[SecretText]),
    new ApiResponse(code = 401, message = "User is not authorized to perform this action.")))
  def addSecretRoute: Route = path("add") {
    pathEndOrSingleSlash {
      (post & authDirectives.authenticated) { user: User =>
        entity(as[SecretText]) { secret =>
          complete(
            secretService.find(secret.value) flatMap {
              case None => secretService.add(Secret(user.name, secret.value, Set())) map {
                _ => OK -> secret
              }
              case Some(_) => Future.successful(OK -> secret)
            }
          )
        }
      }
    }
  }

  @Path("/share")
  @ApiOperation(httpMethod = "POST", response = classOf[SecretShare], value = "Share a secret for the given user.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "token", required = true, dataType = "string", paramType = "query", value = "Authentication token."),
    new ApiImplicitParam(name = "body", required = true, dataTypeClass = classOf[SecretShare], paramType = "body", value = "User to share the secret.")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Secret that was shared.", response = classOf[SecretShare]),
    new ApiResponse(code = 404, message = "User or secret not found."),
    new ApiResponse(code = 401, message = "User is not authorized to perform this action.")))
  def shareSecretWithUserRoute: Route = path("share") {
    pathEndOrSingleSlash {
      (post & authDirectives.authenticated) { _: User =>
        entity(as[SecretShare]) { secretShare =>
          onComplete(userService.find(secretShare.userName) flatMap {
              case Some(user) =>
                secretService.addUser(secretShare.secretText, user.name) map {
                  case Some(secret) => OK -> secret
                  case None => NotFound -> s"Secret ${secretShare.secretText} not found."
                }
              case None =>
                Future.successful(NotFound -> s"User not found ${secretShare.userName}")
          }) {
            case SS((code: StatusCode, secret: Secret)) => complete(Future.successful(code -> secret))
            case SS((code: StatusCode, message: String)) => complete(Future.successful(code -> message))
            case SS(any) => complete(Future.successful(BadRequest -> s"Unexpected state: ${any.getClass.getName}."))
            case FF(ex) => complete(Future.successful(InternalServerError -> ex.getMessage))
          }
        }
      }
    }
  }

  val route: Route = pathPrefix("secrets") {
    getMySecretRoute ~ getAllowedSecretRoute ~ addSecretRoute ~ shareSecretWithUserRoute
  }
}