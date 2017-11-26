import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger

class SnippetTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[SnippetTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  var projectId = -1
  var snippetId = -1

  override def beforeAll(): Unit = {
    projectId = GitlabHelper.createEmptyTestProject
    logger.debug("Starting Snippet Tests")
  }

  override def afterAll() {
    GitlabHelper.deleteTestProject()
    logger.debug("End of Snippet Tests")
    Thread.sleep(1000L)
  }

  "GitlabAPI must manage project snippets" should {

    "create snippet" in {
      val response = await(gitlabAPI.createSnippet(projectId, "test_title", "test_file_name", "test_code"))
      response.status must be(201)
      snippetId = (response.json \ "id").as[Int]
    }

    "get all snippets" in {
      await(gitlabAPI.getSnippets(projectId)).status must be(200)
    }

    "get one snippet" in {
      await(gitlabAPI.getSnippet(projectId, snippetId)).status must be(200)
    }

    "get one raw snippet" in {
      await(gitlabAPI.getRawSnippet(projectId, snippetId)).status must be(200)
    }

    "update snippet" in {
      await(gitlabAPI.updateSnippet(projectId, snippetId, Option("updated_test_title"))).status must be(200)
      val response = await(gitlabAPI.getSnippet(projectId, snippetId))
      response.status must be(200)
      (response.json \ "title").as[String] must be("updated_test_title")
    }

    "delete snippet" in {
      val response = await(gitlabAPI.deleteSnippet(projectId, snippetId))
      response.status must be(200)
      response.json must not be null
    }

  }
}
