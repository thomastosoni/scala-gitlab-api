import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class TeamsTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[TeamsTests])

  val gitlabAPI = GitlabHelper.gitlabAPI

  var teamMemberId = 1
  var projectId = 0

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      try {
        val response = await(gitlabAPI.createProject(GitlabHelper.projectName))
        projectId = (response.json \ "id").as[Int]
      } catch {
        case e: Throwable => logger.error("Couldn't setup testing environment for team testing")
      }
      logger.debug("Base project created. Project id: " + projectId)
      logger.debug("Starting team tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      try {
        if (projectId != 0) {
          val teamMemberResponse = await(gitlabAPI.deleteTeamMember(projectId, teamMemberId))
          teamMemberResponse.status match {
            case 200 => logger.debug("Team member (id: " + teamMemberId + ") successfully removed from project (id: " + projectId + ")")
            case 404 => logger.debug("Team member (id: " + teamMemberId + ") not found, must have been removed by tests")
            case _ => throw new UnsupportedOperationException("Couldn't delete team member (id: " + teamMemberId + ") from project (id: " + projectId + ")")
          }
          val projectResponse = await(gitlabAPI.deleteProject(projectId))
          projectResponse.status match {
            case 200 => logger.debug("Project (id: " + projectId + ") successfully removed")
            case 404 => logger.debug("Project (id: " + projectId + ") not found, must have been removed by tests")
            case _ => throw new UnsupportedOperationException
          }
        }
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

    "add, get and delete team member" in {
      await(gitlabAPI.addTeamMember(projectId, teamMemberId, 10)).status must be(201)
    }

    "get team member" in {
      await(gitlabAPI.getTeamMember(projectId, teamMemberId)).status must be(200)
    }

    "delete team member" in {
      await(gitlabAPI.deleteTeamMember(projectId, teamMemberId)).status must be(200)
    }
  }
}
