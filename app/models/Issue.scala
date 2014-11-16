package models


import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Issue(title: String,
                 description: Option[String],
                 assigneeId: Option[String],
                 milestoneId: Option[String],
                 labels: Option[String],
                 stateEvent: Option[String])

object Issue {
  implicit val issueReader: Reads[Issue] = (
    (__ \ "title").read[String] and
      (__ \ 'description).read[Option[String]] and
      (__ \ 'assigneeId).read[Option[String]] and
      (__ \ 'milestoneId).read[Option[String]] and
      (__ \ 'labels).read[Option[String]] and
      (__ \ 'state_event).read[Option[String]]
    )(Issue.apply _)
}
