package controllers

import play.api.test._
import spechelpers.SpecHelpers
import utils.StringConstants.authorizationEndpointWithParams

class AuthorizationControllerSpec extends SpecHelpers {

  def controller(returnUnexpectedResponse: Boolean = false) = {
    new AuthorizationController(
      mockWS(returnUnexpectedResponse = returnUnexpectedResponse),
      Helpers.stubControllerComponents()
    )
  }

lazy  val authorizationController = controller()

  "AuthorizationController#authorize" should {
    "redirect to spotify's authorization page" in {
      val result = executeAction(authorizationController.authorize(), FakeRequest())
      result.header.status mustBe SEE_OTHER
      result.header.headers mustBe Map(
        LOCATION -> authorizationEndpointWithParams(authorizationController.codeVerifier)
      )
    }
  }

  "AuthorizationController#callback" should {
    "retrieve an access token from spotify" in {

      //TODO need to edit the mockWS instance to return an access token when a POST is made to the endpoint
      val result = executeAction(authorizationController.callback(authorizationController.codeVerifier))

      println(result)

      true mustBe false
    }
  }
}
