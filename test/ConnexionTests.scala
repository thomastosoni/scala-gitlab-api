import java.net.URL

import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.Helpers._

class ConnexionTests extends PlaySpec with OneAppPerSuite with BeforeAndAfter {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  val gitlabAPI = GitlabHelper.gitlabAPI

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
