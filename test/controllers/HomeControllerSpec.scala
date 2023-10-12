package controllers

import mockws.MockWS
import mockws.MockWSHelpers.Action
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play._
import play.Application
import play.api.cache.AsyncCacheApi
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class HomeControllerSpec extends PlaySpec with Results {

  val ws: MockWS = MockWS {
    case (GET, "http://dns/url") =>
      Action {
        Ok("http response")
      }
  }

  val cache: AsyncCacheApi = mock[AsyncCacheApi]

  implicit lazy val app: Application = new GuiceApplicationBuilder().build()

  "HomeController#home" should {
    "should be valid" in {
      val controller             = new HomeController(cache, ws, Helpers.stubControllerComponents())
      val result: Future[Result] = controller.home().apply(FakeRequest())
      val awaitedResult = Await.result(result, Duration.Inf)
      awaitedResult mustBe Result().withHeaders(Location -> /authorize)
    }
  }
}
