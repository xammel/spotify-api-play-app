package controllers

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

import javax.inject.Inject
import play.api.libs.ws._
import play.api.mvc._
import utils.StringConstants.{
  clientId,
  authorizeEndpoint,
  lengthOfCodeVerifier,
  sha256,
  apiTokenEndpoint,
  authorizationCallback
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.Random

class Controller @Inject()(ws: WSClient,
                           val controllerComponents: ControllerComponents)
    extends BaseController {

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

  def joinURLParameters(params: Map[String, String]): String =
    params.map { case (k, v) => s"$k=$v" }.mkString("&")

  //TODO look into if generating these on initiation of the class is an issue
//  lazy val randomString = generateRandomString
//  lazy val hashedString = sha256Hash(randomString)
//  lazy val baseEncoded = base64URLEncode(hashedString)
  lazy val codeVerifier = generateRandomString

  def authorize(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val codeChallenge: String =
        Await.result(generateCodeChallenge(codeVerifier), Duration.Inf)
      println(codeVerifier)
      println(codeChallenge)
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
      Ok(views.html.showArtist(res.body))
  }
}
