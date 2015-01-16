import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class TeamTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[TeamTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val testProjectName = GitlabHelper.projectName
  val testUserName = GitlabHelper.userName

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
      val teamMemberResponse = await(gitlabAPI.deleteTeamMember(projectId, userId))
      GitlabHelper.checkDeleteAfterTest(teamMemberResponse, TEAM_MEMBER)
      logger.debug("End of Team Tests")
      Thread.sleep(1000L)
    }
  }

  "GitlabAPI must manage teams" should {
    "add a team member" in {
      await(gitlabAPI.addTeamMember(projectId, userId, 40)).status must be(201)
    }

    "get team members by project id" in {
      await(gitlabAPI.getTeamMembers(projectId)).status must be(200)
    }

    //    "get team members by project name" in {
    //      await(gitlabAPI.getTeamMembersByName(testProjectName)).status must be(200)
    //    }

    "get a team member by project id" in {
      await(gitlabAPI.getTeamMember(projectId, userId)).status must be(200)
    }

    //    "get a team member by project name" in {
    //      await(gitlabAPI.getTeamMember(testProjectName, userId)).status must be(200)
    //    }

    "update team member access level" in {
      val response = await(gitlabAPI.updateTeamMemberAccessLevel(projectId, userId, 20))
      response.status must be(200)
      (response.json \ "access_level").as[Int] must be(20)
    }

    // Revoking team membership for a user who is not currently a team member is considered success.
    "delete a team member" in {
      await(gitlabAPI.deleteTeamMember(projectId, userId)).status must be (200)
    }
  }
}
