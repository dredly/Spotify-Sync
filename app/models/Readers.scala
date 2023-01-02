package models

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._


object Readers {
  implicit val playlistReads: Reads[Playlist] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "uri").read[String] and
      (JsPath \ "tracks" \ "href").read[String]
  )(Playlist.apply _)

  implicit val myPlaylistsResponseReads: Reads[MyPlaylistsResponse] = (
    (JsPath \ "href").read[String] and (JsPath \ "items").read[Array[Playlist]]
  )(MyPlaylistsResponse.apply _)
}
