package models

import io.circe._
import io.circe.generic.semiauto._
import play.api.data.Form
import play.api.data.Forms._

trait JsonResponse[T] {
  implicit val decoder: Decoder[T]
  def convertToStringSeq(data: T): Seq[String]
}

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

case class Artist(name: String, id: String)

object Artist extends JsonResponse[Artist] {

  implicit val decoder: Decoder[Artist] = deriveDecoder[Artist]

  override def convertToStringSeq(artist: Artist): Seq[String] = Seq(s"Name: ${artist.name}, ID: ${artist.id}")
}

case class ArtistList(items: Seq[Artist])

object ArtistList extends JsonResponse[ArtistList] {

  implicit val decoder: Decoder[ArtistList] = deriveDecoder[ArtistList]

  def convertToStringSeq(artists: ArtistList): Seq[String] =
    artists.items.flatMap(Artist.convertToStringSeq)
}

case class Track(name: String, artists: Seq[Artist], id: String)

object Track extends JsonResponse[Track] {
  implicit val decoder: Decoder[Track] = deriveDecoder[Track]

  def convertToStringSeq(track: Track): Seq[String] =
    Seq(s"${track.name} by ${track.artists.map(_.name).mkString(" & ")}")
}

case class TrackList(items: Seq[Track])

object TrackList extends JsonResponse[TrackList] {
  implicit val decoder: Decoder[TrackList] = deriveDecoder[TrackList]

  def convertToStringSeq(trackList: TrackList): Seq[String] = trackList.items.flatMap(Track.convertToStringSeq)
}

case class Recommendations(seeds: Seq[RecommendationSeed], tracks: Seq[Track]) // seeds: Seq[RecommendationSeedObject],

object Recommendations extends JsonResponse[Recommendations] {
  implicit val decoder: Decoder[Recommendations] = deriveDecoder[Recommendations]

  override def convertToStringSeq(data: Recommendations): Seq[String] =
    data.seeds.flatMap(RecommendationSeed.convertToStringSeq) ++ data.tracks.flatMap(Track.convertToStringSeq)
}

case class RecommendationSeed(afterFilteringSize: Int, afterRelinkingSize: Int, id: String, initialPoolSize: Int)

object RecommendationSeed extends JsonResponse[RecommendationSeed] {
  override implicit val decoder: Decoder[RecommendationSeed] = deriveDecoder[RecommendationSeed]

  override def convertToStringSeq(data: RecommendationSeed): Seq[String] =
    Seq(
      s"afterFilteringSize: ${data.afterFilteringSize}, afterRelinkingSize: ${data.afterRelinkingSize}, id: ${data.id}, initialPoolSize: ${data.initialPoolSize}"
    )
}
