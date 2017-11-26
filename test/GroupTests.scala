import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Logger

class GroupTests extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {
  lazy val logger = Logger(classOf[GroupTests])

  val gitlabAPI = GitlabHelper.gitlabAPI
  val projectName = GitlabHelper.projectName

  val groupName = "Test Group"
  val groupPath= "group"
  var projectId = -1
  var groupId = -1
  var userId = -1

  override def beforeAll(): Unit = {
      userId = GitlabHelper.createTestUser
      projectId = GitlabHelper.createEmptyTestProject
      logger.debug("Starting Group Tests")
  }

  override def afterAll() {
      GitlabHelper.deleteTestUser()
      GitlabHelper.deleteTestProject()
      val response = await(gitlabAPI.deleteGroup(groupId))
      GitlabHelper.checkDeleteAfterTest(response, GROUP)
      logger.debug("End of Group Tests")
      Thread.sleep(1000L)
  }

  "GitlabAPI must manage groups" should {

    "add a group" in {
      val response = await(gitlabAPI.addGroup(groupName, groupPath))
      response.status must be(201)
      groupId = (response.json \ "id").as[Int]
    }

    "get all groups" in {
      await(gitlabAPI.getGroups).status must be(200)
    }

    "get a group" in {
      await(gitlabAPI.getGroup(groupId)).status must be(200)
    }

    "transfer project to group" in {
      await(gitlabAPI.transferProjectToGroup(groupId, projectId)).status must be(201)
    }

    "add group member" in {
      await(gitlabAPI.addGroupMember(groupId, userId, 40)).status must be(201)
    }

    "get group member" in {
      await(gitlabAPI.getGroupMembers(groupId)).status must be(200)
    }

    "remove group member" in {
      await(gitlabAPI.removeGroupMember(groupId, userId)).status must be(200)
    }

    "delete group" in {
      val response = await(gitlabAPI.deleteGroup(groupId))
      response.status must be(200)
      response.json must not be null
    }
  }

}
