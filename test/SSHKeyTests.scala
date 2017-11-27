import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger
import play.api.test.Helpers._

class SSHKeyTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
    lazy val logger = Logger(classOf[SSHKeyTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val sshKeyTitle = "ssh_key_title"
  var sshKeyId = 0

  override def afterAll() {
    val SShKeyResponse = await(gitlabAPI.deleteSSHKey(sshKeyId))
    GitlabHelper.checkDeleteAfterTest(SShKeyResponse, SSH_KEY)
    logger.debug("End of SSHKeys tests")
    Thread.sleep(1000L)
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
