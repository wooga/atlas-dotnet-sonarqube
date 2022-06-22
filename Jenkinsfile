#!groovy
@Library(['github.com/wooga/atlas-jenkins-pipeline@1.x', 'adventure5@add/svn-wrapper']) _
//
//withCredentials([usernamePassword(credentialsId: 'github_integration', passwordVariable: 'githubPassword', usernameVariable: 'githubUser'),
//                 usernamePassword(credentialsId: 'github_integration_2', passwordVariable: 'githubPassword2', usernameVariable: 'githubUser2'),
//                 usernamePassword(credentialsId: 'github_integration_3', passwordVariable: 'githubPassword3', usernameVariable: 'githubUser3'),
//                 string(credentialsId: 'atlas_plugins_sonar_token', variable: 'sonar_token'),
//                 string(credentialsId: 'atlas_plugins_snyk_token', variable: 'SNYK_TOKEN')]) {
//    def testEnvironment = [
//            'macos':
//                    [
//                            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser}",
//                            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword}"
//                    ],
//            'windows':
//                    [
//                            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser2}",
//                            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword2}"
//                    ],
//            'linux':
//                    [
//                            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser3}",
//                            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword3}"
//                    ]
//            ]
//
////    buildGradlePlugin platforms: ['macos','windows', 'linux'], sonarToken: sonar_token, testEnvironment: testEnvironment
//}
pipeline {
    agent none
    stages {
        stage('Preparation') {
            agent {
               label "linux && atlas"
            }
            steps {
                svnWrapper locations: [[credentialsId: 'test-adventure5-svn-credentials',
                                        remote: "https://eu-subversion.assembla.com/svn/wooga^adventure5.content/Content/scenes/541-06"]]
            }
        }
    }
}
