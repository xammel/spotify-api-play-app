package models

import io.circe._
import io.circe.generic.semiauto._
import play.api.data.Form
import play.api.data.Forms._

case class AccessToken(access_token: String)

object AccessToken {
  implicit val accessTokenDecoder: Decoder[AccessToken] = deriveDecoder[AccessToken]
}

case class ArtistId(artistId: String)

object ArtistId {
  val artistIdForm = Form(
    mapping("artistId" -> text)(ArtistId.apply)(ArtistId.unapply)
  )
}

// { "error": { "status": 401, "message": "Invalid access token" } }
case class Error(error: ErrorDetails)

object Error {
  implicit val errorDecoder: Decoder[Error] = deriveDecoder[Error]
}

case class ErrorDetails(status: Int, message: String)

object ErrorDetails {
  implicit val errorDetailsDecoder: Decoder[ErrorDetails] = deriveDecoder[ErrorDetails]
}

// {
//  "external_urls": {
//    "spotify": "https://open.spotify.com/artist/6fxk3UXHTFYET8qCT9WlBF"
//  },
//  "followers": {
//    "href": null,
//    "total": 279275
//  },
//  "genres": [
//    "chamber pop",
//    "indie rock",
//    "kc indie",
//    "modern folk rock"
//  ],
//  "href": "https://api.spotify.com/v1/artists/6fxk3UXHTFYET8qCT9WlBF",
//  "id": "6fxk3UXHTFYET8qCT9WlBF",
//  "images": [
//    {
//      "height": 640,
//      "url": "https://i.scdn.co/image/ab6761610000e5eb292b964365b5de1a53216852",
//      "width": 640
//    },
//    {
//      "height": 320,
//      "url": "https://i.scdn.co/image/ab67616100005174292b964365b5de1a53216852",
//      "width": 320
//    },
//    {
//      "height": 160,
//      "url": "https://i.scdn.co/image/ab6761610000f178292b964365b5de1a53216852",
//      "width": 160
//    }
//  ],
//  "name": "Kevin Morby",
//  "popularity": 50,
//  "type": "artist",
//  "uri": "spotify:artist:6fxk3UXHTFYET8qCT9WlBF"
//}
case class ArtistDetails(name: String, popularity: Int)

object ArtistDetails {
  implicit val artistDetailsDecoder: Decoder[ArtistDetails] = deriveDecoder[ArtistDetails]
}

case class ArtistList(items: Seq[ArtistDetails])

object ArtistList {
  implicit val artistListDecoder: Decoder[ArtistList] = deriveDecoder[ArtistList]
}