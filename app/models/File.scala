package models


import play.api.libs.json._
import play.api.libs.functional.syntax._

case class File(filePath: String,
                 branchName: String,
                 content: String,
                 commitMessage: String,
                 encoding: Option[String] = "text")

object File {
  implicit val fileReader: Reads[File] = (
    (__ \ "file_path").read[String] and
      (__ \ 'branch_name).read[String] and
      (__ \ 'content).read[String] and
      (__ \ 'commit_message).read[String] and
      (__ \ 'encoding).read[Option[String]]
    )(File.apply _)
}
