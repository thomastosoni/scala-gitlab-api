package models

case class GitlabSession(login: String,
                         email: Option[String],
                         password: String)