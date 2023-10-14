package models

import io.circe.parser._
import spechelpers.SpecHelpers
import utils.CacheMethods.UNAUTHORIZED

class JsonObjectsSpec extends SpecHelpers {

  "AccessToken" must {
    "decode" in {
      decode[AccessToken]("""{"access_token" : "123"}""") mustBe Right(AccessToken(accessToken = "123"))
    }
  }

  "Error" must {
    "decode" in {
      decode[Error]("""{"error": {"status": 401, "message": "hi"}}""") mustBe Right(Error(UNAUTHORIZED, "hi"))
    }
  }
}
