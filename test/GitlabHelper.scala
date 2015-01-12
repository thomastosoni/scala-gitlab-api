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
  lazy val logger = Logger("GitlabHelper")

  // Waiting time for project setup branches and commits in seconds
  val branchesImportMaxWaitingTime = 60
  val commitsImportMaxWaitingTime = 10

  // PROJECT
  val projectName = "test_project"
  var projectId = -1

  // USER
  var userId = -1
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
      case _ => throw new UnsupportedOperationException("Invalid status: " + response.status + ", json response: " + response.json)
    }
  }

  def statusCheck(response: WSResponse, objectName: String, id: Int): Unit = {
    response.status match {
      case 200 =>
        if (response.json.toString() != "null") {
          logger.debug(objectName + " (id: " + id + ") successfully removed. Response: " + response.json)
        }
      case 404 => logger.debug("404 Note Found: " + objectName + " (id: " + id + ") must have been removed by tests")
      case 400 => logger.debug("400 Bad Request: " + response.json)
      case _ => throw new UnsupportedOperationException("Invalid status: " + response.status + ", json response: " + response.json)
    }
  }

  /**
   * User
   */

  def createTestUser: Int = {
    try {
      val response = await(gitlabAPI.createUser(email, password, userUsername, userName, None))
      userId = (response.json \ "id").as[Int]
      logger.debug("Created Test User: " + response.json.toString())
      userId
    } catch {
      case e: Throwable => logger.error("Couldn't setup Test User for testing"); -1
    }
  }

  def deleteTestUser(): Unit = {
    try {
      val response = await(gitlabAPI.deleteUser(userId))
      statusCheck(response, "User", userId)
    } catch {
      case e: Throwable => logger.error("Couldn't delete Test User " + e);
    }
  }

  /**
   * Project
   */

  def waitForProjectSetup(projectId: Int): Unit = {
    logger.debug("Waiting for project: " + depot + " to be imported...")
    var repeat = branchesImportMaxWaitingTime / 10
    var branches: Seq[String] = Seq.empty
    var commits: Seq[String] = Seq.empty

    // Wait For Branches (x tries, each try waits for 10 seconds)
    while (branches.isEmpty && repeat > 0) {
      Thread.sleep(10000L)
      val branchesResponse = await(gitlabAPI.getBranches(projectId))
      if (branchesResponse.status == 200)
        branches = (branchesResponse.json \\ "id").map(_.as[String])
      if (branches.isEmpty)
        repeat -= 1
    }
    if (branches.isEmpty)
      throw new NoSuchElementException("Missing branches for project loaded from: " + depot + ". Maybe it needs more loading time.")

    // Wait For Commits (x tries, each try waits for 10 seconds)
    repeat = commitsImportMaxWaitingTime / 10
    while (commits.isEmpty && repeat > 0) {
      Thread.sleep(10000L)
      val commitsResponse = await(gitlabAPI.getCommits(projectId))
      if (commitsResponse.status == 200)
        commits = (commitsResponse.json \\ "id").map(_.as[String])
      if (commits.isEmpty)
        repeat -= 1
    }
    if (commits.isEmpty)
      throw new NoSuchElementException("Missing commits for project loaded from: " + depot + "Maybe it needs more loading time.")
  }

  def createTestProject: Int = {
    try {
      val response = await(gitlabAPI.createProject(GitlabHelper.projectName, importUrl = Option(depot)))
      projectId = (response.json \ "id").as[Int]
      waitForProjectSetup(projectId)
      logger.debug("Created Test Project: " + response.json.toString())
      return projectId
    } catch {
      case e: NoSuchElementException => logger.error(e.toString)
        deleteTestProject()
        deleteTestSSHKey()
      case e: RuntimeException => logger.error(e.toString)
        deleteTestProject()
        deleteTestSSHKey()
      case e: Throwable => logger.error("Couldn't setup Test Project for testing: " + e.toString)
        deleteTestProject()
        deleteTestSSHKey()
    }
    -1
  }

  def createEmptyTestProject: Int = {
    try {
      val response = await(gitlabAPI.createProject(GitlabHelper.projectName))
      logger.debug("Created Empty Test Project: " + response.json.toString())
      projectId = (response.json \ "id").as[Int]
      return projectId
    } catch {
      case e: Throwable =>
        logger.error("Couldn't setup Empty Test Project for testing: " + e.toString)
        deleteTestProject()
        deleteTestSSHKey()
    }
    -1
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
   * SSH Key
   */

  def createTestSSHKey: Int = {
    try {
      val response = await(gitlabAPI.addSSHKey("test_ssh_key", sshKey))
      logger.debug("Created Test SSHKey: " + response.json.toString())
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
