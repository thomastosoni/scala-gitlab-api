import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class TeamsTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[TeamsTests])

  val gitlabAPI = GitlabHelper.gitlabAPI

  var teamMemberId = 123
  var projectId = -1

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      projectId = GitlabHelper.createEmptyTestProject
      logger.debug("Starting Teams tests")
    }
  }

  override def afterAll(): Unit = {
    running(FakeApplication()) {
      try {
        val teamMemberResponse = await(gitlabAPI.deleteTeamMember(projectId, teamMemberId))
        GitlabHelper.statusCheckError(teamMemberResponse, "Team Member", teamMemberId)
        val projectResponse = await(gitlabAPI.deleteProject(projectId))
        GitlabHelper.statusCheck(projectResponse, "Project", projectId)
        super.afterAll()
      } catch {
        case e: UnsupportedOperationException => logger.error(e.toString)
      }
      finally {
        logger.debug("End of GitlabAPI Teams tests")
      }
    }
  }

  "GitlabAPI must manage teams" should {
    "get team members" in {
      await(gitlabAPI.getTeamMembers(projectId)).status must be(200)
    }

    "add a team member" in {
      val response = await(gitlabAPI.addTeamMember(projectId, teamMemberId, 10))
      logger.error(response.json.toString())
    }

//    "get a team member" in {
//      await(gitlabAPI.getTeamMember(projectId, teamMemberId)).status must be(200)
//    }
//
//    "delete a team member" in {
//      await(gitlabAPI.deleteTeamMember(projectId, teamMemberId)).status must be(200)
//    }
  }
}
