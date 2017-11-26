import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger

class MergeRequestTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[MergeRequestTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName

  val mergeTitle = "Test Merge"
  val targetBranch = "master"
  val sourceBranch = "feature"

  var projectId = -1
  var mergeRequestId = -1
  var lastCommitSHA = ""

  override def beforeAll(): Unit = {
    GitlabHelper.createTestSSHKey
    projectId = GitlabHelper.createTestProject
    val commitsResponse = await(gitlabAPI.getCommits(projectId))
    if (commitsResponse.status == 200) {
      lastCommitSHA = (commitsResponse.json \\ "id").map(_.as[String]).head
      await(gitlabAPI.createBranch(projectId, sourceBranch, lastCommitSHA))
    } else logger.error("Before All: Didn't get commits")
    logger.debug("Starting Merge Request Tests")
  }

  override def afterAll() {
    GitlabHelper.deleteTestProject()
    GitlabHelper.deleteTestSSHKey()
    logger.debug("End of Merge Request Tests")
    Thread.sleep(1000L)
  }

  "GitlabAPI must manage merge operations" should {

    "add a merge request" in {
      val response = await(gitlabAPI.addMergeRequest(projectId, sourceBranch, targetBranch, mergeTitle))
      response.status must be(201)
      mergeRequestId = (response.json \ "id").as[Int]
    }

    "get all merge requests" in {
      await(gitlabAPI.getMergeRequests(projectId)).status must be(200)
    }

    "get one merge request" in {
      await(gitlabAPI.getMergeRequest(projectId, mergeRequestId)).status must be(200)
    }

    "add comment to a merge request" in {
      await(gitlabAPI.addMergeRequestComment(projectId, mergeRequestId, "test_comment")).status must be(201)
    }

    "get merge request comments" in {
      await(gitlabAPI.getMergeRequestComments(projectId, mergeRequestId)).status must be(200)
    }

    "update merge request" in {
      val response = await(gitlabAPI.updateMergeRequest(projectId, mergeRequestId, title = Option("updated_title")))
      response.status must be(200)
      (response.json \ "title").as[String] must be("updated_title")
    }

    "accept merge request" in {
      val response = await(gitlabAPI.acceptMergeRequest(projectId, mergeRequestId))
      response.status must be(200)
      response.json must not be null
    }

  }

}
