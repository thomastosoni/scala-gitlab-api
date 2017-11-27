import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger
import play.api.test.Helpers._

class HookTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  lazy val logger = Logger(classOf[HookTests])

  val gitlabAPI = GitlabHelper.gitlabAPI

  val projectName = GitlabHelper.projectName
  var projectId = -1
  var systemHookId = -1
  var projectHookId = -1

  override def beforeAll(): Unit = {
    projectId = GitlabHelper.createEmptyTestProject
    logger.debug("Starting Hook Tests")
  }

  override def afterAll() {
    GitlabHelper.deleteTestProject()
    val systemHookResponse = await(gitlabAPI.deleteHook(systemHookId))
    GitlabHelper.checkDeleteAfterTest(systemHookResponse, SYSTEM_HOOK)
    val projectHookResponse = await(gitlabAPI.deleteHook(projectId, projectHookId))
    GitlabHelper.checkDeleteAfterTest(projectHookResponse, PROJECT_HOOK)
    logger.debug("End of Hook tests")
    Thread.sleep(1000L)
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
      val response = await(gitlabAPI.addHook(projectId, "http://www.google.com"))
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
