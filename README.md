# Jenkins helper tools for working with pipelines

This contains a bunch of functions and shell scripts to help with
running Jenkinsfile pipeline builds.  Funcitons like tagging a git
version, deploying to Elastic Beanstalk, and pushing to dockerhub.

## How to use

Include this repo as a git submodule in your repo by running:
`git submodule add -b release https://github.com/Superpedestrian/sp-jenkins-build-tools.git jenkins_tools`

The path is important as the functions expects to find itself in this
directory when run from a Jenkinsfile.

Now it is included in your repo, to use it in a `Jenkinsfile` you will
want to start out with something like:

```groovy
node {
    checkout scm
    sh 'git submodule init && git submodule update --recursive'

    Object docker = load 'jenkins_tools/groovy/docker.groovy'
    docker.dockerComposeBuild true
}
```

The submodule line will ensure the latest committed version of
sp-jenkins-build-tools is available for your build, and the next two
lines are an example of how you include the tools into your repo and
call a function from it.

## Upgrading

If there is a new release out of the tools, in your repo, just `cd`
into the `jenkins_tools` folder and run `git pull`.  Then go back in
to your repo and `git add jenkins_tools` and commit that.

## Sample usage for A/B or Red/Black deployments with Elastic Beanstalk

```groovy


node {
  stage 'Clean and Checkout'
  checkout scm
  sh 'git submodule init && git submodule update --recursive'
  sh 'git clean -xdf'

  // Import all of our library classes
  docker = load 'jenkins_tools/groovy/docker.groovy'
  deploy = load 'jenkins_tools/groovy/deploy.groovy'
  version = load 'jenkins_tools/groovy/version.groovy'
  git = load 'jenkins_tools/groovy/git.groovy'


  Object docker = load 'jenkins_tools/groovy/docker.groovy'
  docker.dockerComposeBuild true

  stage 'Run tests and coverage'
  // Make coverage folder so it doesn't get root owned
  sh 'rm -rf coverage && mkdir -p coverage'
  sh 'docker-compose run web gulp test'
  archive 'coverage/'
}

String repo = 'my-github-repo'
String port = '3000'
String ram = '512'
String org = 'example'
String credentialId = 'my-aws-credentials-on-jenkins'

if (env.BRANCH_NAME == 'master') {
  node {
    docker.dockerTagPush repo, 'latest', env.BUILD_NUMBER

    Object deploy = load 'jenkins_tools/groovy/deploy.groovy'
    deploy.ebDeploy(
      'my-ci-eb-app',
      'my-ci-eb-env',
      ram,
      env.BUILD_NUMBER,
      port,
      repo,
      org
    )
  }
}

if (env.BRANCH_NAME == 'release') {
  String ver = null
  node {
    // Find version from package to tag in github
    ver = version.npmVersion()
  }

  stage 'Prompt for release build/tagging'
  input "Proceed with tagging of ${ver}?"

  stage 'Tag git repo with new version'
  currentBuild.displayName = ver
  currentBuild.description = "Release ${ver} build ${env.BUILD_NUMBER}"

  node {
    git.gitTag ver, "Release ${ver}"
    docker.dockerTagPush repo, 'release', ver
  }

  String app = 'my-production-eb-app'
  String activeEnv = ''
  String newEnv = ''

  node {
    // Find current environment that holds the main cname
    activeEnv = deploy.ebGetActiveEnv(app, 'my-prod-cname')
    if (activeEnv == 'my-a-env) {
      newEnv = 'my-b-env'
    } else {
      newEnv = 'my-a-env'
    }
    // Clone that environment to the off color one
    deploy.ebClone(app, activeEnv, newEnv, 'my-staging-cname')
  }
  
  node {
    deploy.ebDeploy(
      app,
      newEnv,
      ram,
      ver,
      port,
      repo,
      org
    )
  }

  stage 'QA Environment and Swap URLs'
  input "Ready to promote https://${my-prod-cname}.example.com ?"
  node {
    deploy.ebSwap(
      app,
      newEnv,
      'my-a-env',
      'my-b-env'
    )
  }
  stage 'Terminate Old Environment'
  input "Ready to terminate old environment ${activeEnv} ?"
  node {
    deploy.ebTerminate(app, activeEnv)
  }
}
```
