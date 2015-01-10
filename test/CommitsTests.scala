import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class CommitsTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[CommitsTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  var projectId = 0
  var lastCommitSha = ""

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      await(gitlabAPI.addSSHKey("SSH Key Test", GitlabHelper.sshKey))
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

  "GitlabAPI must manage commits" should {

    "get all commits" in {
      val response = await(gitlabAPI.getCommits(projectId))
      response.status must be(200)
      lastCommitSha = (response.json \\ "id").map(_.as[String]).head
    }

    "get one commit" in {
      val response = await(gitlabAPI.getCommit(projectId, lastCommitSha))
      response.status must be(200)
      response.json must not be null
    }

    "get a commit diff" in {
      await(gitlabAPI.getDiff(projectId, lastCommitSha)).status must be(200)
    }

    "add comment to commit" in {
      await(gitlabAPI.addCommitComments(projectId, lastCommitSha, "test_comment")).status must be(201)
    }

    "get commit comments" in {
      await(gitlabAPI.getCommitComments(projectId, lastCommitSha)).status must be(200)
    }
  }

  "Tests cleanup" in {
    gitlabAPI.deleteProject(projectId)
  }

}
