package utils

import org.scalatestplus.play._

import scala.collection.mutable
import utils.AuthorizationMethods._
import utils.StringConstants.lengthOfCodeVerifier
import play.api.test.Helpers._
class AuthorizationMethodsSpec extends PlaySpec {

  "generateRandomString" must {
    s"generate a string of length ${lengthOfCodeVerifier}" in {
      generateRandomString.length mustBe lengthOfCodeVerifier
    }
  }

}
