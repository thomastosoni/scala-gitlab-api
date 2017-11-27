import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger
import play.api.test.Helpers._

class IssueNoteTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  lazy val logger = Logger(classOf[IssueNoteTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName

  var projectId = -1
  var issueId = -1
  var noteId = -1

  override def beforeAll(): Unit = {
    projectId = GitlabHelper.createEmptyTestProject
    val issueResponse = await(gitlabAPI.addIssue(projectId, "Test Issue"))
    if (issueResponse.status == 201) {
      issueId = (issueResponse.json \ "id").as[Int]
    } else logger.error("Before All: Didn't create test issue")
    logger.debug("Starting Issue Note Tests")
  }

  override def afterAll() {
    GitlabHelper.deleteTestProject()
    logger.debug("End of Issue Note Tests")
    Thread.sleep(1000L)
  }

  "GitlabAPI must manage issue notes" should {

    "add an issue note" in {
      val response = await(gitlabAPI.addIssueNote(projectId, issueId, "Test Issue Body"))
      response.status must be(201)
      noteId = (response.json \ "id").as[Int]
    }

    "get all project issue notes" in {
      await(gitlabAPI.getIssueNotes(projectId, issueId)).status must be(200)
    }

    "get an issue note" in {
      await(gitlabAPI.getIssueNote(projectId, issueId, noteId)).status must be(200)
    }

    //   TODO 405 unauthorized?
    //    "update an issue note" in {
    //      val response = await(gitlabAPI.updateIssueNote(projectId, issueId, noteId, "Updated Issue Note Body"))
    //      response.status must be(200)
    //      (response.json \ "body").as[String] must be("Updated Issue Note Body")
    //    }

  }
}
