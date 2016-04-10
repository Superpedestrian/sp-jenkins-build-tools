#!groovy

node {
    stage 'Clean and Checkout'
    checkout scm

    // Make report folder so it doesn't get root owned
    sh 'rm -rf reports && mkdir -p reports'
    def docker = load 'groovy/docker.groovy'
    docker.dockerComposeBuild(true)

    stage 'Run tests'
    sh 'docker-compose run test'
    archive 'reports/'
}
