import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger

class IssueTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  lazy val logger = Logger(classOf[IssueTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName

  val issueTitle = "Test Issue"
  var projectId = -1
  var issueId = -1

  override def beforeAll(): Unit = {
    projectId = GitlabHelper.createEmptyTestProject
    logger.debug("Starting Issue Tests")
  }

  override def afterAll() {
    GitlabHelper.deleteTestProject()
    logger.debug("End of Issue Tests")
    Thread.sleep(1000L)
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
