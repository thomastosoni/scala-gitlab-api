import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.ws.{DefaultWSResponseHeaders, WSResponse}
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
  var projectId = -1

  // USER
  val userName = "test_name"
  val userUsername = "test_username"
  val email = "test@gitlabtest.com"
  val password = "test_password"

  // SYSTEM HOOKS
  val systemHookUrl = "http://localhost:8000"

  // SSH KEY
  var sshKeyId = -1

  def statusCheckError(response: WSResponse, objectName: String, id: Int): Unit = {
    response.status match {
      case 200 =>
        if (response.json.toString() != "null") {
          logger.error(objectName + " (id: " + id + ") successfully removed, but it should have been removed by tests. Response: " + response.json)
        }
      case 404 => logger.debug(objectName + " (id: " + id + ") not found, must have been removed by tests")
      case _ => throw new UnsupportedOperationException
    }
  }

  def statusCheck(response: WSResponse, objectName: String, id: Int): Unit = {
    response.status match {
      case 200 =>
        if (response.json.toString() != "null") {
          logger.debug(objectName + " (id: " + id + ") successfully removed. Response: " + response.json)
        }
      case 404 => logger.debug(objectName + " (id: " + id + ") not found, must have been removed by tests")
      case _ => throw new UnsupportedOperationException
    }
  }

  // TODO wait for real ending
  def waitForProjectSetup(projectId: Int): Unit = {
    logger.debug("Waiting for project: " + depot + " to be imported...")
    Thread.sleep(20000L)
    val json = await(gitlabAPI.getProject(projectId)).json
  }

  /**
   * Projects
   */

  def createTestProject: Int = {
    try {
      val response = await(gitlabAPI.createProject(GitlabHelper.projectName, importUrl = Option(depot)))
      projectId = (response.json \ "id").as[Int]
      waitForProjectSetup(projectId)
      logger.debug("Created project: " + response.json.toString())
      projectId
    } catch {
      case e: Throwable => logger.error("Couldn't setup Test Project for testing"); -1
    }
  }

  def createEmptyTestProject: Int = {
    try {
      val response = await(gitlabAPI.createProject(GitlabHelper.projectName))
      logger.debug("Created project: " + response.json.toString())
      projectId = (response.json \ "id").as[Int]
      projectId
    } catch {
      case e: Throwable => logger.error("Couldn't setup Empty Test Project for testing"); -1
    }
  }

  def deleteTestProject(): Unit = {
    try {
      val response = await(gitlabAPI.deleteProject(projectId))
      statusCheck(response, "Project", projectId)
    } catch {
      case e: Throwable => logger.error("Couldn't delete Test Project " + e);
    }
  }

  /**
   * SSH KEY
   */

  def createTestSSHKey: Int = {
    try {
      val response = await(gitlabAPI.addSSHKey("test_ssh_key", sshKey))
      logger.debug("Created SSHKey: " + response.json.toString())
      sshKeyId = (response.json \ "id").as[Int]
      sshKeyId
    } catch {
      case e: Throwable => logger.error("Couldn't create Test SSH Key " + e); -1
    }
  }

  def deleteTestSSHKey(): Unit = {
    try {
      val response = await(gitlabAPI.deleteSSHKey(sshKeyId))
      statusCheck(response, "SSH Key", sshKeyId)
    } catch {
      case e: Throwable => logger.error("Couldn't delete Test SSH Key " + e);
    }
  }

  lazy val gitlabAPI = new GitlabAPI(gitlabUrl, gitlabToken)
}
