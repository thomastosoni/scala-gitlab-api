import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext

class DeployKeyTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  lazy val logger = Logger(classOf[DeployKeyTests])

  val gitlabAPI: GitlabAPI = GitlabHelper.gitlabAPI
  val projectName: String = GitlabHelper.projectName

  val deployKeyTitle = "Test Deploy Key"
  val deployKey = "ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAIEAiPWx6WM4lhHNedGfBpPJNPpZ7yKu+dnn1SJejgt4596k6YjzGGphH2TUxwKzxcKDKKezwkpfnxPkSMkuEspGRt/aZZ9wa++Oi7Qkr8prgHc4soW6NUlfDzpvZK2H5E7eQaSeP3SAwGmQKUFHCddNaP0L+hM7zhFNzjFvpaMgJw0="
  var projectId: Int = -1
  var deployKeyId: Int = -1

  override def beforeAll(): Unit = {
      projectId = GitlabHelper.createEmptyTestProject
      logger.debug("Starting Deploy Key Tests")
  }

  override def afterAll() {
      val response = await(gitlabAPI.deleteDeployKey(projectId, deployKeyId))
      GitlabHelper.checkDeleteAfterTest(response, DEPLOY_KEY)
      GitlabHelper.deleteTestProject()
      Thread.sleep(1000L)
      logger.debug("End of Deploy Key Tests")
  }

  "GitlabAPI must manage deploy keys" should {

    "add a deploy key to a project" in {
      val response = await(gitlabAPI.addDeployKey(projectId, deployKeyTitle, deployKey))
      response.status must be(201)
      deployKeyId = (response.json \ "id").as[Int]
    }

    "get all the deploy keys of a project" in {
      await(gitlabAPI.getDeployKeys(projectId)).status must be(200)
    }

    "get a deploy key" in {
      await(gitlabAPI.getDeployKey(projectId, deployKeyId)).status must be(200)
    }

    "delete deploy key" in {
      val response = await(gitlabAPI.deleteDeployKey(projectId, deployKeyId))
      response.status must be(200)
      response.json must not be null
    }
  }

}
