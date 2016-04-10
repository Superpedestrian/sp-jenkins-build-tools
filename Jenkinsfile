#!groovy

node {
    stage 'Clean and Checkout'
    checkout scm
    // Clear and make volume mounted folders so they
    // have the right permissions
    sh "rm -rf reports docs && mkdir -p reports docs"

    Object docker = load 'groovy/docker.groovy'
    docker.dockerComposeBuild(true)

    stage 'Run tests'
    try {
      sh 'docker-compose run test'
    } catch (e) {
      archive 'reports/'
      throw e
    }
    archive 'docs/groovydoc/'
}
