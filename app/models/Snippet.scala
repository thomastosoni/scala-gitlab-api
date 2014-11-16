package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Snippet(title: String,
                   fileName: String,
                   code: String)

object Snippet {
  implicit val snippetReader: Reads[Snippet] = (
    (__ \ "title").read[String] and
      (__ \ 'fileName).read[String] and
      (__ \ 'code).read[String]
    )(Snippet.apply _)
}
