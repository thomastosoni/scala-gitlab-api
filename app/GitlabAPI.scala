import java.net.URL

import com.github.tototoshi.play2.json4s.native.Json4s
import models.{GitlabProject, GitlabSession, GitlabUser, SSHKey}
import org.json4s._
import play.api.Logger
import play.api.Play.current
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc._

import scala.concurrent.Future

class GitlabAPI(gitlabUrl: String, gitlabToken: String) extends Controller with Json4s {
  // To allow json4s deserialization through Extraction.decompose
  implicit val formats = DefaultFormats

  // Implicit context for the Play Framework
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  val logger = Logger("GitlabAPI")
  val authToken = "PRIVATE-TOKEN" -> gitlabToken

  def getAPIUrl(tailAPIUrl: String): URL = {
    var fullUrl = tailAPIUrl
    fullUrl = tailAPIUrl + (if (tailAPIUrl.indexOf('?') > 0) '&' else '?') + "private_token=" + gitlabToken
    if (!fullUrl.startsWith("/"))
      fullUrl = "/" + fullUrl
    new URL(gitlabUrl + fullUrl)
  }

  /**
   * Session
   */

  def connectToSession(login: String, password: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/session").post(Extraction.decompose(GitlabSession(login, None, password)))
  }


  /**
   * Users
   */

  def getUser(userId: Int) = {
    WS.url(gitlabUrl + "/users/" + userId).withHeaders(authToken).get()
  }

  def getUserViaSudo(userId: Int) = {
    WS.url(gitlabUrl + "/users/" + userId).withHeaders(authToken, "SUDO" -> userId.toString).get()
  }

  def getUsers: Future[WSResponse] = {
    WS.url(gitlabUrl + "/users").withHeaders(authToken).get()
  }

  def getUserByUsername(username: String) = {
    WS.url(gitlabUrl + "/users").withHeaders(authToken).withQueryString("search" -> username).get()
  }

  def getUserByEmail(email: String) = {
    WS.url(gitlabUrl + "/users").withHeaders(authToken).withQueryString("search" -> email).get()
  }


  def createUser(email: String,
                 password: String,
                 username: String,
                 name: String,
                 admin: Option[Boolean]): Future[WSResponse] = {
    val holder = WS.url(gitlabUrl + "/users")
    holder.withHeaders(authToken).post(Extraction.decompose(GitlabUser(email, password, username, name, admin)))
  }

  def updateUser(userId: Int,
                 email: String,
                 password: String,
                 username: String,
                 name: String,
                 admin: Option[Boolean] = Option(false)): Future[WSResponse] = {
    val holder = WS.url(gitlabUrl + "/users/" + userId)
    holder.withHeaders(authToken).put(Extraction.decompose(GitlabUser(email, password, username, name, admin)))
  }

  // TODO change this: id != 1 to avoid deleting the administrator account...
  def deleteUser(userId: Int): Future[WSResponse] = {
    var uid = userId
    if (uid == 1) uid = 0
    WS.url(gitlabUrl + "/users/" + uid).withHeaders(authToken).delete()
  }

  /**
   * SSH Keys
   */

  def getSSHKeys: Future[WSResponse] = {
    WS.url(gitlabUrl + "/user/keys").withHeaders(authToken).get()
  }

  def getUserSSHKeys(userId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/users/" + userId + "/keys").withHeaders(authToken).get()
  }

  def getSSHKey(keyId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/user/keys/" + keyId).withHeaders(authToken).get()
  }

  def addSSHKey(title: String, key: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/user/keys").withHeaders(authToken).post(Extraction.decompose(SSHKey(title, key)))
  }

  def deleteSSHKey(keyId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/user/keys/" + keyId).withHeaders(authToken).delete()
  }

  def deleteUserSSHKey(userId: Int, keyId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/users/" + userId + "/keys/" + keyId).withHeaders(authToken).delete()
  }

  /**
   * Projects
   */

  def getProjects(archivedOpt: Option[Boolean]): Future[WSResponse] = {
    val archived = archivedOpt exists { archivedResult => archivedResult }
    WS.url(gitlabUrl + "/projects").withHeaders(authToken).withQueryString("archived" -> archived.toString).get()
  }

  def getOwnedProjects: Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/owned").withHeaders(authToken).get()
  }

  def getAllProjects: Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/all").withHeaders(authToken).get()
  }

  def getProject(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId).withHeaders(authToken).get()
  }

  def getProjectEvents(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/events").withHeaders(authToken).get()
  }

  def createProject(name: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects").withHeaders(authToken).post(Extraction.decompose(GitlabProject(name)))
  }

  //TODO why post id? fork another users'project in tests
  //  def forkProject(projectId: Int): Future[WSResponse] = {
  //    WS.url(gitlabUrl + "/projects/forks/" + projectId).withHeaders(authToken).post(Json.obj("id" -> projectId))
  //  }

  def deleteProject(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId).withHeaders(authToken).delete()
  }

  /**
   * Team
   */

  //...

}
