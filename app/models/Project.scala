package models

case class Project(name: String,
                   path: Option[String] = None,
                   namespaceId: Option[Int] = None,
                   description: Option[String] = None,
                   issuesEnabled: Option[Boolean] = None,
                   mergeRequestsEnabled: Option[Boolean] = None,
                   wikiEnabled: Option[Boolean] = None,
                   snippetsEnabled: Option[Boolean] = None,
                   public: Option[Boolean] = None,
                   visibilityLevel: Option[Int] = None,
                   importUrl: Option[String] = None)