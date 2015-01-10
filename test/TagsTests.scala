import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class TagsTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[TagsTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  var projectId = 0

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      projectId = GitlabHelper.createProject
      logger.debug("Starting Branches tests")
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

  "GitlabAPI must manage repository tags" should {

    "get all the tags of a project" in {
      val response = await(gitlabAPI.getRepositoryTags(projectId))
      response.status must be(200)
    }

  }

  "Tests cleanup" in {
    gitlabAPI.deleteProject(projectId)
  }
}
