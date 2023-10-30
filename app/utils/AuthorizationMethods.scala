package utils

import utils.StringConstants.{lengthOfCodeVerifier, sha256}

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import scala.util.Random

object AuthorizationMethods {

  def generateRandomString: String = {
    Random.alphanumeric.take(lengthOfCodeVerifier).mkString("")
  }

  def base64Encode(bytes: Array[Byte]): String = {
    Base64.getUrlEncoder.withoutPadding
      .encodeToString(bytes)
      .replace('+', '-')
      .replace('/', '_')
  }

  def generateCodeChallenge(codeVerifier: String): String = {
    val encoder: MessageDigest = MessageDigest.getInstance(sha256)
    val codeVerifierBytes: Array[Byte] = codeVerifier.getBytes(StandardCharsets.UTF_8)
    val data: Array[Byte] = encoder.digest(codeVerifierBytes)
    base64Encode(data)
  }

}
