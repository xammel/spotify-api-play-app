package utils

import play.api.mvc.RequestHeader
import utils.StringConstants.tokenKey
import java.lang.Exception

object Functions {

  def getAccessToken(request: RequestHeader): String =
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
