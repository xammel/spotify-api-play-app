package spechelpers

import org.scalatestplus.play._
import play.api.http.HeaderNames
import play.api.http.HttpEntity.Strict
import play.api.mvc._
import play.api.test._
import utils.ApiMethods.await
import utils.StringConstants.tokenKey

trait SpecHelpers extends PlaySpec with MockCacheLayer with MockSpotifyApiEndpoints with HeaderNames {

  lazy val requestWithAccessToken = FakeRequest().withSession((tokenKey, testAccessTokenString))

  def executeAction(action: Action[AnyContent], request: FakeRequest[AnyContent] = requestWithAccessToken): Result = {
    val resultFuture = action.apply(request)
    await(resultFuture)
  }

  def getResultBody(result: Result): String =
    result.body match {
      case strict: Strict => strict.data.utf8String
      case _ => throw new Exception("Could not extract data from an HttpEntity that wasn't Strict")
    }

}
