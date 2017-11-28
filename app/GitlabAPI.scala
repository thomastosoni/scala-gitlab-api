import java.net.URL
import javax.inject.{Inject, Singleton}

import com.github.tototoshi.play.json4s.ws.native.Json4sBodyWritables._
import com.github.tototoshi.play2.json4s.native.Json4s
import models._
import org.json4s.JsonDSL._
import org.json4s._
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._

import scala.concurrent.Future

@Singleton
class GitlabAPI @Inject()(ws: WSClient,
                          gitlabUrl: String,
                          gitlabToken: String) extends InjectedController {
  // To allow json4s deserialization through Extraction.decompose
  implicit val formats: DefaultFormats.type = DefaultFormats

  val logger: Logger = Logger("GitlabAPI")
  val authToken: (String, String) = "PRIVATE-TOKEN" -> gitlabToken

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
    ws.url(gitlabUrl + "/session").post(Extraction.decompose(GitlabSession(login, None, password)).underscoreKeys)
  }

  /**
    * Users
    */

  def getUser(userId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/users/" + userId).withHttpHeaders(authToken).get()
  }

  def getUserViaSudo(userId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/users/" + userId).withHttpHeaders(authToken, "SUDO" -> userId.toString).get()
  }

  def getUsers: Future[WSResponse] = {
    println(gitlabUrl + "/users")
    ws.url(gitlabUrl + "/users").withHttpHeaders(authToken).get()
  }

  def getUserByUsername(username: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/users").withHttpHeaders(authToken).withQueryStringParameters("search" -> username).get()
  }

  def getUserByEmail(email: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/users").withHttpHeaders(authToken).withQueryStringParameters("search" -> email).get()
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
    println(gitlabUrl + "/users")
    println(authToken)
    ws.url(gitlabUrl + "/users").withHttpHeaders(authToken)
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
    ws.url(gitlabUrl + "/users/" + userId).withHttpHeaders(authToken)
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
    ws.url(gitlabUrl + "/users/" + userId).withHttpHeaders(authToken).delete()
  }

  /**
    * SSH Keys
    */

  def getSSHKeys: Future[WSResponse] = {
    ws.url(gitlabUrl + "/user/keys").withHttpHeaders(authToken).get()
  }

  def getUserSSHKeys(userId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/users/" + userId + "/keys").withHttpHeaders(authToken).get()
  }

  def getSSHKey(keyId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/user/keys/" + keyId).withHttpHeaders(authToken).get()
  }

  def addSSHKey(title: String, key: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/user/keys").withHttpHeaders(authToken).post(Extraction.decompose(SSHKey(title, key)).underscoreKeys)
  }

  def deleteSSHKey(keyId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/user/keys/" + keyId).withHttpHeaders(authToken).delete()
  }

  def deleteUserSSHKey(userId: Int, keyId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/users/" + userId + "/keys/" + keyId).withHttpHeaders(authToken).delete()
  }

  /**
    * Projects
    */

  def getProjects(archived: Option[Boolean] = None,
                  visibility: Option[String] = None,
                  orderBy: Option[String] = None,
                  sort: Option[String] = None,
                  search: Option[String] = None,
                  simple: Option[Boolean] = None,
                  owned: Option[Boolean] = None,
                  membership: Option[Boolean] = None,
                  starred: Option[Boolean] = None,
                  statistics: Option[Boolean] = None,
                  withIssuesEnabled: Option[Boolean] = None,
                  withMergeRequestsEnabled: Option[Boolean] = None): Future[WSResponse] = {
    val queryStringParams = List(
      archived.map("archived" -> _.toString),
      visibility.map("visibility" -> _.toString),
      orderBy.map("order_by" -> _.toString),
      sort.map("sort" -> _.toString),
      search.map("search" -> _.toString),
      simple.map("simple" -> _.toString),
      owned.map("owned" -> _.toString),
      membership.map("membership" -> _.toString),
      starred.map("starred" -> _.toString),
      statistics.map("statistics" -> _.toString),
      withIssuesEnabled.map("with_issues_enabled" -> _.toString),
      withMergeRequestsEnabled.map("with_merge_requests_enabled" -> _.toString)
    ).flatten

    ws.url(gitlabUrl + "/projects").withHttpHeaders(authToken).withQueryStringParameters(queryStringParams: _*).get()
  }

  def getProject(projectId: Int, statistics: Option[Boolean] = None): Future[WSResponse] = {
    val queryStringParams = List(statistics.map("statistics" -> _.toString)).flatten
    ws.url(gitlabUrl + "/projects/" + projectId).withHttpHeaders(authToken).withQueryStringParameters(queryStringParams: _*).get()
  }

  def getProjectEvents(projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/events").withHttpHeaders(authToken).get()
  }

  def createProject(name: Option[String] = None,
                    path: Option[String] = None,
                    namespaceId: Option[Int] = None,
                    defaultBranch: Option[String] = None,
                    description: Option[String] = None,
                    issuesEnabled: Option[Boolean] = None,
                    mergeRequestsEnabled: Option[Boolean] = None,
                    jobsEnabled: Option[Boolean] = None,
                    wikiEnabled: Option[Boolean] = None,
                    snippetsEnabled: Option[Boolean] = None,
                    resolveOutdatedDiffDiscussions: Option[Boolean] = None,
                    containerRegistryEnabled: Option[Boolean] = None,
                    sharedRunnersEnabled: Option[Boolean] = None,
                    visibility: Option[String] = None,
                    importUrl: Option[String] = None,
                    publicJobs: Option[Boolean] = None,
                    onlyAllowMergeIfPipelineSucceeds: Option[Boolean] = None,
                    onlyAllowMergeIfAllDiscussionsAreResolved: Option[Boolean] = None,
                    lfsEnabled: Option[Boolean] = None,
                    requestAccessEnabled: Option[Boolean] = None,
                    tagList: Option[Array[String]] = None,
                    printingMergeRequestLinkEnabled: Option[Boolean] = None,
                    ciConfigPath: Option[String] = None,
                    repositoryStorage: Option[String] = None,
                    approvalsBeforeMerge: Option[Int] = None): Future[WSResponse] = {
    if (name.isEmpty && path.isEmpty) throw new IllegalArgumentException("Either name or path must be defined")

    ws.url(gitlabUrl + "/projects").withHttpHeaders(authToken).post(Extraction.decompose(
      Project(
        name = name,
        path = path,
        namespaceId = namespaceId,
        defaultBranch = defaultBranch,
        description = description,
        issuesEnabled = issuesEnabled,
        mergeRequestsEnabled = mergeRequestsEnabled,
        jobsEnabled = jobsEnabled,
        wikiEnabled = wikiEnabled,
        snippetsEnabled = snippetsEnabled,
        resolveOutdatedDiffDiscussions = resolveOutdatedDiffDiscussions,
        containerRegistryEnabled = containerRegistryEnabled,
        sharedRunnersEnabled = sharedRunnersEnabled,
        visibility = visibility,
        importUrl = importUrl,
        publicJobs = publicJobs,
        onlyAllowMergeIfPipelineSucceeds = onlyAllowMergeIfPipelineSucceeds,
        onlyAllowMergeIfAllDiscussionsAreResolved = onlyAllowMergeIfAllDiscussionsAreResolved,
        lfsEnabled = lfsEnabled,
        requestAccessEnabled = requestAccessEnabled,
        tagList = tagList,
        // TODO avatar
        avatar = None,
        printingMergeRequestLinkEnabled = printingMergeRequestLinkEnabled,
        ciConfigPath = ciConfigPath,
        repositoryStorage = repositoryStorage,
        approvalsBeforeMerge = approvalsBeforeMerge
      )).underscoreKeys
    )
  }

  def createProjectForUser(userId: Int,
                           name: String,
                           path: Option[String] = None,
                           namespaceId: Option[Int] = None,
                           defaultBranch: Option[String] = None,
                           description: Option[String] = None,
                           issuesEnabled: Option[Boolean] = None,
                           mergeRequestsEnabled: Option[Boolean] = None,
                           jobsEnabled: Option[Boolean] = None,
                           wikiEnabled: Option[Boolean] = None,
                           snippetsEnabled: Option[Boolean] = None,
                           resolveOutdatedDiffDiscussions: Option[Boolean] = None,
                           containerRegistryEnabled: Option[Boolean] = None,
                           sharedRunnersEnabled: Option[Boolean] = None,
                           visibility: Option[String] = None,
                           importUrl: Option[String] = None,
                           publicJobs: Option[Boolean] = None,
                           onlyAllowMergeIfPipelineSucceeds: Option[Boolean] = None,
                           onlyAllowMergeIfAllDiscussionsAreResolved: Option[Boolean] = None,
                           lfsEnabled: Option[Boolean] = None,
                           requestAccessEnabled: Option[Boolean] = None,
                           tagList: Option[Array[String]] = None,
                           printingMergeRequestLinkEnabled: Option[Boolean] = None,
                           ciConfigPath: Option[String] = None,
                           repositoryStorage: Option[String] = None,
                           approvalsBeforeMerge: Option[Int] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + s"/projects/user/$userId").withHttpHeaders(authToken).post(Extraction.decompose(
      Project(
        name = Some(name),
        path = path,
        namespaceId = namespaceId,
        defaultBranch = defaultBranch,
        description = description,
        issuesEnabled = issuesEnabled,
        mergeRequestsEnabled = mergeRequestsEnabled,
        jobsEnabled = jobsEnabled,
        wikiEnabled = wikiEnabled,
        snippetsEnabled = snippetsEnabled,
        resolveOutdatedDiffDiscussions = resolveOutdatedDiffDiscussions,
        containerRegistryEnabled = containerRegistryEnabled,
        sharedRunnersEnabled = sharedRunnersEnabled,
        visibility = visibility,
        importUrl = importUrl,
        publicJobs = publicJobs,
        onlyAllowMergeIfPipelineSucceeds = onlyAllowMergeIfPipelineSucceeds,
        onlyAllowMergeIfAllDiscussionsAreResolved = onlyAllowMergeIfAllDiscussionsAreResolved,
        lfsEnabled = lfsEnabled,
        requestAccessEnabled = requestAccessEnabled,
        tagList = tagList,
        // TODO avatar
        avatar = None,
        printingMergeRequestLinkEnabled = printingMergeRequestLinkEnabled,
        ciConfigPath = ciConfigPath,
        repositoryStorage = repositoryStorage,
        approvalsBeforeMerge = approvalsBeforeMerge
      )).underscoreKeys
    )
  }

  // TODO 404. Connect as another user?
  //  def forkProject(projectId: Int): Future[WSResponse] = {
  //    WS.url(gitlabUrl + "/projects/forks/" + projectId).withHttpHeaders(authToken).post(Json.obj("id" -> projectId))
  //  }

  def removeProject(projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId).withHttpHeaders(authToken).delete()
  }

  /**
    * Team
    */

  def getTeamMembers(projectId: Int, query: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/members").withHttpHeaders(authToken).withQueryStringParameters("query" -> query.orNull).get()
  }

  // TODO Project Name?
  //  def getTeamMembersByName(projectName: String, query: Option[String] = None): Future[WSResponse] = {
  //    WS.url(gitlabUrl + "/projects/" + projectName + "/members").withHttpHeaders(authToken).withQueryStringParameters("query" -> query.orNull).get()
  //  }

  def getTeamMember(projectId: Int, userId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/members/" + userId).withHttpHeaders(authToken).get()
  }

  //  def getTeamMember(projectName: String, userId: Int): Future[WSResponse] = {
  //    WS.url(gitlabUrl + "/projects/" + projectName + "/members/" + userId).withHttpHeaders(authToken).get()
  //  }

  def addTeamMember(projectId: Int, userId: Int, accessLevel: Int): Future[WSResponse] = {
    val json = ("id" -> projectId) ~ ("user_id" -> userId) ~ ("access_level" -> accessLevel)
    ws.url(gitlabUrl + "/projects/" + projectId + "/members").withHttpHeaders(authToken).post(json)
  }

  def updateTeamMemberAccessLevel(projectId: Int, userId: Int, accessLevel: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/members/" + userId).withHttpHeaders(authToken)
      .put(Json.obj("access_level" -> accessLevel))
  }

  def deleteTeamMember(projectId: Int, userId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/members/" + userId).withHttpHeaders(authToken).delete()
  }

  /**
    * Project Hooks
    */

  def getHook(projectId: Int, hookId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/hooks/" + hookId).withHttpHeaders(authToken).get()
  }

  def addHook(projectId: Int, hookUrl: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/hooks").withHttpHeaders(authToken)
      .post(Extraction.decompose(Hook(hookUrl, projectId)).underscoreKeys)
  }

  def updateHook(projectId: Int, hookId: Int, hookUrl: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/hooks/" + hookId).withHttpHeaders(authToken).put(Json.obj("url" -> hookUrl))
  }

  def deleteHook(projectId: Int, hookId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/hooks/" + hookId).withHttpHeaders(authToken).delete()
  }

  /**
    * Branches
    */

  def getBranches(projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/branches").withHttpHeaders(authToken).get()
  }

  def getBranch(projectId: Int, branch: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/branches/" + branch).withHttpHeaders(authToken).get()
  }

  def protectBranch(projectId: Int, branchName: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/branches/" + branchName + "/protect").withHttpHeaders(authToken)
      .put(Json.obj("branch_name" -> branchName))
  }

  def unprotectBranch(projectId: Int, branchName: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/branches/" + branchName + "/unprotect").withHttpHeaders(authToken)
      .put(Json.obj("branch_name" -> branchName))
  }

  def createBranch(projectId: Int, branchName: String, ref: String): Future[WSResponse] = {
    val newBranch = ("branch_name" -> branchName) ~ ("ref" -> ref)
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/branches").withHttpHeaders(authToken).post(newBranch)
  }

  def deleteBranch(projectId: Int, branchName: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/branches/" + branchName).withHttpHeaders(authToken).delete()
  }

  /**
    * Project Snippet
    */

  def getSnippets(projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/snippets").withHttpHeaders(authToken).get()
  }

  def getSnippet(projectId: Int, snippetId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId).withHttpHeaders(authToken).get()
  }

  def getRawSnippet(projectId: Int, snippetId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId + "/raw").withHttpHeaders(authToken).get()
  }

  def createSnippet(projectId: Int, title: String, fileName: String, code: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/snippets/").withHttpHeaders(authToken)
      .post(Extraction.decompose(Snippet(title, fileName, code)).underscoreKeys)
  }

  def updateSnippet(projectId: Int,
                    snippetId: Int,
                    title: Option[String] = None,
                    fileName: Option[String] = None,
                    code: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId).withHttpHeaders(authToken)
      .put(Extraction.decompose(Snippet(title.orNull, fileName.orNull, code.orNull)).underscoreKeys)
  }

  def deleteSnippet(projectId: Int, snippetId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId).withHttpHeaders(authToken).delete()
  }

  /**
    * Repositories
    */

  def getRepositoryTags(projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/tags").withHttpHeaders(authToken).get()
  }

  def createTag(projectId: Int, tagName: String, ref: String, message: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/tags").withHttpHeaders(authToken)
      .post(Extraction.decompose(Tag(tagName, ref, message)).underscoreKeys)
  }

  def getRepositoryTree(projectId: Int, path: Option[String] = None, ref_name: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/tree").withHttpHeaders(authToken)
      .withQueryStringParameters("path" -> path.orNull, "ref_name" -> ref_name.orNull).get()
  }

  def getRawFileContent(projectId: Int, commitSHAOrBranchName: String, filePath: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/blobs/" + commitSHAOrBranchName).withHttpHeaders(authToken)
      .withQueryStringParameters("filepath" -> filePath).get()
  }

  def getRawBlobContent(projectId: Int, sha: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/raw_blobs/" + sha).withHttpHeaders(authToken).get()
  }

  def getArchive(projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/archive").withHttpHeaders(authToken).get()
  }

  def getContributors(projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/contributors").withHttpHeaders(authToken).get()
  }

  def compare(projectId: Int, from: String, to: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/compare").withHttpHeaders(authToken)
      .withQueryStringParameters("from" -> from, "to" -> to).get()
  }

  /**
    * Files
    */

  def getFile(projectId: Int, filePath: String, ref: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/files").withHttpHeaders(authToken)
      .withQueryStringParameters("file_path" -> filePath, "ref" -> ref).get()
  }

  def createFile(projectId: Int,
                 filePath: String,
                 branchName: String,
                 content: String,
                 commitMessage: String,
                 encoding: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/files").withHttpHeaders(authToken)
      .post(Extraction.decompose(File(filePath, branchName, content, commitMessage, encoding)).underscoreKeys)
  }

  def updateFile(projectId: Int,
                 filePath: String,
                 branchName: String,
                 content: String,
                 commitMessage: String,
                 encoding: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/files").withHttpHeaders(authToken)
      .put(Extraction.decompose(File(filePath, branchName, content, commitMessage, encoding)).underscoreKeys)
  }

  def deleteFile(projectId: Int,
                 filePath: String,
                 branchName: String,
                 commitMessage: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/files").withHttpHeaders(authToken)
      .withQueryStringParameters("file_path" -> filePath, "branch_name" -> branchName, "commit_message" -> commitMessage).delete()
  }

  /**
    * Commits
    */

  def getCommits(projectId: Int, ref: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/commits").withHttpHeaders(authToken)
      .withQueryStringParameters("ref_name" -> ref.orNull).get()
  }

  def getCommit(projectId: Int, sha: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/commits/" + sha).withHttpHeaders(authToken).get()
  }

  def getDiff(projectId: Int, sha: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/commits/" + sha + "/diff").withHttpHeaders(authToken).get()
  }

  def getCommitComments(projectId: Int, sha: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/commits/" + sha + "/comments").withHttpHeaders(authToken).get()
  }

  def addCommitComments(projectId: Int,
                        sha: String,
                        note: String,
                        path: Option[String] = None,
                        line: Option[Int] = None,
                        line_type: Option[String] = None): Future[WSResponse] = {
    val json = ("id" -> projectId) ~ ("sha" -> sha) ~ ("note" -> note) ~
      ("path" -> path) ~ ("line" -> line) ~ ("line_type" -> line_type)
    ws.url(gitlabUrl + "/projects/" + projectId + "/repository/commits/" + sha + "/comments").withHttpHeaders(authToken).post(json)
  }

  /**
    * Merge requests
    */

  def getMergeRequests(projectId: Int,
                       state: Option[String] = None,
                       order_by: Option[String] = None,
                       sort: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_requests").withHttpHeaders(authToken)
      .withQueryStringParameters("state" -> state.orNull, "order_by" -> order_by.orNull, "sort" -> sort.orNull).get()
  }

  def getMergeRequest(projectId: Int, mergeReqId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_request/" + mergeReqId).withHttpHeaders(authToken).get()
  }

  def addMergeRequest(projectId: Int,
                      sourceBranch: String,
                      targetBranch: String,
                      title: String,
                      assigneeId: Option[Int] = None,
                      targetProjectId: Option[Int] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_requests").withHttpHeaders(authToken)
      .post(Extraction.decompose(MergeRequest(sourceBranch, targetBranch, title, assigneeId, targetProjectId)).underscoreKeys)
  }

  def updateMergeRequest(projectId: Int,
                         mergeRequestId: Int,
                         sourceBranch: Option[String] = None,
                         targetBranch: Option[String] = None,
                         assigneeId: Option[Int] = None,
                         title: Option[String] = None,
                         stateEvent: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_request/" + mergeRequestId).withHttpHeaders(authToken)
      .put(Extraction.decompose(MergeRequest(sourceBranch.orNull, targetBranch.orNull, title.orNull, assigneeId)).underscoreKeys)
  }

  def acceptMergeRequest(projectId: Int,
                         mergeRequestId: Int,
                         mergeCommitMessage: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_request/" + mergeRequestId + "/merge").withHttpHeaders(authToken)
      .put(Json.obj("merge_commit_message" -> mergeCommitMessage))
  }

  def addMergeRequestComment(projectId: Int, mergeRequestId: Int, note: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_request/" + mergeRequestId + "/comments").withHttpHeaders(authToken)
      .post(Json.obj("note" -> note))
  }

  def getMergeRequestComments(projectId: Int, mergeRequestId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_request/" + mergeRequestId + "/comments")
      .withHttpHeaders(authToken).get()
  }

  /**
    * Issues
    */

  def getAllIssues(state: Option[String] = None, labels: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/issues").withHttpHeaders(authToken).withQueryStringParameters("state" -> state.orNull, "labels" -> labels.orNull).get()
  }

  def getIssues(projectId: Int,
                state: Option[String] = None,
                labels: Option[String] = None,
                milestone: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/issues").withHttpHeaders(authToken)
      .withQueryStringParameters("state" -> state.orNull, "labels" -> labels.orNull, "milestone" -> milestone.orNull).get()
  }

  def getIssue(projectId: Int, issueId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId).withHttpHeaders(authToken).get()
  }

  def addIssue(projectId: Int,
               title: String,
               description: Option[String] = None,
               assigneeId: Option[String] = None,
               milestoneId: Option[String] = None,
               labels: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/issues").withHttpHeaders(authToken)
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
    ws.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId).withHttpHeaders(authToken)
      .put(Extraction.decompose(Issue(title, description, assigneeId, milestoneId, labels, state)).underscoreKeys)
  }

  def closeIssue(projectId: Int, issueId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId).withHttpHeaders(authToken).put(Json.obj("state_event" -> "close"))
  }

  /**
    * Labels
    */

  def getLabels(projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/labels").withHttpHeaders(authToken).get()
  }

  def addLabel(projectId: Int,
               name: String,
               color: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/labels").withHttpHeaders(authToken)
      .post(Extraction.decompose(Label(name, color)).underscoreKeys)
  }

  def updateLabel(projectId: Int,
                  name: String,
                  newName: Option[String] = None,
                  color: Option[String] = None): Future[WSResponse] = {
    val updatedLabel = ("name" -> name) ~ ("new_name" -> newName.orNull) ~ ("color" -> color.orNull)
    ws.url(gitlabUrl + "/projects/" + projectId + "/labels").withHttpHeaders(authToken).put(updatedLabel)
  }

  def deleteLabel(projectId: Int, name: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/labels").withHttpHeaders(authToken)
      .withQueryStringParameters("name" -> name).delete()
  }


  /**
    * Milestones
    */
  def getMilestones(projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/milestones").withHttpHeaders(authToken).get()
  }

  def getMilestone(projectId: Int, milestoneId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/milestones/" + milestoneId).withHttpHeaders(authToken).get()
  }

  def addMilestone(projectId: Int,
                   title: String,
                   description: Option[String] = None,
                   dueDate: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/milestones").withHttpHeaders(authToken)
      .post(Extraction.decompose(Milestone(title, description, dueDate, None)).underscoreKeys)
  }

  def updateMilestone(projectId: Int,
                      milestoneId: Int,
                      title: String,
                      description: Option[String] = None,
                      dueDate: Option[String] = None,
                      stateEvent: Option[String] = None): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/milestones/" + milestoneId).withHttpHeaders(authToken)
      .put(Extraction.decompose(Milestone(title, description, dueDate, stateEvent)).underscoreKeys)
  }

  def closeMilestone(projectId: Int, milestoneId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/milestones/" + milestoneId).withHttpHeaders(authToken)
      .put(Json.obj("state_event" -> "close"))
  }

  /**
    * Issue Notes
    */

  def getIssueNotes(projectId: Int, issueId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId + "/notes").withHttpHeaders(authToken).get()
  }

  def getIssueNote(projectId: Int, issueId: Int, noteId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId + "/notes/" + noteId).withHttpHeaders(authToken).get()
  }

  def addIssueNote(projectId: Int, issueId: Int, body: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId + "/notes").withHttpHeaders(authToken)
      .post(Json.obj("body" -> body))
  }

  def updateIssueNote(projectId: Int, issueId: Int, noteId: Int, body: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/issues/" + issueId + "/notes/" + noteId).withHttpHeaders(authToken)
      .put(Json.obj("body" -> body))
  }

  /**
    * Snippet Notes
    */

  def getSnippetNotes(projectId: Int, snippetId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId + "/notes").withHttpHeaders(authToken).get()
  }

  def getSnippetNote(projectId: Int, snippetId: Int, noteId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId + "/notes/" + noteId).withHttpHeaders(authToken).get()
  }

  def addSnippetNote(projectId: Int, snippetId: Int, body: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId + "/notes").withHttpHeaders(authToken)
      .post(Json.obj("body" -> body))
  }

  def updateSnippetNote(projectId: Int, snippetId: Int, noteId: Int, body: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/snippets/" + snippetId + "/notes/" + noteId).withHttpHeaders(authToken)
      .put(Json.obj("body" -> body))
  }


  /**
    * Merge Request Notes
    */

  def getMergeRequestNotes(projectId: Int, mergeRequestId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_requests/" + mergeRequestId + "/notes").withHttpHeaders(authToken).get()
  }

  def getMergeRequestNote(projectId: Int, mergeRequestId: Int, noteId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_requests/" + mergeRequestId + "/notes/" + noteId).withHttpHeaders(authToken).get()
  }

  def addMergeRequestNote(projectId: Int, mergeRequestId: Int, body: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_requests/" + mergeRequestId + "/notes").withHttpHeaders(authToken)
      .post(Json.obj("body" -> body))
  }

  def updateMergeRequestNote(projectId: Int, mergeRequestId: Int, noteId: Int, body: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/merge_requests/" + mergeRequestId + "/notes/" + noteId).withHttpHeaders(authToken)
      .put(Json.obj("body" -> body))
  }

  /**
    * Deploy Keys
    */

  def getDeployKeys(projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/keys").withHttpHeaders(authToken).get()
  }

  def getDeployKey(projectId: Int, keyId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/keys/" + keyId).withHttpHeaders(authToken).get()
  }

  def addDeployKey(projectId: Int, title: String, key: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/keys").withHttpHeaders(authToken).post(("title" -> title) ~ ("key" -> key))
  }

  def deleteDeployKey(projectId: Int, keyId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/projects/" + projectId + "/keys/" + keyId).withHttpHeaders(authToken).delete()
  }

  /**
    * System Hooks
    */

  def getHooks: Future[WSResponse] = ws.url(gitlabUrl + "/hooks").withHttpHeaders(authToken).get()

  def getHook(hookId: Int): Future[WSResponse] = ws.url(gitlabUrl + "/hooks/" + hookId).withHttpHeaders(authToken).get()

  def addHook(hookUrl: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/hooks").withHttpHeaders(authToken).post(Json.obj("url" -> hookUrl))
  }

  def deleteHook(hookId: Int): Future[WSResponse] = ws.url(gitlabUrl + "/hooks/" + hookId).withHttpHeaders(authToken).delete()

  /**
    * Groups
    */

  def getGroups: Future[WSResponse] = ws.url(gitlabUrl + "/groups").withHttpHeaders(authToken).get()

  def getGroup(groupId: Int): Future[WSResponse] = ws.url(gitlabUrl + "/groups/" + groupId).withHttpHeaders(authToken).get()

  def addGroup(name: String, path: String): Future[WSResponse] = {
    ws.url(gitlabUrl + "/groups").withHttpHeaders(authToken).post(("name" -> name) ~ ("path" -> path))
  }

  def transferProjectToGroup(groupId: Int, projectId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/groups/" + groupId + "/projects/" + projectId).withHttpHeaders(authToken)
      .post(("group_id" -> groupId) ~ ("projectId" -> projectId))
  }

  def deleteGroup(groupId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/groups/" + groupId).withHttpHeaders(authToken).delete()
  }

  def getGroupMembers(groupId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/groups/" + groupId + "/members").withHttpHeaders(authToken).get()
  }

  def addGroupMember(groupId: Int, userId: Int, accessLevel: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/groups/" + groupId + "/members").withHttpHeaders(authToken)
      .post(("user_id" -> userId) ~ ("access_level" -> accessLevel))
  }

  def removeGroupMember(groupId: Int, userId: Int): Future[WSResponse] = {
    ws.url(gitlabUrl + "/groups/" + groupId + "/members/" + userId).withHttpHeaders(authToken).delete()
  }
}
