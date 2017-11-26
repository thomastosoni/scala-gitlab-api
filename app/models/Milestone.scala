package models

case class Milestone(title: String,
                     description: Option[String],
                     dueDate: Option[String],
                     stateEvent: Option[String])