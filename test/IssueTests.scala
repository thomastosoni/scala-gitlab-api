import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class IssueTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[IssueTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName

  val issueTitle = "Test Issue"
  var projectId = -1
  var issueId = -1

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      projectId = GitlabHelper.createEmptyTestProject
      logger.debug("Starting Issue Tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      try {
        val response = await(gitlabAPI.getIssue(projectId, issueId))
        if (response.status == 200) {
          val issueState = (response.json \ "state").as[String]
          if (issueState != "closed") throw new IllegalStateException("Issue state should be set to \"closed\", actual: " + issueState)
        } else logger.error("After All: Didn't get issue")
      } catch {
        case e: IllegalStateException => logger.error(e.toString)
      }
      GitlabHelper.deleteTestProject()
      logger.debug("End of Issue Tests")
      Thread.sleep(1000L)
    }
  }

  "GitlabAPI must manage project issues" should {

    "add an issue" in {
      val response = await(gitlabAPI.addIssue(projectId, issueTitle))
      response.status must be(201)
      issueId = (response.json \ "id").as[Int]
    }

    "get all issues" in {
      await(gitlabAPI.getAllIssues()).status must be(200)
    }

    "get all the issues of a project" in {
      await(gitlabAPI.getIssues(projectId)).status must be(200)
    }

    "get one issue" in {
      await(gitlabAPI.getIssue(projectId, issueId)).status must be(200)
    }

    "update an issue" in {
      val response = await(gitlabAPI.updateIssue(projectId, issueId, description = Option("issue_description")))
      response.status must be(200)
      (response.json \ "description").as[String] must be("issue_description")
    }

    "close an issue" in {
      val response = await(gitlabAPI.closeIssue(projectId, issueId))
      response.status must be(200)
    }

  }

}
