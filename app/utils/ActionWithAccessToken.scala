package utils

import models.AccessToken
import play.api.mvc._
import utils.ApiMethods.{getAccessToken, redirectToAuthorize}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed trait ActionWithAccessToken

object ActionWithAccessToken {

  def apply(
      block: AccessToken => Future[Result]
  )(implicit controllerComponents: ControllerComponents): Action[AnyContent] =
    controllerComponents.actionBuilder.async { implicit request: RequestHeader =>
      getAccessToken.fold(Future(redirectToAuthorize)) { accessToken =>
        block(accessToken)
      }
    }
}
