package controllers

import javax.inject.Inject
import play.api.libs.ws._
import play.api.mvc._
import utils.StringConstants.{
  currentToken,
  getArtistEndpoint,
  searchApi,
  authorizeEndpoint
}
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}
import scala.util.Random
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.nio.charset.StandardCharsets.UTF_8
import play.mvc.Results.redirect
import utils.StringConstants.CLIENT_ID
import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.JsonBodyWritables._

class Controller @Inject()(ws: WSClient,
                           val controllerComponents: ControllerComponents)
    extends BaseController {

  def generateRandomString: String = Random.alphanumeric.take(128).mkString("")

  def sha256Hash(text: String): String = {
    val digestInstance = MessageDigest.getInstance("SHA-256")
    val bigInt = new BigInteger(1, digestInstance.digest(text.getBytes(UTF_8)))
    String.format("%064x", bigInt)
  }

  def base64URLEncode(text: String): String =
    Base64.getEncoder
      .encodeToString(text.getBytes(UTF_8))
      .replace('+', '-')
      .replace('/', '_')
      .takeWhile(_ != '=')

  def joinURLParameters(params: Seq[(String, String)]): String =
    params.map(tup => s"${tup._1}=${tup._2}").mkString("&")

  //TODO look into if generating these on initiation of the class is an issue
  lazy val randomString = generateRandomString
  lazy val hashedString = sha256Hash(randomString)
  lazy val baseEncoded = base64URLEncode(hashedString)

  def authorize(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      println(randomString)
      println(hashedString)
      println(baseEncoded)
      val params = Seq(
        ("response_type", "code"),
        ("client_id", CLIENT_ID),
        ("redirect_uri", "http://localhost:9000/callback"),
        ("code_challenge_method", "S256"),
        ("code_challenge", baseEncoded)
      )
      val joinedParams = joinURLParameters(params)
      val url = s"$authorizeEndpoint$joinedParams"
      Results.Redirect(url)
//    ws.url(url).get()
  }

  def callback(code: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      println(randomString)
      val params = Seq(
        ("grant_type", "authorization_code"),
        ("code", code),
        ("redirect_uri", "http://localhost:9000/callback"),
        ("client_id", CLIENT_ID),
        ("code_verifier", randomString)
      )
      val url = "https://accounts.spotify.com/api/token"
      val hitURL: Future[WSResponse] = ws
        .url(url)
        .addHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
        .post(joinURLParameters(params))

      val res = Await.result(hitURL, Duration.Inf)
      Ok(views.html.showArtist(res.body))
  }
}
