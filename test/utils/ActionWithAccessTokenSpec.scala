package utils

import mockws.MockWSHelpers.materializer
import play.api.mvc._
import play.api.test._
import spechelpers.SpecHelpers

import scala.concurrent.Await
import scala.concurrent.duration.Duration
class ActionWithAccessTokenSpec extends SpecHelpers {

  implicit lazy val implicitComponents    = Helpers.stubControllerComponents()
  lazy val testAction: Action[AnyContent] = ActionWithAccessToken { accessToken => Ok(accessToken.accessToken) }

  "ActionWithAccessToken" must {
    "should redirect to authorize if the access token is not defined" in {
      val result = executeAction(testAction, FakeRequest())

      result.header.status mustBe 303
      result.header.headers mustBe Map("Location" -> "/authorize")
    }

    "should execute the provided block if the access token is defined" in {

      val result = executeAction(testAction)
      val resultBody = Await.result(result.body.consumeData, Duration.Inf)

      result.header.status mustBe 200
      resultBody.utf8String mustBe testAccessTokenString
    }
  }

}
