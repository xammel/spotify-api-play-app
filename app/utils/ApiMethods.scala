package utils

import models.AccessToken
import play.api.http.HeaderNames
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc._
import utils.StringConstants._

import scala.concurrent.duration._

object ApiMethods extends Results with HeaderNames {

  def redirectToAuthorize: Result = Redirect(controllers.routes.AuthorizationController.authorize())
  def getAccessToken(implicit request: RequestHeader): Option[AccessToken] =
    request.session.get(tokenKey).map(AccessToken(_))

  def hitApi(url: String)(implicit accessToken: AccessToken, ws: WSClient): WSRequest =
    ws.url(url)
      .addHttpHeaders(s"$AUTHORIZATION" -> s"Bearer ${accessToken.access_token}")
      .withRequestTimeout(10000.millis)

  def joinURLParameters(params: Map[String, String]): String = params.map { case (k, v) => s"$k=$v" }.mkString("&")

}
