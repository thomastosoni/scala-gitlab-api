# Scala Gitlab API

[ ![Codeship Status for thomas-tosoni/scala-gitlab-api](https://codeship.com/projects/b9ceb0f0-7f0d-0132-e3f0-72e1afbc4410/status?branch=master)](https://codeship.com/projects/57160)

This is a scala wrapper for the [Gitlab API](https://github.com/gitlabhq/gitlabhq/tree/master/doc/api) using the [Play WS API](https://www.playframework.com/documentation/2.4.x/ScalaWS).

Works with Gitlab 7.6.2

## Requirements

Play Framework

## Usage

* Create a new instance and link it to your deployed Gitlab. 

```scala
val gitlabAPI = new GitlabAPI(gitlabUrl, gitlabToken)
```

* Use it to interact with your Gitlab

```scala
// Create
gitlabAPI.createUser(email, password, userUsername, userName)

// Get
gitlabAPI.getCommit(projectId, commitSHA)

// Update
gitlabAPI.updateIssue(projectId, issueId, description = Option("solved"))

// Delete
gitlabAPI.deleteProject(projectId)
```

## Test the API

This API comes with a set of tests. In order to start the tests please follow these few steps.

* Create an application.conf file following the given application.conf.template.  
* Fill the following fields in the application.conf file  

 Field        			| Description |
 ----------------------|:-------------:|
__gitlab.test-repository__	| Repository that is going to be checked out to test repository actions related behavior
__gitlab.test-ssh-key__	| SSH key used to clone the test depot
__gitlab.url__			| The url to your deployed Gitlab
__gitlab.token__			| The Gitlab user authentication token

* Start the tests  

```
$> sbt test
```
