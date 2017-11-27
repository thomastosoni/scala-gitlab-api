import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext

class CommitTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  lazy val logger = Logger(classOf[CommitTests])

  val gitlabAPI: GitlabAPI = GitlabHelper.gitlabAPI
  val projectName: String = GitlabHelper.projectName
  var projectId: Int = -1
  var commitSHA = ""

  override def beforeAll(): Unit = {
    GitlabHelper.createTestSSHKey
    projectId = GitlabHelper.createTestProject
    logger.debug("Starting Commit Tests")
  }

  override def afterAll() {
    GitlabHelper.deleteTestProject()
    GitlabHelper.deleteTestSSHKey()
    logger.debug("End of Branch Tests")
    Thread.sleep(1000L)
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
