/**
 * docker.groovy
 * Functions for building and publishing docker containers
 */

/**
 * Build a docker image, tag it, and push to docker hub
 *
 * @param repo Dockerhub repository
 * @param base Base build tag (i.e. latest, release)
 * @param tag Special tag to push, i.e. 0.1.2 or env.BUILD_NUMBER
 * @param org Dockerhub organization
 */
void dockerTagPush(repo, base, tag='latest', org='superpedestrian') {
  stage 'Building production docker image'
  sh "docker build -t ${org}/${repo}:${base} ."

  stage 'Push to dockerhub'
  sh "docker push ${org}/${repo}:${base}"

  stage "Tag and push to dockerhub with ${tag}"
  sh "docker tag -f ${org}/${repo}:${base} superpedestrian/${org}:${tag}"
  sh "docker push ${org}/${repo}:${tag}"
}

/**
 * Use docker-compose to set USER_ID and build all containers
 *
 */
void dockerComposeBuild(useCache=True) {
  stage 'Build Docker Image'
    // See https://issues.jenkins-ci.org/browse/JENKINS-26133 for
    // why I have to do this
    sh 'echo $(id -u) > uid'
    String uid = readFile('uid')
    env.USER_ID = uid
    if (useCache) {
      sh 'docker-compose build'
    } else {
        sh 'docker-compose build --no-cache'
    }
}

return this
