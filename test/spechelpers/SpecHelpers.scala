package spechelpers

import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import utils.StringConstants.tokenKey

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SpecHelpers extends PlaySpec with MockCacheLayer with MockSpotifyApiEndpoints {

  lazy val testAccessTokenString  = "abc123"
  lazy val requestWithAccessToken = FakeRequest().withSession((tokenKey, testAccessTokenString))

  def executeAction(action: Action[AnyContent], request: FakeRequest[AnyContent] = requestWithAccessToken): Result = {
    val resultFuture = action.apply(request)
    Await.result(resultFuture, Duration.Inf)
  }

}
