import java.net.URL

import com.typesafe.config.ConfigFactory
import play.api.Logger

//TODO resolve all tests asynchronously & separately (ex: id search has to be revised)
import org.scalatestplus.play._
import play.api.test.Helpers._


//@RunWith(classOf[JUnitRunner])
class GitlabAPITest extends PlaySpec with OneAppPerSuite {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  //  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  lazy val conf = ConfigFactory.load()
  lazy val gitlabUrl = conf.getString("gitlab.url")
  lazy val gitlabToken = conf.getString("gitlab.token")
  lazy val sshKey = conf.getString("gitlab.ssh-key")
  lazy val logger = Logger("GitlabAPITest")

  lazy val gitlabAPI = new GitlabAPI(gitlabUrl, gitlabToken)

  //    sequential
  //    args(stopOnFail = true)

  "GitlabAPI" must {

    "not be able to connect with invalid credentials" in {
      await(gitlabAPI.connectToSession("INVALID", "INVALID")).status must be(401)
    }

    "use a valid API URL" in {
      val expected: URL = new URL(gitlabUrl + "/?private_token=" + gitlabToken)
      gitlabAPI.getAPIUrl("") must be(expected)
    }

    "Manage Users" should {
      val name = "test_name"
      val updatedName = "updated_name"
      val username = "test_username" // Unique
      val email = "test@gitlabtest.com" // Unique
      val password = "test_password"
      var userId = -1 // Shouldn't do that...

      "get all users" in {
        await(gitlabAPI.getUsers).status must be(200)
      }

      "create a user with name: " + name + ", username: " + username + " and email: " + email in {
        await(gitlabAPI.createUser(email, password, username, name, None)).status must be(201)
      }

      "find all users with email: " + email in {
        val response = await(gitlabAPI.getUserByEmail(email))
        response.status must be(200)
        val emailResult = (response.json \\ "email").map(_.as[String]).head
        val usernameResult = (response.json \\ "username").map(_.as[String]).head
        emailResult must be(email)
        usernameResult must be(username)
      }

      "find user by username: " + username + ", update its name to: " + updatedName + " and delete it" in {
        var response = await(gitlabAPI.getUserByUsername(username))
        response.status must be(200)
        val emailResult = (response.json \\ "email").map(_.as[String]).head
        userId = (response.json \\ "id").map(_.as[Int]).head
        emailResult must be(email)

        response = await(gitlabAPI.updateUser(userId, email, password, username, updatedName))
        response.status must be(200)

        response = await(gitlabAPI.getUser(userId))
        response.status must be(200)
        val usernameResult = (response.json \ "username").as[String]
        val nameResult = (response.json \ "name").as[String]
        usernameResult must be(username)
        nameResult must be(updatedName)

        response = await(gitlabAPI.deleteUser(userId))
        response.status must be(200)
        response.json must not be null
      }


      val sshKeyTitle = "ssh_key_title"
      "add, get and remove ssh keys" in {
        var response = await(gitlabAPI.addSSHKey(sshKeyTitle, sshKey))
        response.status must be(201)
        (response.json \ "title").as[String] must be(sshKeyTitle)
        val sshKeyId = (response.json \ "id").as[Int]

        response = await(gitlabAPI.getSSHKeys)
        response.status must be(200)

        response = await(gitlabAPI.getSSHKey(sshKeyId))
        response.status must be(200)
        (response.json \ "title").as[String] must be(sshKeyTitle)

        await(gitlabAPI.deleteSSHKey(sshKeyId)).status must be(200)
      }
    }

    "Manage Projects" should {
      val projectName = "test_project"

      "get all projects" in {
        await(gitlabAPI.getAllProjects).status must be(200)
      }

      "get owned projects" in {
        await(gitlabAPI.getOwnedProjects).status must be(200)
      }

      "create, fork, get and delete a project" in {
        var response = await(gitlabAPI.createProject(projectName))
        response.status must be(201)
        val projectId = (response.json \ "id").as[Int]

//        response = await(gitlabAPI.forkProject(projectId))
//        response.status must be(200)
//        (response.json \ "id").as[Int] must not be projectId

        response = await(gitlabAPI.getProject(projectId))
        response.status must be(200)
        (response.json \ "name").as[String] must be(projectName)

        await(gitlabAPI.deleteProject(projectId)).status must be(200)
      }
    }
  }
}
