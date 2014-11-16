package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Hook(url: String,
                projectId: Int,
                pushEvents: Option[Boolean] = Option(false),
                issuesEvents: Option[Boolean] = Option(false),
                mergeRequests_events: Option[Boolean] = Option(false),
                tagPushEvents: Option[Boolean] = Option(false))

object Hook {
  implicit val hookReader: Reads[Hook] = (
  (__ \ "url").read[String] and
    (__ \ 'project_id).read[Int] and
    (__ \ 'push_events).read[Option[Boolean]] and
    (__ \ 'issues_events).read[Option[Boolean]] and
    (__ \ 'merge_requests).read[Option[Boolean]] and
    (__ \ 'tag_push_events).read[Option[Boolean]]
  )(Hook.apply _)
}
