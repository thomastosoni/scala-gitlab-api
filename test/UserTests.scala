import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger

class UserTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[UserTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val userName = GitlabHelper.userName
  val userUsername = GitlabHelper.userUsername
  val email = GitlabHelper.email
  val password = GitlabHelper.password
  var userId = -1

  override def afterAll() {
    val response = await(gitlabAPI.deleteUser(userId))
    GitlabHelper.checkDeleteAfterTest(response, USER)
    logger.debug("End of User Tests")
    Thread.sleep(1000L)
  }

  "GitlabAPI must manage users" should {
    "get all users" in {
      await(gitlabAPI.getUsers).status must be(200)
    }

    "create a user with name: " + userName + ", username: " + userUsername + " and email: " + email in {
      val response = await(gitlabAPI.createUser(email, password, userUsername, userName))
      response.status must be(201)
      userId = (response.json \ "id").as[Int]
    }

    "get all users with email: " + email in {
      val response = await(gitlabAPI.getUserByEmail(email))
      response.status must be(200)
      (response.json \\ "email").map(_.as[String]).head must be(email)
    }

    "get all users with username: " + userUsername in {
      val response = await(gitlabAPI.getUserByUsername(userUsername))
      response.status must be(200)
      (response.json \\ "username").map(_.as[String]).head must be(userUsername)
    }

    "update a user" in {
      val response = await(gitlabAPI.updateUser(userId, email, password, userUsername, name = "test_name_updated"))
      response.status must be(200)
      (response.json \ "name").as[String] must be("test_name_updated")
    }

    "delete a user" in {
      val response = await(gitlabAPI.deleteUser(userId))
      response.status must be(200)
      response.json must not be null
    }
  }
}
