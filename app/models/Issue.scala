package models

case class Issue(title: Option[String] = None,
                 description: Option[String] = None,
                 assigneeId: Option[String] = None,
                 milestoneId: Option[String] = None,
                 labels: Option[String] = None,
                 stateEvent: Option[String] = None)