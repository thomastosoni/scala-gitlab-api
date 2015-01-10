import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.slf4j.LoggerFactory
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class ProjectsTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[ProjectsTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  var projectId = 0

  override def afterAll() {
    running(FakeApplication()) {
      try {
        if (projectId != 0) {
          val response = await(gitlabAPI.deleteProject(projectId))
          GitlabHelper.statusCheck(response, "Project", projectId)
        }
      } catch {
        case e: UnsupportedOperationException => logger.error("Couldn't delete project with id: " + projectId)
      }
      logger.debug("End of GitlabAPI Projects tests")
    }
  }

  "GitlabAPI must manage projects" should {

    "get projects" in {
      await(gitlabAPI.getProjects()).status must be(200)
    }

    "get all projects" in {
      await(gitlabAPI.getAllProjects).status must be(200)
    }

    "get owned projects" in {
      await(gitlabAPI.getOwnedProjects).status must be(200)
    }

    "create a project" in {
      val response = await(gitlabAPI.createProject(projectName))
      response.status must be(201)
      projectId = (response.json \ "id").as[Int]
    }

    "get a project by id" in {
      val response = await(gitlabAPI.getProject(projectId))
      response.status must be(200)
      (response.json \ "name").as[String] must be(projectName)
    }

    "get a project by name" in {
      val response = await(gitlabAPI.getProject(projectName))
      response.status must be(200)
      (response.json \\ "name").map(_.as[String]).head must be(projectName)
    }

    "delete a project" in {
      val response = await(gitlabAPI.deleteProject(projectId))
      response.status must be(200)
      response.json must not be null
    }
  }
}

// TODO fork
