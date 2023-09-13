package controllers

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

import io.circe.parser._
import javax.inject.Inject
import models.AccessToken
import play.api.libs.ws._
import play.api.mvc._
import utils.Functions.joinURLParameters
import utils.StringConstants.{tokenKey, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random

class AuthorizationController @Inject()(
  ws: WSClient,
  val controllerComponents: ControllerComponents
) extends BaseController {

  def generateRandomString: String = {
    Random.alphanumeric.take(lengthOfCodeVerifier).mkString("")
  }

  def base64Encode(bytes: Array[Byte]): String = {
    Base64.getUrlEncoder.withoutPadding
      .encodeToString(bytes)
      .replace('+', '-')
      .replace('/', '_')
  }

  def generateCodeChallenge(codeVerifier: String): Future[String] = Future {
    val encoder: MessageDigest = MessageDigest.getInstance(sha256)
    val data: Array[Byte] =
      encoder.digest(codeVerifier.getBytes(StandardCharsets.UTF_8))
    base64Encode(data)
  }

  //TODO look into if generating these on initiation of the class is an issue
  lazy val codeVerifier = generateRandomString

  def authorize(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val codeChallenge: String =
        Await.result(generateCodeChallenge(codeVerifier), Duration.Inf)
      val params = Map(
        "response_type" -> "code",
        "client_id" -> clientId,
        "redirect_uri" -> authorizationCallback,
        "code_challenge_method" -> "S256",
        "code_challenge" -> codeChallenge
      )
      val joinedParams = joinURLParameters(params)
      val url = s"$authorizeEndpoint$joinedParams"
      Results.Redirect(url)
  }

  def callback(code: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val params = Map(
        "grant_type" -> "authorization_code",
        "code" -> code,
        "redirect_uri" -> authorizationCallback,
        "client_id" -> clientId,
        "code_verifier" -> codeVerifier
      )

      val hitURL: Future[WSResponse] = ws
        .url(apiTokenEndpoint)
        .addHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
        .post(joinURLParameters(params))

      val res = Await.result(hitURL, Duration.Inf)
      val decodedAccessToken = decode[AccessToken](res.body)
      decodedAccessToken match {
        case Left(failure) => InternalServerError(failure.getMessage)
        case Right(accessToken) => {
          Redirect(routes.HomeController.home())
            .withSession(tokenKey -> accessToken.access_token)
        }
      }
  }
}
