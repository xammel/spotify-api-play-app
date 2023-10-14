package spechelpers

import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import utils.ApiMethods.await
import utils.StringConstants.tokenKey

trait SpecHelpers extends PlaySpec with MockCacheLayer with MockSpotifyApiEndpoints {

  lazy val testAccessTokenString  = "abc123"
  lazy val requestWithAccessToken = FakeRequest().withSession((tokenKey, testAccessTokenString))

  def executeAction(action: Action[AnyContent], request: FakeRequest[AnyContent] = requestWithAccessToken): Result = {
    val resultFuture = action.apply(request)
    await(resultFuture)
  }

}
