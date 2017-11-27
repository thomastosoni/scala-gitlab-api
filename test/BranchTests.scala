import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{Application, Logger}

class BranchTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  import GitlabHelper._

  lazy val logger: Logger = Logger(classOf[BranchTests])

  val projectName: String = GitlabHelper.projectName
  var projectId: Int = -1
  var commitSHA: String = ""

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder().configure(Map("ehcacheplugin" -> "disabled")).build()
  }

  override def beforeAll(): Unit = {
    createTestSSHKey
    projectId = createTestProject
    logger.debug("Starting Branch Tests")
  }

  override def afterAll() {
    val response = await(gitlabAPI.deleteBranch(projectId, "branch_name"))
    checkDeleteAfterTest(response, BRANCH)
    deleteTestSSHKey()
    deleteTestProject()
    logger.debug("End of Branch Tests")
    Thread.sleep(1000L)
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
