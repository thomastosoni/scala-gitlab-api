package models

case class Tag(tagName: String,
               ref: String,
               message: Option[String])