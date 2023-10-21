package utils

import org.scalatestplus.play._
import utils.AuthorizationMethods._
import utils.StringConstants.lengthOfCodeVerifier

class AuthorizationMethodsSpec extends PlaySpec {

  "generateRandomString" must {
    s"generate a string of length ${lengthOfCodeVerifier}" in {
      generateRandomString.length mustEqual lengthOfCodeVerifier
    }
  }
}
