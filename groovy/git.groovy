/**
 * git.groovy
 * Git functions, i.e. tag or any other complex interaction
 */

/**
 * Use bat or sh based on choice
 */
void cmd(cmd, useBat = false) {
  if (useBat) {
    bat cmd
  } else {
    sh cmd
  }
}

/**
 * Tag the git repo at the current HEAD with the given label
 *
 * @param label Git tag to create, i.e. 0.3.0
 * @param message Message to add to annotated tag
 * @param name Git identity name to use in tag
 * @param email Git identity email to use in tag
 * @param credentialUsername Github username to use for credentials
 * @param credentialFileId Jenkins credential plugin ID for Git
 *    credential file to use (https://git-scm.com/docs/git-credential-store)
 * @param useBat boolean flag to determine whether to use sh or bat
 */
@SuppressWarnings(['ParameterCount'])
void gitTag(
  label,
  message = 'Release',
  name = 'build.internal.superpedestrian.com',
  email = 'dev-ops@superpedestrian.com',
  credentialUsername = 'sp-devops',
  credentialFileId = 'sp-devops-github-creds',
  useBat = false
) {
  echo "Tagging version ${label}"
  cmd "git config --local user.name ${name}", useBat
  cmd "git config --local user.email ${email}", useBat
  cmd "git tag -f -a ${label} -m \"${message}\"", useBat
  cmd "git config --local credential.username ${credentialUsername}", useBat
  withCredentials(
    [[
        $class:'FileBinding',
        credentialsId:credentialFileId,
        variable:'GIT_CREDS'
    ]]
  ) {
    String credPath = env.GIT_CREDS

    if (useBat) {
      credPath = credPath.replaceAll('\\\\', '/')
      credPath = credPath.replaceAll('%', '%%')
    }
    cmd "git config --local credential.helper \"store --file=\\\"$credPath\\\"\"", useBat
    cmd 'git config -l', useBat
    cmd 'git push --tags', useBat
  }
}

return this
