package controllers

import play.api.mvc.Result
import play.api.test._
import spechelpers.SpecHelpers
import play.api.http.HttpEntity.Strict

class ApiCallControllerSpec extends SpecHelpers {

  def controller(accessTokenIsExpired: Boolean = false, returnUnexpectedResponse: Boolean = false) = {
    mockCache.removeAll()
    new ApiCallController(
      mockCache,
      mockWS(accessTokenIsExpired = accessTokenIsExpired, returnUnexpectedResponse = returnUnexpectedResponse),
      Helpers.stubControllerComponents()
    )
  }

  "ApiCallController#getMyTopArtists" should {
    "redirect to authorize if the token is expired" in {
      val result = executeAction(controller(accessTokenIsExpired = true).getMyTopArtists())

      result.header.status mustBe SEE_OTHER
      result.header.headers mustBe Map(LOCATION -> "/authorize")
    }

    "throw an InternalServerError if the response from the API is not able to be decoded" in {
      val result = executeAction(controller(returnUnexpectedResponse = true).getMyTopArtists())

      result.header.status mustBe INTERNAL_SERVER_ERROR
    }

    "redirect to top artists page if all is well" in {
      val result: Result = executeAction(controller().getMyTopArtists())

      result.header.status mustBe OK
      getResultBody(result) must include("Your Top Artists")
    }
  }

}
