package models

import play.api.libs.json._
import play.api.libs.functional.syntax._


case class MergeRequest(sourceBranch: String,
                        targetBranch: String,
                        title: String,
                        assigneeId: Option[Int] = None,
                        targetProjectId: Option[Int] = None,
                        stateEvent: Option[String] = None)

object MergeRequest {
  implicit val mergeRequestReader: Reads[MergeRequest] = (
    (__ \ "source_branch").read[String] and
      (__ \ 'target_branch).read[String] and
      (__ \ 'title).read[String] and
      (__ \ 'assignee_id).read[Option[Int]] and
      (__ \ 'target_project_id).read[Option[Int]] and
      (__ \ 'state_event).read[Option[String]]
    )(MergeRequest.apply _)
}
