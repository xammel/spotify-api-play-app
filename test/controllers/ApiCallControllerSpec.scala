package controllers

import play.api.cache.AsyncCacheApi
import play.api.mvc.Result
import play.api.test._
import spechelpers.SpecHelpers
import utils.ApiMethods.await
import utils.StringConstants.{recommendedTracksCacheKey, topTracksCacheKey}
class ApiCallControllerSpec extends SpecHelpers {

  def controller(
      accessTokenIsExpired: Boolean = false,
      returnUnexpectedResponse: Boolean = false,
      returnNonAuthError: Boolean = false,
      cacheOpt: Option[AsyncCacheApi] = None
  ) = {

    val cache: AsyncCacheApi = cacheOpt.fold {
      mockCache
    }(identity)

    new ApiCallController(
      cache,
      mockWS(
        accessTokenIsExpired = accessTokenIsExpired,
        returnUnexpectedResponse = returnUnexpectedResponse,
        returnNonAuthError = returnNonAuthError
      ),
      Helpers.stubControllerComponents()
    )
  }

  "getMyTopArtists" should {
    "redirect to authorize if the token is expired" in {
      val result = executeAction(controller(accessTokenIsExpired = true).artists())

      result.header.status mustBe SEE_OTHER
      result.header.headers mustBe Map(LOCATION -> "/authorize")
    }

    "throw an InternalServerError if the response from the API is not able to be decoded" in {
      val result = executeAction(controller(returnUnexpectedResponse = true).artists())

      result.header.status mustBe INTERNAL_SERVER_ERROR
    }

    "redirect to top artists page if all is well" in {
      val result: Result = executeAction(controller().artists())

      result.header.status mustBe OK
      getResultBody(result) must include("Your Top Artists")
    }
  }

  "getMyTopTracks" should {
    "redirect to authorize if the token is expired" in {
      val result = executeAction(controller(accessTokenIsExpired = true).tracks())

      result.header.status mustBe SEE_OTHER
      result.header.headers mustBe Map(LOCATION -> "/authorize")
    }

    "throw an InternalServerError if the spotify error can be decoded but is not an authorization error" in {
      val result = executeAction(controller(returnNonAuthError = true).tracks())

      result.header.status mustBe INTERNAL_SERVER_ERROR
      getResultBody(result) mustEqual nonAuthSpotifyError.error.message
    }

    "throw an InternalServerError if the response from the API is not able to be decoded" in {
      val result = executeAction(controller(returnUnexpectedResponse = true).tracks())

      result.header.status mustBe INTERNAL_SERVER_ERROR
      getResultBody(result) mustEqual "Response couldn't be decoded as an error or artist details..."
    }

    "redirect to top tracks page if all is well" in {
      val result: Result = executeAction(controller().tracks())

      result.header.status mustBe OK
      getResultBody(result) must include("Your Top Tracks")
    }
  }

  "getRecommendedTracks" should {

    "redirect to recommendations page if all is well" in {

      val cache = mockCache
      cache.set(topTracksCacheKey, trackList)
      cache.set(recommendedTracksCacheKey, recommendations)

      val result: Result = executeAction(controller(cacheOpt = Some(cache)).recommendations())

      result.header.status mustBe OK
      getResultBody(result) must include("Recommendations")
    }

    "redirect to home if recommended tracks aren't defined in the cache" in {
      val cache = mockCache
      cache.set(topTracksCacheKey, trackList)

      val result: Result = executeAction(controller(cacheOpt = Some(cache)).recommendations())

      result.header.status mustBe SEE_OTHER
      result.header.headers mustBe Map(LOCATION -> "/")
    }

    "redirect to home if top tracks aren't defined in the cache" in {
      val cache = mockCache
      cache.set(recommendedTracksCacheKey, recommendations)

      val result: Result = executeAction(controller(cacheOpt = Some(cache)).recommendations())

      result.header.status mustBe SEE_OTHER
      result.header.headers mustBe Map(LOCATION -> "/")
    }
  }

  "saveTrack" should {
    "perform a put request to the tracks spotify endpoint to save a track to the users library" in {
      val cache = mockCache
      cache.set(topTracksCacheKey, trackList)
      cache.set(recommendedTracksCacheKey, recommendations)

      val result = executeAction(controller(cacheOpt = Some(cache)).saveTrack(""))

      result.header.status mustBe OK
      getResultBody(result) mustEqual testPutResponse
    }
  }

  "refreshRecommendations" must {
    "update the recommendations in the cache" in {
      val cache = mockCache
      cache.set(topTracksCacheKey, trackList)
      cache.set(recommendedTracksCacheKey, recommendations2)

      executeAction(controller(cacheOpt = Some(cache)).refreshRecommendations())

      await(cache.get(recommendedTracksCacheKey)) mustBe Some(recommendations)
    }

    "throw an error if the top tracks can't be fetched from the cache" in {
      val result = executeAction(controller().refreshRecommendations())

      result.header.status mustEqual INTERNAL_SERVER_ERROR
      getResultBody(result) mustEqual s"Could not retrieve item from cache with key: $topTracksCacheKey"
    }
  }

}
