package controllers

import io.circe
import io.circe.parser._
import models.AccessToken
import play.api.libs.ws._
import play.api.mvc._
import utils.ApiMethods.{await, joinURLParameters}
import utils.AuthorizationMethods._
import utils.StringConstants._

import javax.inject.Inject
import scala.concurrent.Future

class AuthorizationController @Inject() (
    ws: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController {

  lazy val codeVerifier = generateRandomString

  def authorize(): Action[AnyContent] =
    Action {
      val codeChallenge: String = generateCodeChallenge(codeVerifier)
      val joinedParams          = joinURLParameters(authorizeParams(codeChallenge))

      Redirect(s"$authorizeEndpoint$joinedParams")
    }

  def callback(code: String): Action[AnyContent] =
    Action {
      val joinedParams = joinURLParameters(callbackParams(code, codeVerifier))

      val apiTokenFuture: Future[WSResponse] = ws
        .url(apiTokenEndpoint)
        .addHttpHeaders(CONTENT_TYPE -> FORM)
        .post(joinedParams)

      val accessTokenJson: String                              = await(apiTokenFuture).body
      val decodedAccessToken: Either[circe.Error, AccessToken] = decode[AccessToken](accessTokenJson)

      decodedAccessToken match {
        case Left(circeError) => InternalServerError(circeError.getMessage)
        case Right(accessToken) =>
          Redirect(routes.HomeController.home())
            .withSession(tokenKey -> accessToken.accessToken)
      }
    }
}
