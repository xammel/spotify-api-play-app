package models

import io.circe._
import io.circe.generic.semiauto._
import play.api.data.Form
import play.api.data.Forms._

trait JsonResponse[T] {
  implicit val decoder: Decoder[T]
  def convertToString(data: T): String
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

case class ArtistDetails(name: String, popularity: Int)

object ArtistDetails extends JsonResponse[ArtistDetails] {

  implicit val decoder: Decoder[ArtistDetails] = deriveDecoder[ArtistDetails]

  def convertToString(artist: ArtistDetails): String = s"Name: ${artist.name}, Popularity: ${artist.popularity}"
}

case class ArtistList(items: Seq[ArtistDetails])

object ArtistList extends JsonResponse[ArtistList] {

  implicit val decoder: Decoder[ArtistList] = deriveDecoder[ArtistList]

  def convertToString(artists: ArtistList): String =
    artists.items.map(ArtistDetails.convertToString).mkString(" | ")
}

case class Track(name: String)

object Track extends JsonResponse[Track] {
  implicit val decoder: Decoder[Track] = deriveDecoder[Track]

  def convertToString(track: Track): String = s"Track Name: ${track.name}"
}

case class TrackList(items: Seq[Track])

object TrackList extends JsonResponse[TrackList] {
  implicit val decoder: Decoder[TrackList] = deriveDecoder[TrackList]

  def convertToString(trackList: TrackList): String = trackList.items.map(Track.convertToString).mkString(" | ")
}
