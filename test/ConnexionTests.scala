import java.net.URL

import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import scala.concurrent.ExecutionContext

class ConnexionTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfter {
  val gitlabAPI: GitlabAPI = GitlabHelper.gitlabAPI

  "GitlabAPI" must {
    "not be able to connect with invalid credentials" in {
      await(gitlabAPI.connectToSession("INVALID", "INVALID")).status must be(401)
    }

    "use a valid API URL" in {
      val expected: URL = new URL(GitlabHelper.gitlabUrl + "/?private_token=" + GitlabHelper.gitlabToken)
      gitlabAPI.getAPIUrl("") must be(expected)
    }
  }
}
