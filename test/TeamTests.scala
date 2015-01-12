import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class TeamTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[TeamTests])

  val gitlabAPI = GitlabHelper.gitlabAPI

  var userId = -1
  var projectId = -1

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      userId = GitlabHelper.createTestUser
      projectId = GitlabHelper.createEmptyTestProject
      logger.debug("Starting Team Tests")
    }
  }

  override def afterAll(): Unit = {
    running(FakeApplication()) {
      GitlabHelper.deleteTestUser()
      GitlabHelper.deleteTestProject()
      try {
        val teamMemberResponse = await(gitlabAPI.deleteTeamMember(projectId, userId))
        GitlabHelper.statusCheck(teamMemberResponse, "Team Member", userId)
      } catch {
        case e: UnsupportedOperationException => logger.error(e.toString)
      }
      logger.debug("End of Team Tests")
      Thread.sleep(1000L)
    }
  }

  "GitlabAPI must manage teams" should {
    "get team members" in {
      await(gitlabAPI.getTeamMembers(projectId)).status must be(200)
    }

    "add a team member" in {
      await(gitlabAPI.addTeamMember(projectId, userId, 40)).status must be(201)
    }

    "get a team member" in {
      await(gitlabAPI.getTeamMember(projectId, userId)).status must be(200)
    }

    // Revoking team membership for a user who is not currently a team member is considered success.
    "delete a team member" in {
      await(gitlabAPI.deleteTeamMember(projectId, userId)).status must be (200)
    }
  }
}
