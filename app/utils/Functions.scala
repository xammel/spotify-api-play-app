package utils

import play.api.mvc.{RequestHeader, _}
import utils.StringConstants.tokenKey
import play.api.mvc.Results

object Functions extends Results {

  def redirectToAuthorize: Result = Redirect(controllers.routes.AuthorizationController.authorize())
  def getAccessToken(implicit request: RequestHeader): Option[String] = request.session.get(tokenKey)
  def getAccessTokenUnsafe(request: RequestHeader): String =
    request.session.get(tokenKey) match {
      case Some(v) => v
      case None =>
        throw new Exception(
          s"No value found in session state for key $tokenKey"
        )
    }
  def joinURLParameters(params: Map[String, String]): String =
    params.map { case (k, v) => s"$k=$v" }.mkString("&")

}
