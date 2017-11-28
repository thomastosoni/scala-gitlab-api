import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class ProjectTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  lazy val logger = Logger(classOf[ProjectTests])

  val gitlabAPI: GitlabAPI = GitlabHelper.gitlabAPI
  val projectName: String = GitlabHelper.projectName
  var projectId: Int = -1

  override def beforeAll() {
    logger.debug("Starting Project tests...")
  }

  override def afterAll() {
    val response = await(gitlabAPI.removeProject(projectId))
    GitlabHelper.checkDeleteAfterTest(response, PROJECT)
    logger.debug("End of Project tests")
    Thread.sleep(1000L)
  }

  "GitlabAPI must manage projects" should {

    "get projects" in {
      await(gitlabAPI.getProjects()).status must be(200)
    }

    "get owned projects" in {
      await(gitlabAPI.getProjects(owned = Some(true))).status must be(200)
    }

    "create a project" in {
      val response = await(gitlabAPI.createProject(name = Some(projectName)))
      response match {
        case response: WSResponse if response.status == 201 =>
          projectId = (response.json \ "id").as[Int]
        case _ => logger.error(response.statusText)
      }
    }

    "get a project by id" in {
      val response = await(gitlabAPI.getProject(projectId))
      response.status must be(200)
      (response.json \ "name").as[String] must be(projectName)
    }

    "get a project by name" in {
      val response = await(gitlabAPI.getProjects(search = Some(projectName)))
      response.status must be(200)
      (response.json \ "name").as[String].head must be(projectName)
    }

    "delete a project" in {
      val response = await(gitlabAPI.removeProject(projectId))
      response.status must be(200)
      response.json must not be null
    }
  }
}
