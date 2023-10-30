package controllers

import models.TrackList
import play.api.cache.AsyncCacheApi
import play.api.test._
import spechelpers.SpecHelpers
import utils.ApiMethods.await
import utils.StringConstants.{recommendedTracksCacheKey, topTracksCacheKey}

class HomeControllerSpec extends SpecHelpers {

  def controller(cache: Option[AsyncCacheApi] = None, accessTokenIsExpired: Boolean = false, returnUnexpectedResponse: Boolean = false) = {
    new HomeController(
      cache.fold(mockCache)(identity),
      mockWS(accessTokenIsExpired = accessTokenIsExpired, returnUnexpectedResponse = returnUnexpectedResponse),
      Helpers.stubControllerComponents()
    )
  }

  "home" should {
    "cache the top tracks" in {

      val cache = mockCache

      executeAction(controller(cache = Some(cache)).home())

      val cachedTopTracks: Option[TrackList] = await(cache.get(topTracksCacheKey))

      cachedTopTracks mustBe Some(trackList)
    }

    "cache the recommended tracks" in {

      val cache = mockCache

      executeAction(controller(cache = Some(cache)).home())

      val recommendedTracks: Option[TrackList] = await(cache.get(recommendedTracksCacheKey))

      recommendedTracks mustBe Some(recommendations)
    }

    "redirect to authorize if the token is expired" in {

      val result = executeAction(controller(accessTokenIsExpired = true).home())

      result.header.status mustBe SEE_OTHER
      result.header.headers mustBe Map(LOCATION -> "/authorize")
    }

    "redirect to home page if all is well" in {
      val result = executeAction(controller().home())

      result.header.status mustBe OK
      getResultBody(result) must include("""<section id="home-content">""")
    }

    "throw an InternalServerError if the response from the API is not able to be decoded" in {
      val result = executeAction(controller(returnUnexpectedResponse = true).home())

      result.header.status mustBe INTERNAL_SERVER_ERROR
    }
  }
}