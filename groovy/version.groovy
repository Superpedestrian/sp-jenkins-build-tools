/**
 * version.groovy
 * Functions for parsing the version out of a project.
 */

/**
 * Get version string from package.json for node projects
 */
String npmVersion() {
  Object matcher = readFile('package.json') =~ '  "version": \"(.+)\",'
  matcher ? matcher[0][1] : null
}

/**
 * Get version string from setup.py for python projects
 */
String pyVersion() {
  Object matcher = readFile('setup.py') =~ 'version=\'(.+)\''
  matcher ? matcher[0][1] : null
}

/**
 * Get version string from build.gradle file specified by gradlePath
 */
String androidVersion(gradlePath) {
  Object matcher = readFile(gradlePath) =~ 'versionName \"(.+)\"'
  matcher ? matcher[0][1] : null
}

return this
