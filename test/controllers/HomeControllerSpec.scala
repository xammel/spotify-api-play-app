package controllers

import models.TrackList
import play.api.test._
import spechelpers.SpecHelpers
import spechelpers.TestData.{recommendations, trackList}
import utils.ApiMethods.await
import utils.StringConstants.{recommendedTracksCacheKey, topTracksCacheKey}

class HomeControllerSpec extends SpecHelpers {

  "HomeController#home" should {
    "cache the top tracks" in {
      mockCache.removeAll()

      val controller = new HomeController(mockCache, mockWS(), Helpers.stubControllerComponents())

      executeAction(controller.home())

      val cachedTopTracks: Option[TrackList] = await(mockCache.get(topTracksCacheKey))

      cachedTopTracks mustBe Some(trackList)
    }

    "cache the recommended tracks" in {
      mockCache.removeAll()

      val controller = new HomeController(mockCache, mockWS(), Helpers.stubControllerComponents())

      executeAction(controller.home())

      val recommendedTracks: Option[TrackList] = await(mockCache.get(recommendedTracksCacheKey))

      recommendedTracks mustBe Some(recommendations)
    }

    "redirect to authorize if the token is expired" in {
      mockCache.removeAll()

      val controller = new HomeController(mockCache, mockWS(true), Helpers.stubControllerComponents())

      val result = executeAction(controller.home())

      result.header.status mustBe 303
      result.header.headers mustBe Map("Location" -> "/authorize")

    }

    "redirect to home page if all is well" in {
      mockCache.removeAll()

      val controller = new HomeController(mockCache, mockWS(), Helpers.stubControllerComponents())

      val result = executeAction(controller.home())

      result.header.status mustBe 200
    }
  }
}
