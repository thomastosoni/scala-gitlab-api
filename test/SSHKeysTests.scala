import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class SSHKeysTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll  {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[SSHKeysTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val sshKeyTitle = "ssh_key_title"
  var sshKeyId = 0

  override def afterAll() {
    running(FakeApplication()) {
      try {
        if (sshKeyId != 0) {
          val SShKeyResponse = await(gitlabAPI.deleteSSHKey(sshKeyId))
          GitlabHelper.statusCheck(SShKeyResponse, "SSH KEY", sshKeyId)
        }
        super.afterAll()
      } catch {
        case e: UnsupportedOperationException => logger.error(e.toString)
      }
      finally {
        logger.debug("End of GitlabAPI SSHKeys tests")
      }
    }
  }

  "GitlabAPI must manage SSH keys" should {

    "add an SSH key" in {
      val response = await(gitlabAPI.addSSHKey(sshKeyTitle, GitlabHelper.sshKey))
      response.status must be(201)
      sshKeyId = (response.json \ "id").as[Int]
      (response.json \ "title").as[String] must be(sshKeyTitle)
    }
    
    "get SSH keys" in {
      val response = await(gitlabAPI.getSSHKeys)
      response.status must be(200)
    }

    "get SSH key" in {
      val response = await(gitlabAPI.getSSHKey(sshKeyId))
      response.status must be(200)
      (response.json \ "title").as[String] must be(sshKeyTitle)
    }

    "delete SSH key" in {
      await(gitlabAPI.deleteSSHKey(sshKeyId)).status must be(200)
    }

  }
}
