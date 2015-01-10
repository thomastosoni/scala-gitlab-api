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
  var projectId = -1
  var commitSHA = ""

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      GitlabHelper.createTestSSHKey
      projectId = GitlabHelper.createTestProject
      logger.debug("Starting Branch Tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      try {
        val response = await(gitlabAPI.deleteBranch(projectId, "test_branch_name"))
        GitlabHelper.statusCheckError(response, "Branch", -1)
        super.afterAll()
      } catch {
        case e: UnsupportedOperationException => logger.error(e.toString)
      }
      GitlabHelper.deleteTestSSHKey()
      GitlabHelper.deleteTestProject()
      logger.debug("End of GitlabAPI Branch Tests")
    }
  }

  "GitlabAPI must manage project branches" should {

    "get all branches" in {
      val response = await(gitlabAPI.getBranches(projectId))
      response.status must be(200)
    }

    "get one branch" in {
      val response = await(gitlabAPI.getBranch(projectId, "master"))
      response.status must be(200)
      commitSHA = (response.json \ "commit" \ "id").as[String]
    }

    "protect a branch" in {
      await(gitlabAPI.protectBranch(projectId, "master")).status must be(200)
    }

    "unprotect a branch" in {
      await(gitlabAPI.unprotectBranch(projectId, "master")).status must be(200)
    }

    "create a new branch" in {
      val response = await(gitlabAPI.createBranch(projectId, "test_branch_name", commitSHA))
      response.status must be(201)
    }

    "delete a branch" in {
      val response = await(gitlabAPI.deleteBranch(projectId, "test_branch_name"))
      response.status must be(200)
      response.json must not be null
    }

  }
}
