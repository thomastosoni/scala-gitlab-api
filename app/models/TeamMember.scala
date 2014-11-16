package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class TeamMember(username: String,
                      email: String,
                      name: String,
                      state: String,
                      created_at: String,
                      access_level: Int)

object TeamMember {
  implicit val teamMemberReader: Reads[TeamMember] = (
    (__ \ "username").read[String] and
      (__ \ 'email).read[String] and
      (__ \ 'name).read[String] and
      (__ \ 'state).read[String] and
      (__ \ 'created_at).read[String] and
      (__ \ 'access_level).read[Int]
    )(TeamMember.apply _)
}
