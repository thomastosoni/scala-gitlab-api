package models


import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Issue(title: Option[String] = None,
                 description: Option[String] = None,
                 assigneeId: Option[String] = None,
                 milestoneId: Option[String] = None,
                 labels: Option[String] = None,
                 stateEvent: Option[String] = None)

object Issue {
  implicit val issueReader: Reads[Issue] = (
    (__ \ "title").read[Option[String]] and
      (__ \ 'description).read[Option[String]] and
      (__ \ 'assigneeId).read[Option[String]] and
      (__ \ 'milestoneId).read[Option[String]] and
      (__ \ 'labels).read[Option[String]] and
      (__ \ 'state_event).read[Option[String]]
    )(Issue.apply _)
}
