import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class RepositoryFilesTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[RepositoryFilesTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  var projectId = 0

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      projectId = GitlabHelper.createProject
      logger.debug("Starting Repository tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      try {
        if (projectId != 0) {
          val response = await(gitlabAPI.deleteProject(projectId))
          GitlabHelper.statusCheck(response, "Project", projectId)
        }
        super.afterAll()
      } catch {
        case e: UnsupportedOperationException => logger.error(e.toString)
      }
      logger.debug("End of GitlabAPI Branches tests")
    }
  }

  "GitlabAPI must manage repository files" should {

    "create file" in {
      val response = await(gitlabAPI.createFile(projectId, "test.file", "develop", "content", "commit message", None))
      logger.error(response.json.toString())
      response.status must be(201)
    }
  }

  "Tests cleanup" in {
    gitlabAPI.deleteProject(projectId)
  }
}
