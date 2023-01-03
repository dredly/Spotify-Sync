package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, text}

object PlaylistIdForm {
  case class PlaylistIdFormData(playlistId: String)

  object PlaylistIdFormData {
    val form: Form[PlaylistIdFormData] = Form(
      mapping(
        "playlistId" -> text
      )(PlaylistIdFormData.apply)(PlaylistIdFormData.unapply)
    )
  }
}
