package models

import io.circe.parser._
import spechelpers.SpecHelpers

class JsonObjectsSpec extends SpecHelpers {

  lazy val testAccessToken        = "123"
  lazy val accessTokenJsonString  = s"""{"access_token" : "$testAccessToken"}"""
  lazy val testErrorMessage       = "test message"
  lazy val spotifyErrorJsonString = s"""{"error": {"status": $UNAUTHORIZED, "message": "$testErrorMessage"}}"""

  "AccessToken" must {
    "decode" in {
      decode[AccessToken](accessTokenJsonString) mustBe Right(AccessToken(accessToken = testAccessToken))
    }
  }

  "Error" must {
    "decode" in {
      decode[Error](spotifyErrorJsonString) mustBe Right(Error(UNAUTHORIZED, testErrorMessage))
    }
  }
}
