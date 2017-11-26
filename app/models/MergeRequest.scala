package models


case class MergeRequest(sourceBranch: String,
                        targetBranch: String,
                        title: String,
                        assigneeId: Option[Int] = None,
                        targetProjectId: Option[Int] = None,
                        stateEvent: Option[String] = None)