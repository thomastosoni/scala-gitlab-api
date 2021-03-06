import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class LabelTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[LabelTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName

  val labelTitle = "Test Label"
  val labelColor = "#FFAABB"
  var projectId = -1

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      projectId = GitlabHelper.createEmptyTestProject
      logger.debug("Starting Label Tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      GitlabHelper.deleteTestProject()
      val response = await(gitlabAPI.deleteLabel(projectId, labelTitle))
      GitlabHelper.checkDeleteAfterTest(response, LABEL)
      logger.debug("End of Label Tests")
      Thread.sleep(1000L)
    }
  }

  "GitlabAPI must manage project labels" should {

    "add a label to a project" in {
      await(gitlabAPI.addLabel(projectId, labelTitle, labelColor)).status must be(201)
    }

    "get all the labels of a project" in {
      await(gitlabAPI.getLabels(projectId)).status must be(200)
    }

    "update label" in {
      val response = await(gitlabAPI.updateLabel(projectId, labelTitle, color = Option("#FFFFFF")))
      response.status must be(200)
      (response.json \ "color").as[String] must be("#FFFFFF")
    }

    "delete label" in {
      val response = await(gitlabAPI.deleteLabel(projectId, labelTitle))
      response.status must be(200)
      response.json must not be null
    }
  }

}
