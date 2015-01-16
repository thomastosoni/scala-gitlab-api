package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class User(email: String,
                password: String,
                username: String,
                name: String,
                skype: Option[String] = None,
                linkedin: Option[String] = None,
                twitter: Option[String] = None,
                websiteUrl: Option[String] = None,
                projectsLimit: Option[Int] = None,
                externUid: Option[String] = None,
                provider: Option[String] = None,
                bio: Option[String] = None,
                admin: Option[Boolean] = None,
                canCreateGroup: Option[Boolean] = None)

object User {
  implicit val gitlabUserWriter: Writes[User] = (
    (__ \ "email").write[String] and
      (__ \ 'password).write[String] and
      (__ \ 'username).write[String] and
      (__ \ 'name).write[String] and
      (__ \ 'skype).write[Option[String]] and
      (__ \ 'linkedin).write[Option[String]] and
      (__ \ 'twitter).write[Option[String]] and
      (__ \ 'website_url).write[Option[String]] and
      (__ \ 'projects_limit).write[Option[Int]] and
      (__ \ 'extern_uid).write[Option[String]] and
      (__ \ 'provider).write[Option[String]] and
      (__ \ 'bio).write[Option[String]] and
      (__ \ 'admin).write[Option[Boolean]] and
      (__ \ 'can_create_group).write[Option[Boolean]]
    )(unlift(User.unapply))

  implicit val gitlabUserReader: Reads[User] = (
    (__ \ "email").read[String] and
      (__ \ 'password).read[String] and
      (__ \ 'username).read[String] and
      (__ \ 'name).read[String] and
      (__ \ 'skype).read[Option[String]] and
      (__ \ 'linkedin).read[Option[String]] and
      (__ \ 'twitter).read[Option[String]] and
      (__ \ 'website_url).read[Option[String]] and
      (__ \ 'projects_limit).read[Option[Int]] and
      (__ \ 'extern_uid).read[Option[String]] and
      (__ \ 'provider).read[Option[String]] and
      (__ \ 'bio).read[Option[String]] and
      (__ \ 'admin).read[Option[Boolean]] and
      (__ \ 'can_create_group).read[Option[Boolean]]
    )(User.apply _)
}
