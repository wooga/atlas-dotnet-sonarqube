package wooga.gradle.dotnetsonar.tasks

import com.wooga.spock.extensions.github.GithubRepository
import com.wooga.spock.extensions.github.Repository
import com.wooga.spock.extensions.github.api.RateLimitHandlerWait
import com.wooga.spock.extensions.github.api.TravisBuildNumberPostFix
import org.ajoberstar.grgit.Grgit
import org.gradle.process.internal.ExecException
import spock.lang.Unroll
import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec
import wooga.gradle.dotnetsonar.utils.FakeExecutable

import static wooga.gradle.dotnetsonar.utils.SpecFakes.argReflectingFakeExecutable
import static wooga.gradle.dotnetsonar.utils.SpecUtils.rootCause

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
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 0)
        and: "a set up sonar scanner extension"
        buildFile << forceAddObjectsToExtension(fakeSonarScannerExec)
        and: "a set up github extension"
        buildFile << """
        github {
            repositoryName = "${testRepo.fullName}"
            username = "${testRepo.userName}"
            token = "${testRepo.token}"
        }
        """
        and: "a pull request open for this repository"
        def prBranchName = "realbranch"
        testRepo.createBranch(prBranchName)
        testRepo.commit("commitmsg", prBranchName)
        def pr = testRepo.createPullRequest("Test", prBranchName, testRepo.defaultBranch.name, "description")

        and: "a setup PR git repository"
        def git = Grgit.init(dir: projectDir)
        git.commit(message: "any message")
        git.checkout(branch: "PR-${pr.number}", createBranch: true)

        when:
        def result = runTasksSuccessfully("sonarScannerBegin")

        then:
        def execResults = FakeExecutable.lastExecutionResults(result)
        def companyName = testRepo.fullName.split("/")[0]
        execResults.args.contains("-n:${testRepo.name}".toString())
        execResults.args.contains("-k:${companyName}_${testRepo.name}".toString())
        execResults.args.contains("-d:sonar.branch.name=${prBranchName}".toString())
    }

    def "task executes sonar scanner tool begin command with extension properties"() {
        given: "a sonar scanner executable"
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 0)
        and: "a set up sonar scanner extension"
        buildFile << forceAddObjectsToExtension(fakeSonarScannerExec)

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
        def execResults = FakeExecutable.lastExecutionResults(result)
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
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 0)
        and: "a set up sonar scanner task"
        buildFile << """
        ${createSonarScannerFromExecutable("scanner", fakeSonarScannerExec)}
        sonarScannerBegin {
            sonarScanner.set(scanner)
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
        def execResults = FakeExecutable.lastExecutionResults(result)
        execResults.args.contains("-k:key")
        execResults.args.contains("-v:0.0.1")
        execResults.args.contains("-n:name")
        execResults.args.contains("-d:sonar.exclusions=src")
        execResults.args.contains("-d:sonar.prop=value")
    }

    @Unroll
    def "task fails #key property isn't present"() {
        given: "a sonar scanner executable"
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 0)
        and: "a set up sonar scanner task without mandatory properties"
        buildFile << """
        ${createSonarScannerFromExecutable("scanner", fakeSonarScannerExec)}
        sonarScannerBegin {
            sonarScanner.set(scanner)
        ${key=="sonar.projectKey"?
                """sonarqubeProperties.put("sonar.version", "val")""":
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
        e.message == "SonarqubeBegin needs a set ${key} property"

        where:
        key << ["sonar.projectKey", "sonar.version"]
    }

    def "task fails if sonar scanner tool begin command returns non-zero status"() {
        given: "a failing sonar scanner executable"
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 1)
        and: "a set up sonar scanner extension"
        buildFile << forceAddObjectsToExtension(fakeSonarScannerExec)

        when: "running the sonarScannerBegin task"
        def result = runTasksWithFailure("sonarScannerBegin")

        then: "should fail on execution with non-zero exit value"
        def e = rootCause(result.failure)
        e.getClass().name == ExecException.name
        e.message.contains("exit value 1")
    }
}
