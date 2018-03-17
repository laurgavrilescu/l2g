package http.routes

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import authenticator.Failure
import io.swagger.annotations._
import json.PlayJsonSupport
import model.{Credentials, SignUp, Token, UserMessage}
import services._

import scala.concurrent.{ExecutionContext, Future}
import model.CredentialsJson._
import model.SignUpJson._
import model.UserJson._
import model.TokenJson._

import scala.util.{Success => SS, Failure => FF}

@Api(value = "auth", produces = "application/json")
@Path("v1/auth")
class AuthRouter(authService: AuthenticationService,
                 userService: UserService)(implicit executionContext: ExecutionContext) extends PlayJsonSupport {

  import StatusCodes._

  @Path("/signIn")
  @ApiOperation(httpMethod = "POST", response = classOf[Token], value = "Returns a token")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", required = true, dataTypeClass = classOf[Credentials], paramType = "body", value = "Sign in credentials.")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK", response = classOf[Token]),
    new ApiResponse(code = 404, message = "Mate not found.", response = classOf[Token]),
    new ApiResponse(code = 401, message = "Mate is not logged in.", response = classOf[Token]),
    new ApiResponse(code = 400, message = "Sign in failed.", response = classOf[String])))
  def signInRoute: Route =
    path("signIn") {
      pathEndOrSingleSlash {
        post {
          entity(as[Credentials]) { credentials =>
            onComplete(authService.authenticate(credentials)) {
              case SS(Authenticated(user)) => complete(authService.createToken(user, credentials) map {
                token => OK -> token
              })
              case SS(NoMate(error)) => complete(Future.successful(NotFound -> error))
              case SS(MateNotLoggedIn(error)) => complete(Future.successful(Unauthorized -> error))
              case SS(f: Failure[_]) => complete(Future.successful(BadRequest -> f.cause.getMessage))
              case SS(any) => complete(Future.successful(BadRequest -> s"Unexpected state: ${any.getClass.getName}."))
              case FF(ex) => complete(InternalServerError, ex.getMessage)
            }
          }
        }
      }
    }

  @Path("/signUp")
  @ApiOperation(httpMethod = "POST", response = classOf[Token], value = "Returns a token if user has a valid activation code or an user message that he has to wait for approval from his mate.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", required = true, dataTypeClass = classOf[SignUp], paramType = "body", value = "Sign in credentials.")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Token returned", response = classOf[Token]),
    new ApiResponse(code = 201, message = "User created", response = classOf[UserMessage]),
    new ApiResponse(code = 401, message = "Invalid activation code or user already exists or mate is not logged in.", response = classOf[UserMessage]),
    new ApiResponse(code = 404, message = "Mate does not exist", response = classOf[UserMessage]),
    new ApiResponse(code = 400, message = "Sign up failed.", response = classOf[String])))
  def signUpRoute: Route = path("signUp") {
    pathEndOrSingleSlash {
      post {
        entity(as[SignUp]) { signUpEntity =>
          onComplete(
            authService.signUp(signUpEntity)) {
            case SS(Authenticated(_)) => complete(authService.createToken(signUpEntity) map {
              token => OK -> token
            })
            case SS(WaitingForApproval(mateName)) => complete(Future.successful(Created -> UserMessage(s"Waiting for $mateName to validate you.")))
            case SS(NoMate(error)) => complete(Future.successful(NotFound -> error))
            case SS(UserExists(error)) => complete(Future.successful(Unauthorized -> error))
            case SS(MateNotLoggedIn(error)) => complete(Future.successful(Unauthorized -> error))
            case SS(ActivationCodeInvalid(error)) => complete(Future.successful(Unauthorized -> error))
            case SS(f: Failure[_]) => complete(Future.successful(BadRequest -> f.cause.getMessage))
            case SS(any) => complete(Future.successful(BadRequest -> s"Unexpected state: ${any.getClass.getName}."))
            case FF(ex) => complete(InternalServerError, ex.getMessage)
          }
        }
      }
    }
  }

  val route: Route = pathPrefix("auth") {
    signInRoute ~
      signUpRoute
  }
}
