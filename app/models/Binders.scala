package models

import play.api.mvc.QueryStringBindable
//import play.mvc.QueryStringBindable

object Binders {

  //return the error message on the left should the parsing fail
  private def trackListFromString(s: String): Either[String, TrackList] = Right(TrackList(Seq.empty))

  private def trackListToString(trackList: TrackList): String = TrackList.convertToStringSeq(trackList).mkString(", ")

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[TrackList] = new QueryStringBindable[TrackList] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, TrackList]] = {
      for {
        eitherPropString <- stringBinder.bind("trackList", params)
      } yield {
        eitherPropString match {
          case Right(propString) => trackListFromString(propString)
          case _ => Left("Unable to bind trackList")
        }
      }
    }
    override def unbind(key: String, trackList: TrackList): String = {
      stringBinder.unbind("trackList", trackListToString(trackList))
    }
  }

}