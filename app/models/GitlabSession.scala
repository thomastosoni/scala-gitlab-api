package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class GitlabSession(login: String,
                   email: Option[String],
                   password: String)

object GitlabSession {
  implicit val gitlabUserReader: Reads[GitlabSession] = (
    (__ \ "login").read[String] and
      (__ \ 'email).read[Option[String]] and
      (__ \ 'password).read[String]
    )(GitlabSession.apply _)
}
