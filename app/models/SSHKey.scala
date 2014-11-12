package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SSHKey(title: String, key: String)

object SSHKey {
  def SSHKeyReader: Reads[SSHKey]  = (
    (__ \ "title").read[String] and
      (__ \ 'key).read[String]
    )(SSHKey.apply _)
}
