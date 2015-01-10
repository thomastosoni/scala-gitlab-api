import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class HooksTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[HooksTests])

  val gitlabAPI = GitlabHelper.gitlabAPI

  val projectName = GitlabHelper.projectName
  var projectId = 0
  var systemHookId = 0
  var projectHookId = 0

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      projectId = GitlabHelper.createEmptyTestProject
      logger.debug("Starting Team Tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      try {
        val systemHookResponse = await(gitlabAPI.deleteHook(systemHookId))
        GitlabHelper.statusCheckError(systemHookResponse, "System Hook", systemHookId)
        val projectHookResponse = await(gitlabAPI.deleteHook(projectId, projectHookId))
        GitlabHelper.statusCheckError(projectHookResponse, "Project Hook", projectHookId)
        super.afterAll()
      } catch {
        case e: UnsupportedOperationException => logger.error(e.toString)
      }
      GitlabHelper.deleteTestProject()
      logger.debug("End of GitlabAPI Hooks tests")
    }
  }

  "GitlabAPI must manage system hooks" should {
    "create a system hook" in {
      val response = await(gitlabAPI.addHook("http://www.google.com"))
      systemHookId = (response.json \ "id").as[Int]
      systemHookId must not be 0
    }

    "get all system hooks" in {
      await(gitlabAPI.getHooks).status must be(200)
    }

    "get one system hook" in {
      await(gitlabAPI.getHook(systemHookId)).status must be(200)
    }

    "delete a system hook" in {
      val response = await(gitlabAPI.deleteHook(systemHookId))
      response.status must be(200)
      response.json must not be null
    }
  }

  "GitlabAPI must manage project hooks" should {
    "create a project hook" in {
      val response = await(gitlabAPI.addHook(projectId,  "http://www.google.com"))
      response.status must be(201)
      projectHookId = (response.json \ "id").as[Int]
      projectHookId must not be 0
    }

    "get one project hook" in {
      await(gitlabAPI.getHook(projectId, projectHookId)).status must be(200)
    }

    "update a project hook" in {
      await(gitlabAPI.updateHook(projectId, projectHookId, "http://www.yahoo.com")).status must be(200)
    }

    "delete a project hook" in {
      val response = await(gitlabAPI.deleteHook(projectId, projectHookId))
      response.status must be(200)
      response.json must not be null
    }

  }
}
