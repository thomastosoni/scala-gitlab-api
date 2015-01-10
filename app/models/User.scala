package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class User(email: String,
                password: String,
                username: String,
                name: String,
                admin: Option[Boolean])

object User {
  implicit val gitlabUserWriter: Writes[User] = (
    (__ \ "email").write[String] and
      (__ \ 'password).write[String] and
      (__ \ 'username).write[String] and
      (__ \ 'name).write[String] and
      (__ \ 'admin).write[Option[Boolean]]
    )(unlift(User.unapply))

  implicit val gitlabUserReader: Reads[User] = (
    (__ \ "email").read[String] and
      (__ \ 'password).read[String] and
      (__ \ 'username).read[String] and
      (__ \ 'name).read[String] and
      (__ \ 'admin).read[Option[Boolean]]
    )(User.apply _)
}
