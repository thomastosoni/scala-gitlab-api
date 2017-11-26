package models

case class Hook(url: String,
                projectId: Int,
                pushEvents: Option[Boolean] = Option(false),
                issuesEvents: Option[Boolean] = Option(false),
                mergeRequests_events: Option[Boolean] = Option(false),
                tagPushEvents: Option[Boolean] = Option(false))