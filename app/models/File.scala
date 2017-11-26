package models

case class File(filePath: String,
                branchName: String,
                content: String,
                commitMessage: String,
                encoding: Option[String] = None)