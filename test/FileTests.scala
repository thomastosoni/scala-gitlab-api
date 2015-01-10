import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class FileTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[FileTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  var projectId = 0

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      projectId = GitlabHelper.createTestProject
      logger.debug("Starting Repository Tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      GitlabHelper.deleteTestProject()
      logger.debug("End of GitlabAPI Repository Tests")
    }
  }

  "GitlabAPI must manage repository files" should {

    "create file" in {
      val response = await(gitlabAPI.createFile(projectId, "test.file", "develop", "content", "commit message", None))
      logger.error(response.json.toString())
      response.status must be(201)
    }
  }

}
