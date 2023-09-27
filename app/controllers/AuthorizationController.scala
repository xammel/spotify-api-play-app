package controllers

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

import io.circe
import io.circe.parser._
import javax.inject.Inject
import models.AccessToken
import play.api.libs.ws._
import play.api.mvc._
import utils.Functions.joinURLParameters
import utils.StringConstants._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random

class AuthorizationController @Inject() (
    ws: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController {

  implicit val implicitWS = ws
  lazy val codeVerifier   = generateRandomString

  def generateRandomString: String = {
    Random.alphanumeric.take(lengthOfCodeVerifier).mkString("")
  }

  def base64Encode(bytes: Array[Byte]): String = {
    Base64.getUrlEncoder.withoutPadding
      .encodeToString(bytes)
      .replace('+', '-')
      .replace('/', '_')
  }

  def generateCodeChallenge(codeVerifier: String): String = {
    val encoder: MessageDigest         = MessageDigest.getInstance(sha256)
    val codeVerifierBytes: Array[Byte] = codeVerifier.getBytes(StandardCharsets.UTF_8)
    val data: Array[Byte]              = encoder.digest(codeVerifierBytes)
    base64Encode(data)
  }

  def authorize(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val codeChallenge: String = generateCodeChallenge(codeVerifier)
      val joinedParams = joinURLParameters(authorizeParams(codeChallenge))
      val url          = s"$authorizeEndpoint$joinedParams"
      Redirect(url)
    }

  def callback(code: String): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>

      val joinedParams = joinURLParameters(callbackParams(code, codeVerifier))

      val hitURL: Future[WSResponse] = ws
        .url(apiTokenEndpoint)
        .addHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
        .post(joinedParams)

      val response: WSResponse                                 = Await.result(hitURL, Duration.Inf)
      val decodedAccessToken: Either[circe.Error, AccessToken] = decode[AccessToken](response.body)

      decodedAccessToken match {
        case Left(failure) => InternalServerError(failure.getMessage)
        case Right(accessToken) => {
          Redirect(routes.HomeController.home())
            .withSession(tokenKey -> accessToken.access_token)
        }
      }
    }
}
