package models

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