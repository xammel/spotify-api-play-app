package models

import io.circe._
import io.circe.generic.semiauto._

case class AccessToken(access_token: String)

object AccessToken {
  implicit val accessTokenDecoder: Decoder[AccessToken] = deriveDecoder[AccessToken]
}
