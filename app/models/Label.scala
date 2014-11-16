package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Label(name: String,
                 color: String)

object Label {
  implicit val labelReader: Reads[Label] = (
    (__ \ "name").read[String] and
      (__ \'color).read[String]
    )(Label.apply _)
}
