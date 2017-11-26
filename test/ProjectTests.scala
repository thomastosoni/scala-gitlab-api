package tmp

import play.api.Logger

class ProjectTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[ProjectTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  var projectId = -1

  override def afterAll() {
    val response = await(gitlabAPI.deleteProject(projectId))
    GitlabHelper.checkDeleteAfterTest(response, PROJECT)
    logger.debug("End of Project tests")
    Thread.sleep(1000L)
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

    //    "fork project" in {
    //      await(gitlabAPI.forkProject(projectId)).status must be (200)
    //    }

    "delete a project" in {
      val response = await(gitlabAPI.deleteProject(projectId))
      response.status must be(200)
      response.json must not be null
    }
  }
}
