/**
 * git.groovy
 * Git functions, i.e. tag or any other complex interaction
 */

/**
 * Tag the git repo at the current HEAD with the given label
 *
 * @param label Git tag to create, i.e. 0.3.0
 * @param message Message to add to annotated tag
 * @param name Git identity name to use in tag
 * @param email Git identity email to use in tag
 * @param credentialUsername Github username to use for credentials
 * @param credentialFileId Jenkins credential plugin ID for Git
     credential file to use (https://git-scm.com/docs/git-credential-store)
 */
@SuppressWarnings(['ParameterCount'])
void gitTag(
  label,
  message = 'Release',
  name = 'build.internal.superpedestrian.com',
  email = 'dev-ops@superpedestrian.com',
  credentialUsername = 'sp-devops',
  credentialFileId = 'sp-devops-github-creds'
) {
  echo "Tagging version ${label}"
  sh "git config --local user.name ${name}"
  sh "git config --local user.email ${email}"
  sh "git tag -f -a ${label} -m '${message}'"
  sh "git config --local credential.username ${credentialUsername}"
  withCredentials(
    [[
        $class:'FileBinding',
        credentialsId:credentialFileId,
        variable:'GIT_CREDS'
    ]]
  ) {
    sh 'git config --local credential.helper "store --file=\'$GIT_CREDS\'"'
    sh 'git config -l'
    sh 'git push --tags'
  }
}

return this
