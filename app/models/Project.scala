package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Project(name: String,
                   path: Option[String] = None,
                   namespaceId: Option[Int] = None,
                   description: Option[String] = None,
                   issuesEnabled: Option[Boolean] = None,
                   mergeRequestsEnabled: Option[Boolean] = None,
                   wikiEnabled: Option[Boolean] = None,
                   snippetsEnabled: Option[Boolean] = None,
                   public: Option[Boolean] = None,
                   visibilityLevel: Option[Int] = None,
                   importUrl: Option[String] = None)

object Project  {
  implicit val gitlabUserReader: Reads[Project] = (
    (__ \ "name").read[String] and
      (__ \ 'path).read[Option[String]] and
      (__ \ 'namespace_id).read[Option[Int]] and
      (__ \ 'description).read[Option[String]] and
      (__ \ 'issues_enabled).read[Option[Boolean]] and
      (__ \ 'merge_requests_enabled).read[Option[Boolean]] and
      (__ \ 'wiki_enabled).read[Option[Boolean]] and
      (__ \ 'snippets_enabled).read[Option[Boolean]] and
      (__ \ 'public).read[Option[Boolean]] and
      (__ \ 'visibility_level).read[Option[Int]] and
      (__ \ 'import_url).read[Option[String]]
    )(Project.apply _)
}
