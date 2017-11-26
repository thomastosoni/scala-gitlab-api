import com.typesafe.config.{Config, ConfigFactory}
import play.api.Logger
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.api.test.WsTestClient

object GitlabHelper {
  lazy val conf: Config = ConfigFactory.load()
  lazy val sshKey: String = conf.getString("gitlab.test-ssh-key")
  lazy val depot: String = conf.getString("gitlab.test-repository")
  lazy val gitlabUrl: String = conf.getString("gitlab.url")
  lazy val gitlabToken: String = conf.getString("gitlab.token")
  lazy val logger: Logger = Logger("GitlabHelper")

  implicit def withGitlabAPI[T](block: GitlabAPI => T): T = {
    WsTestClient.withClient { client =>
      block(new GitlabAPI(client, gitlabUrl, gitlabToken))
    }
  }

  // Waiting time for project setup branches and commits in seconds
  val branchesImportMaxWaitingTime: Int = 40
  val commitsImportMaxWaitingTime: Int = 10

  // PROJECT
  val projectName: String = "test_project"
  var projectId: Int = -1

  // USER
  var userId: Int = -1
  val userName: String = "test_name"
  val userUsername: String = "test_username"
  val email: String = "test@gitlabtest.com"
  val password: String = "test_password"

  // SYSTEM HOOKS
  val systemHookUrl: String = "http://localhost:8000"

  // SSH KEY
  var sshKeyId: Int = -1

  private def statusCheck(response: WSResponse, resource: Resource): Unit = {
    response.status match {
      case 200 => logger.debug("200 OK: " + resource + ". Response: " + response.json)
      case 201 => logger.debug("201 Created: " + resource + ". Response: " + response.json)
      case 400 => logger.debug("400 Bad Request: " + resource + ". Response: " + response.json)
      case 401 => logger.debug("400 Unauthorized: " + resource + ". Response: " + response.json)
      case 403 => logger.debug("403 Forbidden: " + resource + ". Response: " + response.json)
      case 404 => logger.debug("404 Not Found: " + resource + ". Must have been removed by tests. Response: " + response.json)
      case 405 => logger.debug("405 Not Allowed: " + resource + ". Response: " + response.json)
      case 409 => logger.debug("405 Conflict: " + resource + ". Response: " + response.json)
      case 422 => logger.debug("422 Unprocessable: " + resource + ". Response: " + response.json)
      case 500 => logger.debug("500 Unprocessable: " + resource + ". Response: " + response.json)
    }
  }

  def checkDeleteAfterTest(response: WSResponse, resource: Resource): Unit = {
    if (response.status == 200 && response.json.toString() != "null") {
      logger.error(resource + " has been deleted, but it should have been done during the tests.")
    } else {
      statusCheck(response, resource)
    }
  }

  /**
    * User
    */

  def createTestUser(implicit gitlabAPI: GitlabAPI): Int = {
    try {
      val response = await(gitlabAPI.createUser(email, password, userUsername, userName))
      userId = (response.json \ "id").as[Int]
      logger.debug("Created Test User: " + response.json.toString())
      userId
    } catch {
      case e: Throwable => logger.error("Couldn't setup Test User for testing"); -1
    }
  }

  def deleteTestUser(implicit gitlabAPI: GitlabAPI): Unit = {
    try {
      val response = await(gitlabAPI.deleteUser(userId))
      statusCheck(response, USER)
    } catch {
      case e: Throwable => logger.error("Couldn't delete Test User " + e);
    }
  }

  /**
    * Project
    */

  def waitForProjectSetup(projectId: Int)(implicit gitlabAPI: GitlabAPI): Unit = {
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

  // TODO Refactor createTestProject and createEmptyTestProject. Check if test project exist. Generate name. Handle kill.
  def createTestProject(implicit gitlabAPI: GitlabAPI): Int = {
    try {
      val response = await(gitlabAPI.createProject(projectName, importUrl = Option(depot)))
      if (response.status == 201) {
        projectId = (response.json \ "id").as[Int]
        waitForProjectSetup(projectId)
        logger.debug("Created Test Project: " + response.json.toString())
      } else throw new Throwable("Couldn't setup Test Project for testing")
      return projectId
    } catch {
      case e: NoSuchElementException => logger.error(e.toString)
      case e: RuntimeException => logger.error(e.toString)
      case e: Throwable => logger.error(e.toString)
    }
    deleteTestProject()
    deleteTestSSHKey()
    -1
  }

  def createEmptyTestProject(implicit gitlabAPI: GitlabAPI): Int = {
    try {
      val response = await(gitlabAPI.createProject(projectName))
      if (response.status == 201) {
        logger.debug("Created Empty Test Project: " + response.json.toString())
        projectId = (response.json \ "id").as[Int]
      } else throw new Throwable("Couldn't setup Test Project for testing")
      return projectId
    } catch {
      case e: Throwable => logger.error(e.toString)
    }
    deleteTestProject()
    deleteTestSSHKey()
    -1
  }

  def deleteTestProject()(implicit gitlabAPI: GitlabAPI): Unit = {
    val response = await(gitlabAPI.deleteProject(projectId))
    statusCheck(response, PROJECT)
  }

  /**
    * SSH Key
    */

  def createTestSSHKey(implicit gitlabAPI: GitlabAPI): Int = {
    try {
      val response = await(gitlabAPI.addSSHKey("test_ssh_key", sshKey))
      if (response.status == 201) {
        logger.debug("Created Test SSHKey: " + response.json.toString())
        sshKeyId = (response.json \ "id").as[Int]
        return sshKeyId
      } else throw new Throwable("Couldn't create SSH Key for testing")
    } catch {
      case e: Throwable => logger.error(e.toString);
    }
    deleteTestProject()
    deleteTestSSHKey()
    -1
  }

  def deleteTestSSHKey()(implicit gitlabAPI: GitlabAPI): Unit = {
    val response = await(gitlabAPI.deleteSSHKey(sshKeyId))
    statusCheck(response, SSH_KEY)
  }
}
