package spechelpers

import io.circe.generic.auto._
import io.circe.syntax._
import models._
object TestData {

  lazy val trackList = TrackList(items =
    Seq(
      Track(
        id = "Id",
        name = "song",
        artists = Seq(ArtistLite("artist")),
        album = Album(images = Seq(Image(height = 1, url = "...", width = 1)))
      )
    )
  )
  lazy val trackListJson = trackList.asJson.noSpaces
}
