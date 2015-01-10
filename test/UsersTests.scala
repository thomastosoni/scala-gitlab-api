import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter}
import org.scalatestplus.play._
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class UsersTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[UsersTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val userName = GitlabHelper.userName
  val userUsername = GitlabHelper.userUsername
  val email = GitlabHelper.email
  val password = GitlabHelper.password
  var userId = -1

  override def afterAll() {
    running(FakeApplication()) {
      try {
        val response = await(gitlabAPI.deleteUser(userId))
        GitlabHelper.statusCheckError(response, "User", userId)
        super.afterAll()
      } catch {
        case e: UnsupportedOperationException => logger.error(e.toString)
      }
      logger.debug("End of GitlabAPI Users tests")
    }
  }

  "GitlabAPI must manage users" should {
    "get all users" in {
      await(gitlabAPI.getUsers).status must be(200)
    }

    "create a user with name: " + userName + ", username: " + userUsername + " and email: " + email in {
      val response = await(gitlabAPI.createUser(email, password, userUsername, userName, None))
      response.status must be(201)
      userId = (response.json \ "id").as[Int]
    }

    "get all users by email: " + email in {
      val response = await(gitlabAPI.getUserByEmail(email))
      response.status must be(200)
      (response.json \\ "email").map(_.as[String]).head must be(email)
    }

    "get user by username: " + userUsername in {
      val response = await(gitlabAPI.getUserByUsername(userUsername))
      response.status must be(200)
      (response.json \\ "username").map(_.as[String]).head must be(userUsername)
    }

    "update a user" in {
      val response = await(gitlabAPI.updateUser(userId, email, password, userUsername, "test_name_updated"))
      response.status must be(200)
    }

    "delete a user" in {
      val response = await(gitlabAPI.deleteUser(userId))
      response.status must be(200)
      response.json must not be null
    }
  }
}
