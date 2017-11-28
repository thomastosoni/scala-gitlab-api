package models

import play.api.libs.json.{Reads, Writes}
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
//
//object User {
//  implicit val gitlabUserWriter: Writes[User] = (
//    (__ \ "email").write[String] and
//      (__ \ 'password).write[String] and
//      (__ \ 'username).write[String] and
//      (__ \ 'name).write[String] and
//      (__ \ 'skype).write[Option[String]] and
//      (__ \ 'linkedin).write[Option[String]] and
//      (__ \ 'twitter).write[Option[String]] and
//      (__ \ 'website_url).write[Option[String]] and
//      (__ \ 'projects_limit).write[Option[Int]] and
//      (__ \ 'extern_uid).write[Option[String]] and
//      (__ \ 'provider).write[Option[String]] and
//      (__ \ 'bio).write[Option[String]] and
//      (__ \ 'admin).write[Option[Boolean]] and
//      (__ \ 'can_create_group).write[Option[Boolean]]
//    )(unlift(User.unapply))
//
//  implicit val gitlabUserReader: Reads[User] = (
//    (__ \ "email").read[String] and
//      (__ \ 'password).read[String] and
//      (__ \ 'username).read[String] and
//      (__ \ 'name).read[String] and
//      (__ \ 'skype).readNullable[String] and
//      (__ \ 'linkedin).readNullable[String] and
//      (__ \ 'twitter).readNullable[String] and
//      (__ \ 'website_url).readNullable[String] and
//      (__ \ 'projects_limit).readNullable[Int] and
//      (__ \ 'extern_uid).readNullable[String] and
//      (__ \ 'provider).readNullable[String] and
//      (__ \ 'bio).readNullable[String] and
//      (__ \ 'admin).readNullable[Boolean] and
//      (__ \ 'can_create_group).readNullable[Boolean]
//    )(User.apply _)
//}