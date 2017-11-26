import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger

class RepositoryTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[RepositoryTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  val tagName = "tag"
  var projectId = -1
  var commitSHA = ""

  override def beforeAll(): Unit = {
    GitlabHelper.createTestSSHKey
    projectId = GitlabHelper.createTestProject
    val branchResponse = await(gitlabAPI.getBranch(projectId, "master"))
    if (branchResponse.status == 200) {
      commitSHA = (branchResponse.json \ "commit" \ "id").as[String]
    } else logger.error("Before All: Didn't create test branch")
    logger.debug("Starting Repository Tests")
  }

  override def afterAll() {
    GitlabHelper.deleteTestSSHKey()
    GitlabHelper.deleteTestProject()
    logger.debug("End of Repository Tests")
    Thread.sleep(1000L)
  }

  "GitlabAPI must manage a repository " should {

    "get all tags of a project" in {
      val response = await(gitlabAPI.getRepositoryTags(projectId))
      response.status must be(200)
    }

    "create a new tag for a project" in {
      val response = await(gitlabAPI.createTag(projectId, tagName, commitSHA))
      response.status must be(201)
    }

    "list a repository tree" in {
      await(gitlabAPI.getRepositoryTree(projectId)).status must be(200)
    }

    "get raw file content for a file by commit SHA" in {
      await(gitlabAPI.getRawFileContent(projectId, commitSHA, "README.md")).status must be(200)
    }

    //    "get raw file content for a blob by blob SHA" in {
    //      await(gitlabAPI.getRawBlobContent(projectId, blobSHA)).status must be(200)
    //    }

    "get an archive from the repository" in {
      await(gitlabAPI.getArchive(projectId)).status must be(200)
    }

    //    "compare branches" in {
    //      await(gitlabAPI.compare(projectId, commitSHA, firstCommitSHA))
    //    }

    "get the contributors" in {
      await(gitlabAPI.getContributors(projectId)).status must be(200)
    }
  }

}
