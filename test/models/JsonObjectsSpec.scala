package models

import io.circe.parser._
import spechelpers.SpecHelpers

class JsonObjectsSpec extends SpecHelpers {

  lazy val testErrorMessage       = "test message"
  lazy val spotifyErrorJsonString = s"""{"error": {"status": $UNAUTHORIZED, "message": "$testErrorMessage"}}"""

  "AccessToken" must {
    "decode" in {
      decode[AccessToken](accessTokenJsonString) mustBe Right(AccessToken(accessToken = testAccessTokenString))
    }
  }

  "Error" must {
    "decode" in {
      decode[SpotifyError](spotifyErrorJsonString) mustBe Right(SpotifyError(UNAUTHORIZED, testErrorMessage))
    }
  }
}
