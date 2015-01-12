import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class SnippetNotesTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[SnippetNotesTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName

  var projectId = -1
  var snippetId = -1
  var noteId = -1

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      projectId = GitlabHelper.createEmptyTestProject
      val snippetResponse = await(gitlabAPI.createSnippet(projectId, "Snippet Title", "snippet", "code"))
      if (snippetResponse.status == 201) {
        snippetId = (snippetResponse.json \ "id").as[Int]
      } else logger.error("Didn't create test snippet")
      logger.debug("Starting Snippet Note Tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      running(FakeApplication()) {
        GitlabHelper.deleteTestProject()
        logger.debug("End of Snippet Note Tests")
        Thread.sleep(1000L)
      }
    }
  }

  "GitlabAPI must manage snippet notes" should {
    
    "add an snippet note" in {
      val response = await(gitlabAPI.addSnippetNote(projectId, snippetId, "Test snippet Body"))
      response.status must be(201)
      noteId = (response.json \ "id").as[Int]
    }

    "get all project snippet notes" in {
      await(gitlabAPI.getSnippetNotes(projectId, snippetId)).status must be(200)
    }

    "get an snippet note" in {
      await(gitlabAPI.getSnippetNote(projectId, snippetId, noteId)).status must be (200)
    }

//    "update an snippet note" in {
//      val response = await(gitlabAPI.updateSnippetNote(projectId, snippetId, noteId, "Updated Snippet Note Body"))
//      response.status must be(200)
//      (response.json \ "body").as[String] must be("Updated Snippet Note Body")
//    }

  }
}
