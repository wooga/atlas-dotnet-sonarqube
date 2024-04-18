/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.dotnetsonar.tasks

import com.wooga.gradle.test.executable.FakeExecutables
import com.wooga.spock.extensions.github.GithubRepository
import com.wooga.spock.extensions.github.Repository
import com.wooga.spock.extensions.github.api.RateLimitHandlerWait
import com.wooga.spock.extensions.github.api.TravisBuildNumberPostFix
import org.ajoberstar.grgit.Grgit
import org.gradle.api.GradleException
import spock.lang.Unroll
import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec

class SonarScannerBeginTaskIntegrationSpec extends PluginIntegrationSpec {

    @GithubRepository(
            usernameEnv = "ATLAS_GITHUB_INTEGRATION_USER",
            tokenEnv = "ATLAS_GITHUB_INTEGRATION_PASSWORD",
            repositoryPostFixProvider = [TravisBuildNumberPostFix.class],
            rateLimitHandler = RateLimitHandlerWait.class,
            resetAfterTestCase = true
    )
    def "task executes sonar scanner tool begin command with git-based properties"(Repository testRepo) {
        given: "a sonar scanner executable"
        def fakeSonarScannerExec = FakeExecutables.argsReflector("sonarscanner", 0)
        and: "a set up sonar scanner extension"
        buildFile << forceAddObjectsToExtension(fakeSonarScannerExec.executable)
        and: "a set up github extension"
        buildFile << """
        github {
            repositoryName = "${testRepo.fullName}"
            username = "${testRepo.userName}"
            token = "${testRepo.token}"
        }
        """

        when:
        def result = runTasksSuccessfully("sonarScannerBegin")

        then:
        def execResults = fakeSonarScannerExec.firstResult(result.standardOutput)
        def companyName = testRepo.fullName.split("/")[0]
        execResults.args.contains("-n:${testRepo.name}".toString())
        execResults.args.contains("-k:${companyName}_${testRepo.name}".toString())
    }

    def "task sets branch property name accordingly with its PR or non-PR status"() {
        given: "a sonar scanner executable"
        def fakeSonarScanner = FakeExecutables.argsReflector("sonarscanner", 0)
        and: "a set up sonar scanner extension"
        buildFile << forceAddObjectsToExtension(fakeSonarScanner.executable)

        when:
        def result = runTasksSuccessfully("sonarScannerBegin")

        then:
        def execResults = fakeSonarScanner.firstResult(result.standardOutput)
        if (expectedBranchProperty) {
            execResults.args.contains("-d:sonar.branch.name=${expectedBranchProperty}".toString())
        } else {
            !execResults.args.contains("-d:sonar.branch.name")
        }
        where:
        branchName | expectedBranchProperty
        "name"     | "name"
        "PR-10"    | null
        "PR-fe"    | "PR-fe"
    }

    def "task executes sonar scanner tool begin command with extension properties"() {
        given: "a sonar scanner executable"
        def fakeSonarScanner = FakeExecutables.argsReflector("sonarscanner", 0)
        and: "a set up sonar scanner extension"
        buildFile << forceAddObjectsToExtension(fakeSonarScanner.executable)

        and: "a configured github extension"
        def companyName = "company"
        def repoName = "repo"
        buildFile << """
        github {
            repositoryName = "${companyName}/${repoName}"
        }
        """
        and: "a git repository on branch"
        def branchName = "branch"
        def git = Grgit.init(dir: projectDir)
        git.commit(message: "any message")
        git.checkout(branch: branchName, createBranch: true)

        and: "a configured sonarqube extension"
        def projectVersion = "0.0.1"
        buildFile << """
        version = "${projectVersion}"
        sonarqube {
            properties {
                property "sonar.prop", ""
                property "sonar.exclusions", "src"
            }
        }
        """

        when: "running the sonarScannerBegin task"
        def result = runTasksSuccessfully("sonarScannerBegin")

        then:
        def execResults = fakeSonarScanner.firstResult(result.standardOutput)
        execResults.args.contains("-n:${repoName}".toString())
        execResults.args.contains("-k:${companyName}_${repoName}".toString())
        execResults.args.contains("-v:${projectVersion}".toString())
        execResults.args.contains("-d:sonar.branch.name=${branchName}".toString())
        execResults.args.contains("-d:sonar.exclusions=src")
        !execResults.args.contains("-d:sonar.sources=")
        !execResults.args.contains("-d:sonar.prop=")
    }

    def "task executes sonar scanner tool begin command with task properties"() {
        given: "a sonar scanner executable"
        def fakeSonarScanner = FakeExecutables.argsReflector("sonarscanner", 0)
        and: "a set up sonar scanner task"
        buildFile << forceAddObjectsToExtension(fakeSonarScanner.executable)
        buildFile << """
        sonarScannerBegin {
            sonarqubeProperties.put("sonar.projectKey", "key")
            sonarqubeProperties.put("sonar.projectName", "name")
            sonarqubeProperties.put("sonar.version", "0.0.1")
            sonarqubeProperties.put("sonar.exclusions", "src")
            sonarqubeProperties.put("sonar.prop", "value")
        }
        """
        when: "running the sonarScannerBegin task"
        def result = runTasksSuccessfully("sonarScannerBegin")
        then:
        def execResults = fakeSonarScanner.firstResult(result.standardOutput)
        execResults.args.contains("-k:key")
        execResults.args.contains("-v:0.0.1")
        execResults.args.contains("-n:name")
        execResults.args.contains("-d:sonar.exclusions=src")
        execResults.args.contains("-d:sonar.prop=value")
    }

    @Unroll
    def "task fails #key property isn't present"() {
        given: "a sonar scanner executable"
        def fakeSonarScanner = FakeExecutables.argsReflector("sonarscanner", 0)
        and: "a set up sonar scanner task without mandatory properties"
        buildFile << forceAddObjectsToExtension(fakeSonarScanner.executable)
        buildFile << """
        sonarScannerBegin {
        ${key == "sonar.projectKey" ?
                """sonarqubeProperties.put("sonar.version", "val")""" :
                """sonarqubeProperties.put("sonar.projectKey", "val")"""}
            sonarqubeProperties.put("sonar.exclusions", "src")
            sonarqubeProperties.put("sonar.prop", "value")
        }
        """
        when: "running the sonarScannerBegin task"
        def result = runTasksWithFailure("sonarScannerBegin")

        then:
        def e = rootCause(result.failure)
        e instanceof IllegalArgumentException
        e.message == "SonarScannerBegin task needs a set ${key} property"

        where:
        key << ["sonar.projectKey", "sonar.version"]
    }

    def "task fails if sonar scanner tool begin command returns non-zero status"() {
        given: "a failing sonar scanner executable"
        def fakeSonarScanner = FakeExecutables.argsReflector("sonarscanner", 1)
        and: "a set up sonar scanner extension"
        buildFile << forceAddObjectsToExtension(fakeSonarScanner.executable)

        when: "running the sonarScannerBegin task"
        def result = runTasksWithFailure("sonarScannerBegin")

        then: "should fail on execution with non-zero exit value"
        def e = rootCause(result.failure)
        e.getClass().name == GradleException.name
        e.message.contains("exit value 1")
    }
}


import static wooga.gradle.dotnetsonar.utils.SpecUtils.rootCause