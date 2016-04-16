/**
 * deploy.groovy
 * Functions for elastic beanstalk or other deployment tasks
 */

KEY_ID_VAR = 'AWS_ACCESS_KEY_ID'
KEY_SECRET_VAR = 'AWS_SECRET_ACCESS_KEY'
SECRET_BINDING_CLASS = 'UsernamePasswordMultiBinding'

/**
 * Deploy new version to an Elastic Beanstalk multi docker environment
 *
 * @param app Elastic Beanstalk application
 * @param environment Elastic Beanstalk environment within @app
 * @param ram Memory to give application container
 * @param tag Dockerhub tag to deploy
 * @param port Port in application container to map to 80
 * @param repo Dockerhub repository
 * @param org Dockerhub organization
 * @param credentialId Jenkins credential plugin ID that has AWS credentials
     as username/password type
 */
@SuppressWarnings(['ParameterCount'])
void ebDeploy(
  app,
  environment,
  ram,
  tag,
  port,
  repo,
  org='superpedestrian',
  credentialId='aws-eb-creds'
) {
  stage 'Push to Elastic Beanstalk'
  withEnv(
    [
      "TAG=${tag}",
      "APP_RAM=${ram}",
      "CONTAINER_PORT=${port}",
      "EB_APP=${app}",
      "EB_ENV=${environment}",
      "DH_REPO=${repo}",
      "DH_ORG=${org}"
    ]
  ) {
    withCredentials(
      [[$class:SECRET_BINDING_CLASS, credentialsId:credentialId,
        usernameVariable:KEY_ID_VAR, passwordVariable:KEY_SECRET_VAR
       ]]
    ) {
      sh 'jenkins_tools/shell/eb_deploy'
    }
  }
}

/**
 * Swap Elastic Beanstalk URLs
 *
 * @param app Elastic Beanstalk application
 * @param environment to issue URL swap to within @app
 * @param environment_white White environment name
 * @param environment_red Red environment name
 * @param credentialId Jenkins credential plugin ID that has AWS credentials
     as username/password type
 */
void ebSwap(
  app, environment, environmentWhite, environmentRed, credentialsId='aws-eb-creds'
) {
  stage 'Swap URLs'
  withEnv(
    [
      "EB_APP=${app}",
      "EB_ENV=${environment}",
      "EB_ENV_WHITE=${environmentWhite}",
      "EB_ENV_RED=${environmentRed}",
    ]
  ) {
    withCredentials(
      [[$class:SECRET_BINDING_CLASS, credentialsId:credentialsId,
        usernameVariable:KEY_ID_VAR, passwordVariable:KEY_SECRET_VAR
       ]]
    ) {
      sh 'jenkinks_tools/shell/eb_swap'
    }
  }
}

return this
