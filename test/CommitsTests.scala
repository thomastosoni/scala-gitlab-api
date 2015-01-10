import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class CommitsTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[CommitsTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  var projectId = -1
  var commitSHA = ""

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      GitlabHelper.createTestSSHKey
      projectId = GitlabHelper.createTestProject
      logger.debug("Starting Commit Tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      GitlabHelper.deleteTestProject()
      GitlabHelper.deleteTestSSHKey()
      logger.debug("End of GitlabAPI Branch Tests")
    }
  }

  "GitlabAPI must manage commits" should {

    "get all commits" in {
      val response = await(gitlabAPI.getCommits(projectId))
      response.status must be(200)
      commitSHA = (response.json \\ "id").map(_.as[String]).head
    }

    "get one commit" in {
      val response = await(gitlabAPI.getCommit(projectId, commitSHA))
      response.status must be(200)
      response.json must not be null
    }

    "get a commit diff" in {
      await(gitlabAPI.getDiff(projectId, commitSHA)).status must be(200)
    }

    "add comment to commit" in {
      await(gitlabAPI.addCommitComments(projectId, commitSHA, "test_comment")).status must be(201)
    }

    "get commit comments" in {
      await(gitlabAPI.getCommitComments(projectId, commitSHA)).status must be(200)
    }
  }

}
