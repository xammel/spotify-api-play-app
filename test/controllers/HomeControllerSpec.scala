package controllers

import play.Application
import play.api.test._
import play.inject.guice.GuiceApplicationBuilder
import spechelpers.SpecHelpers

class HomeControllerSpec extends SpecHelpers {

  lazy val app: Application = new GuiceApplicationBuilder().build()
  lazy val controller       = new HomeController(mockCache, ws, Helpers.stubControllerComponents())

  "HomeController#home" should {
    "should " in {

      val result = executeAction(controller.home(), requestWithAccessToken)

      result.header.status mustBe 303
      result.header.headers mustBe Map("Location" -> "/authorize")
    }
  }
}
