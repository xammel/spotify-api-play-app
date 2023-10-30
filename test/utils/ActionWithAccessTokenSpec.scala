package utils

import mockws.MockWSHelpers.materializer
import play.api.mvc._
import play.api.test._
import spechelpers.SpecHelpers
import utils.ApiMethods.await

class ActionWithAccessTokenSpec extends SpecHelpers {

  implicit lazy val implicitComponents    = Helpers.stubControllerComponents()
  lazy val testAction: Action[AnyContent] = ActionWithAccessToken { accessToken => Ok(accessToken.accessToken) }

  "ActionWithAccessToken" must {
    "redirect to authorize if the access token is not defined" in {
      val result = executeAction(testAction, FakeRequest())

      result.header.status mustBe SEE_OTHER
      result.header.headers mustBe Map(LOCATION -> "/authorize")
    }

    "execute the provided block if the access token is defined" in {

      val result     = executeAction(testAction)
      val resultBody = await(result.body.consumeData)

      result.header.status mustBe OK
      resultBody.utf8String mustBe testAccessTokenString
    }
  }

}
