package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Project(name: String,
                   path: Option[String] = None,
                   namespaceId: Option[Int] = None,
                   description: Option[String] = None,
                   issuesEnabled: Boolean = false,
                   mergeRequestsEnabled: Boolean = false,
                   wikiEnabled: Boolean = false,
                   snippetsEnabled: Boolean = false,
                   public: Boolean = false,
                   visibilityLevel: Option[Int] = None,
                   importUrl: Option[String] = None)

object Project  {
  implicit val gitlabUserReader: Reads[Project] = (
    (__ \ "name").read[String] and
      (__ \ 'path).read[Option[String]] and
      (__ \ 'namespace_id).read[Option[Int]] and
      (__ \ 'description).read[Option[String]] and
      (__ \ 'issues_enabled).read[Boolean] and
      (__ \ 'merge_requests_enabled).read[Boolean] and
      (__ \ 'wiki_enabled).read[Boolean] and
      (__ \ 'snippets_enabled).read[Boolean] and
      (__ \ 'public).read[Boolean] and
      (__ \ 'visibility_level).read[Option[Int]] and
      (__ \ 'import_url).read[Option[String]]
    )(Project.apply _)
}
