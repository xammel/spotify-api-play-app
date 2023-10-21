package controllers

import io.circe.parser._
import models.AccessToken
import play.api.libs.ws._
import play.api.mvc._
import utils.AuthorizationMethods._
import utils.StringConstants._
import utils.TypeAliases.CirceError

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorizationController @Inject() (
    ws: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController {

  lazy val codeVerifier = generateRandomString

  def authorize(): Action[AnyContent] = Action { Redirect(authorizationEndpointWithParams(codeVerifier)) }

  def callback(code: String): Action[AnyContent] =
    Action.async {

      val apiTokenFuture: Future[WSResponse] = ws
        .url(apiTokenEndpoint)
        .addHttpHeaders(CONTENT_TYPE -> FORM)
        .post(apiTokenPayload(code, codeVerifier))

      val accessTokenJson: Future[String]                             = apiTokenFuture.map(_.body)
      val decodedAccessToken: Future[Either[CirceError, AccessToken]] = accessTokenJson.map(decode[AccessToken](_))

      decodedAccessToken.map {
        case Left(circeError) => InternalServerError(circeError.getMessage)
        case Right(accessToken) =>
          Redirect(routes.HomeController.home())
            .withSession(tokenKey -> accessToken.accessToken)
      }
    }
}
