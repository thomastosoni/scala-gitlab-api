package models


import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Milestone(title: String,
                     description: Option[String],
                     dueDate: Option[String],
                     stateEvent: Option[String])

object Milestone {
  implicit val milestoneReader: Reads[Milestone] = (
    (__ \ "title").read[String] and
      (__ \ 'description).read[Option[String]] and
      (__ \ 'due_date).read[Option[String]] and
      (__ \ 'state_event).read[Option[String]]
    )(Milestone.apply _)
}
