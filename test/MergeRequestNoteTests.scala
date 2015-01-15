import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.test.FakeApplication
import play.api.test.Helpers._

class MergeRequestNoteTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[MergeRequestNoteTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName

  val mergeTitle = "Test Merge"
  val targetBranch = "master"
  val sourceBranch = "test_branch"

  var projectId = -1
  var mergeRequestId = -1
  var noteId = -1
  var lastCommitSHA = ""

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      GitlabHelper.createTestSSHKey
      projectId = GitlabHelper.createTestProject
      val commitsResponse = await(gitlabAPI.getCommits(projectId))
      if (commitsResponse.status == 200) {
        lastCommitSHA = (commitsResponse.json \\ "id").map(_.as[String]).head
        val branchResponse = await(gitlabAPI.createBranch(projectId, sourceBranch, lastCommitSHA))
        if (branchResponse.status == 201) {
          val mergeRequestResponse = await(gitlabAPI.addMergeRequest(projectId, sourceBranch, targetBranch, mergeTitle))
          if (mergeRequestResponse.status == 201) {
            mergeRequestId = (mergeRequestResponse.json \ "id").as[Int]
          } else logger.error("Before All: Didn't create merge request")
        } else logger.error("Before All: Didn't create branch")
      } else logger.error("Before All: Didn't create commit")
      logger.debug("Starting Merge Request Note Tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      GitlabHelper.deleteTestSSHKey()
      GitlabHelper.deleteTestProject()
      logger.debug("End of Merge Request Note Tests")
      Thread.sleep(1000L)
    }
  }

  "GitlabAPI must manage merge request notes" should {

    "add an issue note" in {
      val response = await(gitlabAPI.addMergeRequestNote(projectId, mergeRequestId, "Test Issue Body"))
      response.status must be(201)
      noteId = (response.json \ "id").as[Int]
    }

    "get all project issue notes" in {
      await(gitlabAPI.getMergeRequestNotes(projectId, mergeRequestId)).status must be(200)
    }

    "get an issue note" in {
      await(gitlabAPI.getMergeRequestNote(projectId, mergeRequestId, noteId)).status must be (200)
    }

    //   TODO 405 unauthorized?
    //    "update an issue note" in {
    //      val response = await(gitlabAPI.updateIssueNote(projectId, issueId, noteId, "Updated Issue Note Body"))
    //      response.status must be(200)
    //      (response.json \ "body").as[String] must be("Updated Issue Note Body")
    //    }

  }
}
