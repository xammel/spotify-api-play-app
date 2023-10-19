package controllers

import play.api.test._
import spechelpers.SpecHelpers
import utils.StringConstants.{authorizationEndpointWithParams, tokenKey}

class AuthorizationControllerSpec extends SpecHelpers {

  def controller(returnUnexpectedResponse: Boolean = false) = {
    new AuthorizationController(
      mockWS(returnUnexpectedResponse = returnUnexpectedResponse),
      Helpers.stubControllerComponents()
    )
  }

  lazy val authorizationController = controller()
  import authorizationController.codeVerifier

  "AuthorizationController#authorize" should {
    "redirect to spotify's authorization page" in {
      val result = executeAction(authorizationController.authorize(), FakeRequest())
      result.header.status mustBe SEE_OTHER
      result.header.headers mustBe Map(
        LOCATION -> authorizationEndpointWithParams(codeVerifier)
      )
    }
  }

  "AuthorizationController#callback" should {

    lazy val result = executeAction(authorizationController.callback(codeVerifier))

    "retrieve the access token from spotify and add it to the session" in {
      result.newSession.flatMap(_.data.get(tokenKey)) mustBe Some(
        testAccessTokenString
      )
    }

    "redirect to the homepage" in {
      result.header.status mustBe SEE_OTHER
      result.header.headers mustBe Map(LOCATION -> "/")
    }
  }
}
