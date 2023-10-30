package utils

import models.{AccessToken, SpotifyError, Recommendations, TrackList}
import play.api.cache.AsyncCacheApi
import spechelpers.SpecHelpers
import utils.CacheMethods._
import utils.StringConstants._
import utils.ApiMethods.await

class CacheMethodsSpec extends SpecHelpers {

  lazy val testKey                       = "testKey"
  lazy val token: AccessToken            = AccessToken(testAccessTokenString)

  "getCache" must {
    "return a Left error if the key is not defined in the cache" in {
      implicit val cache = mockCache
      getCache[Int](testKey) mustBe Left(SpotifyError(INTERNAL_SERVER_ERROR, getCacheErrorMessage(testKey)))
    }

    "return the Right value from the cache if the key does exist in the cache" in {
      implicit val cache = mockCache
      await(cache.set(testKey, 2))
      getCache[Int](testKey) mustBe Right(2)
    }
  }

  "cacheTopTracks" must {
    "insert the top tracks into the cache" in {
      implicit val cache = mockCache
      cacheTopTracks(token, mockWS(), cache)
      getCache[TrackList](topTracksCacheKey) mustBe Right(trackList)
    }

    "return the spotify error if that is returned from the API" in {
      val result = cacheTopTracks(token, mockWS(accessTokenIsExpired = true), mockCache)
      result mustBe Left(SpotifyError(unauthorizedSpotifyError.error.status, unauthorizedSpotifyError.error.message))
    }

    "return a custom error if the error message cannot be decoded" in {
      val result = cacheTopTracks(token, mockWS(returnUnexpectedResponse = true), mockCache)
      result mustBe Left(SpotifyError(INTERNAL_SERVER_ERROR, cacheTopTracksErrorMessage))
    }
  }

  "cacheRecommendedTracks" must {
    "insert the recommended tracks into the cache" in {
      implicit val cache = mockCache
      cacheRecommendedTracks(trackList)(token, mockWS(), cache)
      getCache[Recommendations](recommendedTracksCacheKey) mustBe Right(recommendations)
    }

    "return the spotify error if that is returned from the API" in {
      val result = cacheRecommendedTracks(trackList)(token, mockWS(accessTokenIsExpired = true), mockCache)
      result mustBe Left(SpotifyError(unauthorizedSpotifyError.error.status, unauthorizedSpotifyError.error.message))
    }

    "return a custom error if the error message cannot be decoded" in {
      val result = cacheRecommendedTracks(trackList)(token, mockWS(returnUnexpectedResponse = true), mockCache)
      result mustBe Left(SpotifyError(INTERNAL_SERVER_ERROR, cacheRecommendedTracksErrorMessage))
    }
  }

}
