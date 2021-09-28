#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([usernamePassword(credentialsId: 'github_integration', passwordVariable: 'githubPassword', usernameVariable: 'githubUser'),
                 usernamePassword(credentialsId: 'github_integration_2', passwordVariable: 'githubPassword2', usernameVariable: 'githubUser2'),
                 usernamePassword(credentialsId: 'github_integration_3', passwordVariable: 'githubPassword3', usernameVariable: 'githubUser3'),
                 string(credentialsId: 'atlas_plugins_sonar_token', variable: 'sonar_token')]) {
    def testEnvironment = [
            'macos':
                    [
                            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser}",
                            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword}"
                    ],
            'windows':
                    [
                            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser2}",
                            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword2}"
                    ],
            'linux':
                    [
                            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser3}",
                            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword3}"
                    ]
            ]

    buildGradlePlugin platforms: ['macos','windows', 'linux'], sonarToken: sonar_token, testEnvironment: testEnvironment
}
