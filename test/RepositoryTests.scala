import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._

class RepositoryTests extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val logger = Logger(classOf[RepositoryTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName
  var projectId = -1
  var commitSHA = ""

  override def beforeAll(): Unit = {
    running(FakeApplication()) {
      projectId = GitlabHelper.createTestProject
      commitSHA = (await(gitlabAPI.getBranch(projectId, "master")).json \ "commit" \ "id").as[String]
      logger.debug("Starting Branches tests")
    }
  }

  override def afterAll() {
    running(FakeApplication()) {
      GitlabHelper.deleteTestProject()
      //        super.afterAll()
      logger.debug("End of GitlabAPI Branches tests")
    }
  }

  "GitlabAPI must manage a repository " should {

    "get all tags of a project" in {
      val response = await(gitlabAPI.getRepositoryTags(projectId))
      response.status must be(200)
    }

    "create a new tag for a project" in {
      val response = await(gitlabAPI.createTag(projectId, "tag_name", commitSHA))
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
