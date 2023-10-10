package models

import io.circe._
import io.circe.generic.semiauto._

abstract class JsonResponse[T] {
  implicit val decoder: Decoder[T]
}
case class AccessToken(accessToken: String)

object AccessToken extends JsonResponse[AccessToken] {
  implicit val decoder: Decoder[AccessToken] = Decoder.instance { h =>
    for {
      token <- h.get[String]("access_token")
    } yield AccessToken(accessToken = token)
  }
}

//TODO consider rename
//TODO consider renaming error to errorDetails and customizing the decoder
case class Error(error: ErrorDetails)

object Error extends JsonResponse[Error] {
  implicit val decoder: Decoder[Error] = deriveDecoder[Error]
}

case class ErrorDetails(status: Int, message: String)

object ErrorDetails extends JsonResponse[ErrorDetails] {
  implicit val decoder: Decoder[ErrorDetails] = deriveDecoder[ErrorDetails]
}

case class Artist(name: String, images: Seq[Image])

object Artist extends JsonResponse[Artist] {

  implicit val decoder: Decoder[Artist] = deriveDecoder[Artist]
}

case class Image(height: Int, url: String, width: Int)

object Image extends JsonResponse[Image] {
  implicit val decoder: Decoder[Image] = deriveDecoder[Image]
}

case class ArtistList(items: Seq[Artist])

object ArtistList extends JsonResponse[ArtistList] {
  implicit val decoder: Decoder[ArtistList] = deriveDecoder[ArtistList]
}

case class Track(id: String, name: String, artists: Seq[ArtistLite], album: Album)

object Track extends JsonResponse[Track] {
  implicit val decoder: Decoder[Track] = deriveDecoder[Track]
}

case class ArtistLite(name: String)

object ArtistLite {
  implicit val decoder: Decoder[ArtistLite] = deriveDecoder[ArtistLite]
}

case class Album(images: Seq[Image])

object Album extends JsonResponse[Album] {
  implicit val decoder: Decoder[Album] = deriveDecoder[Album]
}

case class TrackList(items: Seq[Track])

object TrackList extends JsonResponse[TrackList] {
  implicit val decoder: Decoder[TrackList] = deriveDecoder[TrackList]
}

case class Recommendations(seeds: Seq[RecommendationSeed], tracks: Seq[Track])

object Recommendations extends JsonResponse[Recommendations] {
  implicit val decoder: Decoder[Recommendations] = deriveDecoder[Recommendations]
}

case class RecommendationSeed(afterFilteringSize: Int, afterRelinkingSize: Int, id: String, initialPoolSize: Int)

object RecommendationSeed extends JsonResponse[RecommendationSeed] {
  implicit val decoder: Decoder[RecommendationSeed] = deriveDecoder[RecommendationSeed]
}
