/**
 * deploy.groovy
 * Functions for elastic beanstalk or other deployment tasks
 */

KEY_ID_VAR = 'AWS_ACCESS_KEY_ID'
KEY_SECRET_VAR = 'AWS_SECRET_ACCESS_KEY'
SECRET_BINDING_CLASS = 'UsernamePasswordMultiBinding'
SECRET_CLASS = 'StringBinding'
LOGSTASH_KEYS = [
  [
    $class:SECRET_CLASS,
    variable:'LOGSTASH_CLIENT_KEY',
    credentialsId:'logstash-client-key'
  ],
  [
    $class:SECRET_CLASS,
    variable:'LOGSTASH_CLIENT_CERT',
    credentialsId:'logstash-client-cert'
  ],
  [
    $class:SECRET_CLASS,
    variable:'LOGSTASH_CA_CERT',
    credentialsId:'logstash-ca-cert'
  ]
]

/**
 * Clone an Elastic Beanstalk environment
 *
 * @param app Elastic Beanstalk application
 * @param environmentSource Environment to clone
 * @param environmentDst Environment to create from clone
 * @param environmentCNAME CNAME for new environment
 * @param credentialId Jenkins credential plugin ID that has AWS credentials
     as username/password type
 */
void ebClone(
  app, environmentSource, environmentDestination, environmentCNAME, credentialsId='aws-eb-creds'
) {
  stage "Cloning Elastic Beanstalk Environment from ${environmentSource} to ${environmentDestination}"
  withEnv(
    [
      "EB_APP=${app}",
      "EB_ENV_SRC=${environmentSource}",
      "EB_ENV_DST=${environmentDestination}",
      "EB_ENV_CNAME=${environmentCNAME}"
    ]
  ) {
    withCredentials(
      [[$class:SECRET_BINDING_CLASS, credentialsId:credentialsId,
        usernameVariable:KEY_ID_VAR, passwordVariable:KEY_SECRET_VAR
       ]]
    ) {
      sh 'jenkins_tools/shell/eb_clone'
    }
  }
}

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
 * @param installExtensions string to determine if EB extensions should be
     installed
 * @param logstashKeys List of credentials instantiations to provide
     logstash SSL credentials for that extension
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
  credentialId='aws-eb-creds',
  installExtensions='false',
  logstashKeys=null
) {
  stage "Push to Elastic Beanstalk (${environment})"
  withEnv(
    [
      "TAG=${tag}",
      "APP_RAM=${ram}",
      "CONTAINER_PORT=${port}",
      "EB_APP=${app}",
      "EB_ENV=${environment}",
      "DH_REPO=${repo}",
      "DH_ORG=${org}",
      "INSTALL_EXTENSIONS=${installExtensions}"
    ]
  ) {
    DEPLOY_SCRIPT = 'jenkins_tools/shell/eb_deploy'
    credentials = [[
        $class:SECRET_BINDING_CLASS, credentialsId:credentialId,
        usernameVariable:KEY_ID_VAR, passwordVariable:KEY_SECRET_VAR
    ]]
    // Jenkins doesn't let you + or addAll to combine to collections
    // so I just have to call it two different ways based on the parameter
    if (installExtensions == 'true' && logstashKeys != null) {
      withCredentials(logstashKeys) {
        withCredentials(credentials) {
          sh DEPLOY_SCRIPT
        }
      }
    }
    withCredentials(credentials) {
      sh DEPLOY_SCRIPT
    }
  }
}

/**
 * Get Active Environment
 *
 * @param app Elastic Beanstalk application
 * @param cname production CNAME start string
 * @param envFile File name/path to use to store the active environment name.
 * @param credentialId Jenkins credential plugin ID that has AWS credentials
     as username/password type
 */
String ebGetActiveEnv(app, cname, envFile='EB_ACTIVE_ENV', credentialsId='aws-eb-creds') {
  stage 'Get active Elastic Beanstalk environment'
  withEnv(
    [
      "EB_APP=${app}",
      "EB_CNAME=${cname}",
      "EB_ACTIVE_ENV_FILE=${envFile}"
    ]
  ) {
    withCredentials(
      [[$class:SECRET_BINDING_CLASS, credentialsId:credentialsId,
        usernameVariable:KEY_ID_VAR, passwordVariable:KEY_SECRET_VAR
       ]]
    ) {
      sh 'jenkins_tools/shell/eb_get_env'
    }
  }
  String activeEnv = readFile(envFile)
  sh "rm ${envFile}"
  return activeEnv
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
      sh 'jenkins_tools/shell/eb_swap'
    }
  }
}

/**
 * Terminate an Elastic Beanstalk environment
 *
 * @param app Elastic Beanstalk application
 * @param environment to terminate
 * @param credentialId Jenkins credential plugin ID that has AWS credentials
     as username/password type
 */
void ebTerminate(
  app, environment, credentialsId='aws-eb-creds'
) {
  stage "Terminating ${environment}"
  withEnv(
    [
      "EB_APP=${app}",
      "EB_ENV=${environment}"
    ]
  ) {
    withCredentials(
      [[$class:SECRET_BINDING_CLASS, credentialsId:credentialsId,
        usernameVariable:KEY_ID_VAR, passwordVariable:KEY_SECRET_VAR
       ]]
    ) {
      sh 'jenkins_tools/shell/eb_terminate'
    }
  }
}

return this
