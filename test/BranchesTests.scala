import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class BranchesTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[BranchesTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  var projectId = 0

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      projectId = GitlabHelper.createProject
      logger.debug("Starting Branches tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      try {
        if (projectId != 0) {
          val response = await(gitlabAPI.deleteProject(projectId))
          GitlabHelper.statusCheck(response, "Project", projectId)
        }
        super.afterAll()
      } catch {
        case e: UnsupportedOperationException => logger.error(e.toString)
      }
      logger.debug("End of GitlabAPI Branches tests")
    }
  }

  "GitlabAPI must manage project branches" should {

    //    "get all branches" in {
    //      val response = await(gitlabAPI.getBranches(projectId))
    //      logger.error(" ---------> " + response.body)
    //      response.status must be(200)
    //    }

    "get one branch" in {
      val response = await(gitlabAPI.getBranch(projectId, "master"))
      logger.error(" ---------> " + response.body)
      response.status must be(200)
    }

    //    "create a new branch" in {
    //      val response = await(gitlabAPI.createBranch(projectId, "new_branch", "0b4bc9a49b562e85de7cc9e834518ea6828729b9"))
    //      logger.error(" ---------> " + response.body)
    //      response.status must be(201)
    //    }

  }

  "Tests cleanup" in {
    gitlabAPI.deleteProject(projectId)
  }
}
