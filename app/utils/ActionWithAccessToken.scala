package utils

import models.AccessToken
import play.api.mvc._
import utils.Functions.{getAccessToken, redirectToAuthorize}

sealed trait ActionWithAccessToken

object ActionWithAccessToken {
  def apply(block: AccessToken => Result)(implicit controllerComponents: ControllerComponents): Action[AnyContent] =
    controllerComponents.actionBuilder { implicit request: RequestHeader =>
      getAccessToken.fold(redirectToAuthorize) { accessToken =>
        block(accessToken)
      }
    }
}
