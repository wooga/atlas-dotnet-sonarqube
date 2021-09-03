#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@refactor/scripts') _

withCredentials([string(credentialsId: 'atlas_plugins_sonar_token', variable: 'sonar_token')]) {
    buildGradlePlugin platforms: ['macos','windows', 'linux'], sonarToken: sonar_token
}
