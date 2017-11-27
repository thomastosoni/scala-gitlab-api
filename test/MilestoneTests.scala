import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger
import play.api.test.Helpers._

class MilestoneTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
    lazy val logger = Logger(classOf[MilestoneTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName

  val milestoneTitle = "Test Milestone"
  var projectId = -1
  var milestoneId = -1

  override def beforeAll(): Unit = {
    GitlabHelper.createTestSSHKey
    projectId = GitlabHelper.createEmptyTestProject
    logger.debug("Starting Milestone Tests")
  }

  override def afterAll() {
    GitlabHelper.deleteTestSSHKey()
    GitlabHelper.deleteTestProject()
    logger.debug("End of Milestone Tests")
    Thread.sleep(1000L)
  }

  "GitlabAPI must manage milestones" should {

    "add a project milestone" in {
      val response = await(gitlabAPI.addMilestone(projectId, milestoneTitle))
      response.status must be(201)
      milestoneId = (response.json \ "id").as[Int]
    }

    "get all the project milestones" in {
      await(gitlabAPI.getMilestones(projectId)).status must be(200)
    }

    "get a project milestone" in {
      await(gitlabAPI.getMilestone(projectId, milestoneId)).status must be(200)
    }

    "update a project milestone" in {
      val response = await(gitlabAPI.updateMilestone(projectId, milestoneId, milestoneTitle, Option("milestone_description")))
      response.status must be(200)
      (response.json \ "description").as[String] must be("milestone_description")
    }

    "close a project milestone" in {
      val response = await(gitlabAPI.closeMilestone(projectId, milestoneId))
      response.status must be(200)
    }

  }
}
