import java.net.URL

import com.github.tototoshi.play2.json4s.native.Json4s
import models._
import org.json4s._
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc._
import org.json4s.JsonDSL._

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
    WS.url(gitlabUrl + "/session").post(Extraction.decompose(GitlabSession(login, None, password)).underscoreKeys)
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
                 skype: Option[String] = None,
                 linkedin: Option[String] = None,
                 twitter: Option[String] = None,
                 websiteUrl: Option[String] = None,
                 projectsLimit: Option[Int] = None,
                 externUid: Option[String] = None,
                 provider: Option[String] = None,
                 bio: Option[String] = None,
                 admin: Option[Boolean] = None,
                 canCreateGroup: Option[Boolean] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/users").withHeaders(authToken)
      .post(Extraction.decompose(User(
      email,
      password,
      username,
      name,
      skype,
      linkedin,
      twitter,
      websiteUrl,
      projectsLimit,
      externUid,
      provider,
      bio,
      admin,
      canCreateGroup
    )).underscoreKeys)
  }

  def updateUser(userId: Int,
                 email: String,
                 password: String,
                 username: String,
                 name: String,
                 skype: Option[String] = None,
                 linkedin: Option[String] = None,
                 twitter: Option[String] = None,
                 websiteUrl: Option[String] = None,
                 projectsLimit: Option[Int] = None,
                 externUid: Option[String] = None,
                 provider: Option[String] = None,
                 bio: Option[String] = None,
                 admin: Option[Boolean] = None,
                 canCreateGroup: Option[Boolean] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/users/" + userId).withHeaders(authToken)
      .put(Extraction.decompose(User(
      email,
      password,
      username,
      name,
      skype,
      linkedin,
      twitter,
      websiteUrl,
      projectsLimit,
      externUid,
      provider,
      bio,
      admin,
      canCreateGroup
    )).underscoreKeys)
  }

  def deleteUser(userId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/users/" + userId).withHeaders(authToken).delete()
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
    WS.url(gitlabUrl + "/user/keys").withHeaders(authToken).post(Extraction.decompose(SSHKey(title, key)).underscoreKeys)
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

  def getProjects(archived: Option[Boolean] = None,
                  orderBy: Option[String] = None,
                  sort: Option[String] = None,
                  search: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects").withHeaders(authToken).withQueryString(
      "archived" -> archived.map(archived => archived.toString).orNull,
      "order_by" -> orderBy.orNull,
      "sort" -> sort.orNull,
      "search" -> search.orNull
    ).get()
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

  // TODO should work too
  //  def getProject(nameOrNamespace: String): Future[WSResponse] = {
  //    val url = gitlabUrl + "/projects/" + nameOrNamespace + java.net.URLEncoder.encode("/" + nameOrNamespace, "UTF-8")
  //    logger.error(url)
  //    WS.url(url).withHeaders(authToken).get()
  //  }

  def getProject(projectName: String,
                 perPage: Option[String] = None,
                 page: Option[String] = None,
                 orderBy: Option[String] = None,
                 sort: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/search/" + projectName).withHeaders(authToken).withQueryString(
      "per_page" -> perPage.orNull,
      "page" -> page.orNull,
      "order_by" -> orderBy.orNull,
      "sort" -> sort.orNull
    ).get()
  }

  def getProjectEvents(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/events").withHeaders(authToken).get()
  }

  def createProject(name: String,
                    path: Option[String] = None,
                    namespaceId: Option[Int] = None,
                    description: Option[String] = None,
                    issuesEnabled: Option[Boolean] = None,
                    mergeRequestsEnabled: Option[Boolean] = None,
                    wikiEnabled: Option[Boolean] = None,
                    snippetsEnabled: Option[Boolean] = None,
                    public: Option[Boolean] = None,
                    visibilityLevel: Option[Int] = None,
                    importUrl: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects").withHeaders(authToken).post(Extraction.decompose(
      Project(
        name,
        path,
        namespaceId,
        description,
        issuesEnabled,
        mergeRequestsEnabled,
        wikiEnabled,
        snippetsEnabled,
        public,
        visibilityLevel,
        importUrl
      )).underscoreKeys
    )
  }

  def createProjectForUser(userId: Int,
                           name: String,
                           path: Option[String] = None,
                           namespaceId: Option[Int] = None,
                           description: Option[String] = None,
                           issuesEnabled: Option[Boolean] = None,
                           mergeRequestsEnabled: Option[Boolean] = None,
                           wikiEnabled: Option[Boolean] = None,
                           snippetsEnabled: Option[Boolean] = None,
                           public: Option[Boolean] = None,
                           visibilityLevel: Option[Int] = None,
                           importUrl: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/user/" + userId).withHeaders(authToken).post(Extraction.decompose(
      Project(
        name,
        path,
        namespaceId,
        description,
        issuesEnabled,
        mergeRequestsEnabled,
        wikiEnabled,
        snippetsEnabled,
        public,
        visibilityLevel,
        importUrl
      )).underscoreKeys
    )
  }

  // TODO 404. Connect as another user?
  //  def forkProject(projectId: Int): Future[WSResponse] = {
  //    WS.url(gitlabUrl + "/projects/forks/" + projectId).withHeaders(authToken).post(Json.obj("id" -> projectId))
  //  }

  def deleteProject(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId).withHeaders(authToken).delete()
  }

  /**
   * Team
   */

  def getTeamMembers(projectId: Int, query: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/members").withHeaders(authToken).withQueryString("query" -> query.orNull).get()
  }

  // TODO Project Name?
  //  def getTeamMembersByName(projectName: String, query: Option[String] = None): Future[WSResponse] = {
  //    WS.url(gitlabUrl + "/projects/" + projectName + "/members").withHeaders(authToken).withQueryString("query" -> query.orNull).get()
  //  }

  def getTeamMember(projectId: Int, userId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/members/" + userId).withHeaders(authToken).get()
  }

  //  def getTeamMember(projectName: String, userId: Int): Future[WSResponse] = {
  //    WS.url(gitlabUrl + "/projects/" + projectName + "/members/" + userId).withHeaders(authToken).get()
  //  }

  def addTeamMember(projectId: Int, userId: Int, accessLevel: Int): Future[WSResponse] = {
    val json = ("id" -> projectId) ~ ("user_id" -> userId) ~ ("access_level" -> accessLevel)
    WS.url(gitlabUrl + "/projects/" + projectId + "/members").withHeaders(authToken).post(json)
  }

  def updateTeamMemberAccessLevel(projectId: Int, userId: Int, accessLevel: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/members/" + userId).withHeaders(authToken)
      .put(Json.obj("access_level" -> accessLevel))
  }

  def deleteTeamMember(projectId: Int, userId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/members/" + userId).withHeaders(authToken).delete()
  }

  /**
   * Project Hooks
   */

  def getHook(projectId: Int, hookId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/hooks/" + hookId).withHeaders(authToken).get()
  }

  def addHook(projectId: Int, hookUrl: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/hooks").withHeaders(authToken)
      .post(Extraction.decompose(Hook(hookUrl, projectId)).underscoreKeys)
  }

  def updateHook(projectId: Int, hookId: Int, hookUrl: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/hooks/" + hookId).withHeaders(authToken).put(Json.obj("url" -> hookUrl))
  }

  def deleteHook(projectId: Int, hookId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/hooks/" + hookId).withHeaders(authToken).delete()
  }

  /**
   * Branches
   */

  def getBranches(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/branches").withHeaders(authToken).get()
  }

  def getBranch(projectId: Int, branch: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/branches/" + branch).withHeaders(authToken).get()
  }

  def protectBranch(projectId: Int, branchName: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/branches/" + branchName + "/protect").withHeaders(authToken)
      .put(Json.obj("branch_name" -> branchName))
  }

  def unprotectBranch(projectId: Int, branchName: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/branches/" + branchName + "/unprotect").withHeaders(authToken)
      .put(Json.obj("branch_name" -> branchName))
  }

  def createBranch(projectId: Int, branchName: String, ref: String): Future[WSResponse] = {
    val newBranch = ("branch_name" -> branchName) ~ ("ref" -> ref)
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/branches").withHeaders(authToken).post(newBranch)
  }

  def deleteBranch(projectId: Int, branchName: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/branches/" + branchName).withHeaders(authToken).delete()
  }

  /**
   * Project Snippet
   */

  def getSnippets(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/snippets").withHeaders(authToken).get()
  }

  def getSnippet(projectId: Int, snippetId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId).withHeaders(authToken).get()
  }

  def getRawSnippet(projectId: Int, snippetId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId + "/raw").withHeaders(authToken).get()
  }

  def createSnippet(projectId: Int, title: String, fileName: String, code: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/snippets/").withHeaders(authToken)
      .post(Extraction.decompose(Snippet(title, fileName, code)).underscoreKeys)
  }

  def updateSnippet(projectId: Int,
                    snippetId: Int,
                    title: Option[String] = None,
                    fileName: Option[String] = None,
                    code: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId).withHeaders(authToken)
      .put(Extraction.decompose(Snippet(title.orNull, fileName.orNull, code.orNull)).underscoreKeys)
  }

  def deleteSnippet(projectId: Int, snippetId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId).withHeaders(authToken).delete()
  }

  /**
   * Repositories
   */

  def getRepositoryTags(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/tags").withHeaders(authToken).get()
  }

  def createTag(projectId: Int, tagName: String, ref: String, message: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/tags").withHeaders(authToken)
      .post(Extraction.decompose(Tag(tagName, ref, message)).underscoreKeys)
  }

  def getRepositoryTree(projectId: Int, path: Option[String] = None, ref_name: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/tree").withHeaders(authToken)
      .withQueryString("path" -> path.orNull, "ref_name" -> ref_name.orNull).get()
  }

  def getRawFileContent(projectId: Int, commitSHAOrBranchName: String, filePath: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/blobs/" + commitSHAOrBranchName).withHeaders(authToken)
      .withQueryString("filepath" -> filePath).get()
  }

  def getRawBlobContent(projectId: Int, sha: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/raw_blobs/" + sha).withHeaders(authToken).get()
  }

  def getArchive(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/archive").withHeaders(authToken).get()
  }

  def getContributors(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/contributors").withHeaders(authToken).get()
  }

  def compare(projectId: Int, from: String, to: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/compare").withHeaders(authToken)
      .withQueryString("from" -> from, "to" -> to).get()
  }

  /**
   * Files
   */

  def getFile(projectId: Int, filePath: String, ref: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/files").withHeaders(authToken)
      .withQueryString("file_path" -> filePath, "ref" -> ref).get()
  }

  def createFile(projectId: Int,
                 filePath: String,
                 branchName: String,
                 content: String,
                 commitMessage: String,
                 encoding: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/files").withHeaders(authToken)
      .post(Extraction.decompose(File(filePath, branchName, content, commitMessage, encoding)).underscoreKeys)
  }

  def updateFile(projectId: Int,
                 filePath: String,
                 branchName: String,
                 content: String,
                 commitMessage: String,
                 encoding: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/files").withHeaders(authToken)
      .put(Extraction.decompose(File(filePath, branchName, content, commitMessage, encoding)).underscoreKeys)
  }

  def deleteFile(projectId: Int,
                 filePath: String,
                 branchName: String,
                 commitMessage: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/files").withHeaders(authToken)
      .withQueryString("file_path" -> filePath, "branch_name" -> branchName, "commit_message" -> commitMessage).delete()
  }

  /**
   * Commits
   */

  def getCommits(projectId: Int, ref: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/commits").withHeaders(authToken)
      .withQueryString("ref_name" -> ref.orNull).get()
  }

  def getCommit(projectId: Int, sha: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/commits/" + sha).withHeaders(authToken).get()
  }

  def getDiff(projectId: Int, sha: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/commits/" + sha + "/diff").withHeaders(authToken).get()
  }

  def getCommitComments(projectId: Int, sha: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/commits/" + sha + "/comments").withHeaders(authToken).get()
  }

  def addCommitComments(projectId: Int,
                        sha: String,
                        note: String,
                        path: Option[String] = None,
                        line: Option[Int] = None,
                        line_type: Option[String] = None): Future[WSResponse] = {
    val json = ("id" -> projectId) ~ ("sha" -> sha) ~ ("note" -> note) ~
      ("path" -> path) ~ ("line" -> line) ~ ("line_type" -> line_type)
    WS.url(gitlabUrl + "/projects/" + projectId + "/repository/commits/" + sha + "/comments").withHeaders(authToken).post(json)
  }

  /**
   * Merge requests
   */

  def getMergeRequests(projectId: Int,
                       state: Option[String] = None,
                       order_by: Option[String] = None,
                       sort: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_requests").withHeaders(authToken)
      .withQueryString("state" -> state.orNull, "order_by" -> order_by.orNull, "sort" -> sort.orNull).get()
  }

  def getMergeRequest(projectId: Int, mergeReqId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_request/" + mergeReqId).withHeaders(authToken).get()
  }

  def addMergeRequest(projectId: Int,
                      sourceBranch: String,
                      targetBranch: String,
                      title: String,
                      assigneeId: Option[Int] = None,
                      targetProjectId: Option[Int] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_requests").withHeaders(authToken)
      .post(Extraction.decompose(MergeRequest(sourceBranch, targetBranch, title, assigneeId, targetProjectId)).underscoreKeys)
  }

  def updateMergeRequest(projectId: Int,
                         mergeRequestId: Int,
                         sourceBranch: Option[String] = None,
                         targetBranch: Option[String] = None,
                         assigneeId: Option[Int] = None,
                         title: Option[String] = None,
                         stateEvent: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_request/" + mergeRequestId).withHeaders(authToken)
      .put(Extraction.decompose(MergeRequest(sourceBranch.orNull, targetBranch.orNull, title.orNull, assigneeId)).underscoreKeys)
  }

  def acceptMergeRequest(projectId: Int,
                         mergeRequestId: Int,
                         mergeCommitMessage: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_request/" + mergeRequestId + "/merge").withHeaders(authToken)
      .put(Json.obj("merge_commit_message" -> mergeCommitMessage))
  }

  def addMergeRequestComment(projectId: Int, mergeRequestId: Int, note: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_request/" + mergeRequestId + "/comments").withHeaders(authToken)
      .post(Json.obj("note" -> note))
  }

  def getMergeRequestComments(projectId: Int, mergeRequestId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_request/" + mergeRequestId + "/comments")
      .withHeaders(authToken).get()
  }

  /**
   * Issues
   */

  def getAllIssues(state: Option[String] = None, labels: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/issues").withHeaders(authToken).withQueryString("state" -> state.orNull, "labels" -> labels.orNull).get()
  }

  def getIssues(projectId: Int,
                state: Option[String] = None,
                labels: Option[String] = None,
                milestone: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/issues").withHeaders(authToken)
      .withQueryString("state" -> state.orNull, "labels" -> labels.orNull, "milestone" -> milestone.orNull).get()
  }

  def getIssue(projectId: Int, issueId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId).withHeaders(authToken).get()
  }

  def addIssue(projectId: Int,
               title: String,
               description: Option[String] = None,
               assigneeId: Option[String] = None,
               milestoneId: Option[String] = None,
               labels: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/issues").withHeaders(authToken)
      .post(Extraction.decompose(Issue(Option(title), description, assigneeId, milestoneId, labels, None)).underscoreKeys)
  }


  // To delete an issue, set the stateEvent to 'closed'
  def updateIssue(projectId: Int,
                  issueId: Int,
                  title: Option[String] = None,
                  description: Option[String] = None,
                  assigneeId: Option[String] = None,
                  milestoneId: Option[String] = None,
                  labels: Option[String] = None,
                  state: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId).withHeaders(authToken)
      .put(Extraction.decompose(Issue(title, description, assigneeId, milestoneId, labels, state)).underscoreKeys)
  }

  def closeIssue(projectId: Int, issueId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId).withHeaders(authToken).put(Json.obj("state_event" -> "close"))
  }

  /**
   * Labels
   */

  def getLabels(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/labels").withHeaders(authToken).get()
  }

  def addLabel(projectId: Int,
               name: String,
               color: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/labels").withHeaders(authToken)
      .post(Extraction.decompose(Label(name, color)).underscoreKeys)
  }

  def updateLabel(projectId: Int,
                  name: String,
                  newName: Option[String] = None,
                  color: Option[String] = None): Future[WSResponse] = {
    val updatedLabel = ("name" -> name) ~ ("new_name" -> newName.orNull) ~ ("color" -> color.orNull)
    WS.url(gitlabUrl + "/projects/" + projectId + "/labels").withHeaders(authToken).put(updatedLabel)
  }

  def deleteLabel(projectId: Int, name: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/labels").withHeaders(authToken)
      .withQueryString("name" -> name).delete()
  }


  /**
   * Milestones
   */
  def getMilestones(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/milestones").withHeaders(authToken).get()
  }

  def getMilestone(projectId: Int, milestoneId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/milestones/" + milestoneId).withHeaders(authToken).get()
  }

  def addMilestone(projectId: Int,
                   title: String,
                   description: Option[String] = None,
                   dueDate: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/milestones").withHeaders(authToken)
      .post(Extraction.decompose(Milestone(title, description, dueDate, None)).underscoreKeys)
  }

  def updateMilestone(projectId: Int,
                      milestoneId: Int,
                      title: String,
                      description: Option[String] = None,
                      dueDate: Option[String] = None,
                      stateEvent: Option[String] = None): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/milestones/" + milestoneId).withHeaders(authToken)
      .put(Extraction.decompose(Milestone(title, description, dueDate, stateEvent)).underscoreKeys)
  }

  def closeMilestone(projectId: Int, milestoneId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/milestones/" + milestoneId).withHeaders(authToken)
      .put(Json.obj("state_event" -> "close"))
  }

  /**
   * Issue Notes
   */

  def getIssueNotes(projectId: Int, issueId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId + "/notes").withHeaders(authToken).get()
  }

  def getIssueNote(projectId: Int, issueId: Int, noteId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId + "/notes/" + noteId).withHeaders(authToken).get()
  }

  def addIssueNote(projectId: Int, issueId: Int, body: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId + "/notes").withHeaders(authToken)
      .post(Json.obj("body" -> body))
  }

  def updateIssueNote(projectId: Int, issueId: Int, noteId: Int, body: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId + "/notes/" + noteId).withHeaders(authToken)
      .put(Json.obj("body" -> body))
  }

  /**
   * Snippet Notes
   */

  def getSnippetNotes(projectId: Int, snippetId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId + "/notes").withHeaders(authToken).get()
  }

  def getSnippetNote(projectId: Int, snippetId: Int, noteId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId + "/notes/" + noteId).withHeaders(authToken).get()
  }

  def addSnippetNote(projectId: Int, snippetId: Int, body: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId + "/notes").withHeaders(authToken)
      .post(Json.obj("body" -> body))
  }

  def updateSnippetNote(projectId: Int, snippetId: Int, noteId: Int, body: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId + "/notes/" + noteId).withHeaders(authToken)
      .put(Json.obj("body" -> body))
  }


  /**
   * Merge Request Notes
   */

  def getMergeRequestNotes(projectId: Int, mergeRequestId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_requests/" + mergeRequestId + "/notes").withHeaders(authToken).get()
  }

  def getMergeRequestNote(projectId: Int, mergeRequestId: Int, noteId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_requests/" + mergeRequestId + "/notes/" + noteId).withHeaders(authToken).get()
  }

  def addMergeRequestNote(projectId: Int, mergeRequestId: Int, body: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_requests/" + mergeRequestId + "/notes").withHeaders(authToken)
      .post(Json.obj("body" -> body))
  }

  def updateMergeRequestNote(projectId: Int, mergeRequestId: Int, noteId: Int, body: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/merge_requests/" + mergeRequestId + "/notes/" + noteId).withHeaders(authToken)
      .put(Json.obj("body" -> body))
  }

  /**
   * Deploy Keys
   */

  def getDeployKeys(projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/keys").withHeaders(authToken).get()
  }

  def getDeployKey(projectId: Int, keyId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/keys/" + keyId).withHeaders(authToken).get()
  }

  def addDeployKey(projectId: Int, title: String, key: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/keys").withHeaders(authToken).post(("title" -> title) ~ ("key" -> key))
  }

  def deleteDeployKey(projectId: Int, keyId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/projects/" + projectId + "/keys/" + keyId).withHeaders(authToken).delete()
  }

  /**
   * System Hooks
   */

  def getHooks: Future[WSResponse] = WS.url(gitlabUrl + "/hooks").withHeaders(authToken).get()

  def getHook(hookId: Int): Future[WSResponse] = WS.url(gitlabUrl + "/hooks/" + hookId).withHeaders(authToken).get()

  def addHook(hookUrl: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/hooks").withHeaders(authToken).post(Json.obj("url" -> hookUrl))
  }

  def deleteHook(hookId: Int): Future[WSResponse] = WS.url(gitlabUrl + "/hooks/" + hookId).withHeaders(authToken).delete()

  /**
   * Groups
   */

  def getGroups: Future[WSResponse] = WS.url(gitlabUrl + "/groups").withHeaders(authToken).get()

  def getGroup(groupId: Int): Future[WSResponse] = WS.url(gitlabUrl + "/groups/" + groupId).withHeaders(authToken).get()

  def addGroup(name: String, path: String): Future[WSResponse] = {
    WS.url(gitlabUrl + "/groups").withHeaders(authToken).post(("name" -> name) ~ ("path" -> path))
  }

  def transferProjectToGroup(groupId: Int, projectId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/groups/" + groupId + "/projects/" + projectId).withHeaders(authToken)
      .post(("group_id" -> groupId) ~ ("projectId" -> projectId))
  }

  def deleteGroup(groupId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/groups/" + groupId).withHeaders(authToken).delete()
  }

  def getGroupMembers(groupId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/groups/" + groupId + "/members").withHeaders(authToken).get()
  }

  def addGroupMember(groupId: Int, userId: Int, accessLevel: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/groups/" + groupId + "/members").withHeaders(authToken)
      .post(("user_id" -> userId) ~ ("access_level" -> accessLevel))
  }

  def removeGroupMember(groupId: Int, userId: Int): Future[WSResponse] = {
    WS.url(gitlabUrl + "/groups/" + groupId + "/members/" + userId).withHeaders(authToken).delete()
  }
}
