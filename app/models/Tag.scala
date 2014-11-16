package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Tag(tagName: String,
               ref: String,
               message: Option[String])

object Tag {
  implicit val tagReader: Reads[Tag] = (
    (__ \ "tag_name").read[String] and
      (__ \ 'ref).read[String] and
      (__ \ 'message).read[Option[String]]
    )(Tag.apply _)
}
