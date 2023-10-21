package utils

import models.{AccessToken, Error, Recommendations, TrackList}
import play.api.cache.AsyncCacheApi
import spechelpers.SpecHelpers
import utils.CacheMethods._
import utils.StringConstants._

class CacheMethodsSpec extends SpecHelpers {

  lazy val testKey                       = "testKey"
  lazy implicit val cache: AsyncCacheApi = mockCache
  lazy val token: AccessToken            = AccessToken(testAccessTokenString)

  "getCache" must {
    "return a Left error if the key is not defined in the cache" in {
      cache.removeAll()
      getCache[Int](testKey) mustBe Left(Error(INTERNAL_SERVER_ERROR, getCacheErrorMessage(testKey)))
    }

    "return the Right value from the cache if the key does exist in the cache" in {
      cache.removeAll()
      cache.set(testKey, 2)
      getCache[Int](testKey) mustBe Right(2)
    }
  }

  "cacheTopTracks" must {
    "insert the top tracks into the cache" in {
      cache.removeAll()
      cacheTopTracks(token, mockWS(), cache)
      getCache[TrackList](topTracksCacheKey) mustBe Right(trackList)
    }

    "return the spotify error if that is returned from the API" in {
      cache.removeAll()
      val result = cacheTopTracks(token, mockWS(accessTokenIsExpired = true), cache)
      result mustBe Left(Error(unauthorizedSpotifyError.error.status, unauthorizedSpotifyError.error.message))
    }

    "return a custom error if the error message cannot be decoded" in {
      cache.removeAll()
      val result = cacheTopTracks(token, mockWS(returnUnexpectedResponse = true), cache)
      result mustBe Left(Error(INTERNAL_SERVER_ERROR, cacheTopTracksErrorMessage))
    }
  }

  "cacheRecommendedTracks" must {
    "insert the recommended tracks into the cache" in {
      cache.removeAll()
      cacheRecommendedTracks(trackList)(token, mockWS(), cache)
      getCache[Recommendations](recommendedTracksCacheKey) mustBe Right(recommendations)
    }

    "return the spotify error if that is returned from the API" in {
      cache.removeAll()
      val result = cacheRecommendedTracks(trackList)(token, mockWS(accessTokenIsExpired = true), cache)
      result mustBe Left(Error(unauthorizedSpotifyError.error.status, unauthorizedSpotifyError.error.message))
    }

    "return a custom error if the error message cannot be decoded" in {
      cache.removeAll()
      val result = cacheRecommendedTracks(trackList)(token, mockWS(returnUnexpectedResponse = true), cache)
      result mustBe Left(Error(INTERNAL_SERVER_ERROR, cacheRecommendedTracksErrorMessage))
    }
  }

}
