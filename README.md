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
