import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

object GitlabHelper {
  lazy val conf = ConfigFactory.load()
  lazy val depot = conf.getString("gitlab.testing-depot")
  lazy val gitlabUrl = conf.getString("gitlab.url")
  lazy val gitlabToken = conf.getString("gitlab.token")
  lazy val sshKey = conf.getString("gitlab.ssh-key")
  lazy val logger = Logger("GitlabAPITest")

  // Static testing values
  // PROJECT
  val projectName = "test_project"

  // USER
  val userName = "test_name"
  val userUsername = "test_username"
  val email = "test@gitlabtest.com"
  val password = "test_password"

  // SYSTEM HOOKS
  val systemHookUrl = "http://localhost:8000"

  def statusCheck(response: WSResponse, objectName: String, id: Int): Unit = {
    response.status match {
      case 200 =>
        if (response.json.toString() != "null") {
          logger.error(objectName + " (id: " + id + ") successfully removed, but it should have been removed by tests. Response: " + response.json)
        }
      case 404 => logger.debug(objectName + " (id: " + id + ") not found, must have been removed by tests")
      case _ => throw new UnsupportedOperationException
    }
  }

  def createProject: Int = {
    var projectId = 0
    try {
      val response = await(gitlabAPI.createProject(GitlabHelper.projectName, importUrl = Option(depot)))
      logger.debug("Waiting for project: " + depot + " to be imported...")
      Thread.sleep(30000L)
      logger.debug("Created project: " + response.json.toString())
      projectId = (response.json \ "id").as[Int]
    } catch {
      case e: Throwable => logger.error("Couldn't setup testing environment for testing")
    }
    logger.debug("Base project created. Project id: " + projectId)
    projectId
  }

  lazy val gitlabAPI = new GitlabAPI(gitlabUrl, gitlabToken)
}

object && {
  def unapply[A](a: A) = Some((a, a))
}
