package wooga.gradle.dotnetsonar.tasks

import org.gradle.process.internal.ExecException
import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec
import wooga.gradle.dotnetsonar.utils.FakeExecutable

import static wooga.gradle.dotnetsonar.utils.SpecFakes.argReflectingFakeExecutable
import static wooga.gradle.dotnetsonar.utils.SpecUtils.rootCause

class SonarScannerBeginTaskIntegrationSpec extends PluginIntegrationSpec {


    def "task executes sonar scanner tool begin command with extension properties"() {
        given: "a sonar scanner executable"
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 0)
        and: "a set up sonar scanner extension"
        buildFile << forceAddSonarScannerObjectToExtension(fakeSonarScannerExec)
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
        execResults.args.contains("-k:${moduleName}".toString())
        execResults.args.contains("-v:${projectVersion}".toString())
        execResults.args.contains("-d:sonar.exclusions=src")
        !execResults.args.contains("-d:sonar.sources=")
        !execResults.args.contains("-d:sonar.prop=")
        result.wasExecuted(":sonarScannerInstall")
    }

    def "task fails if sonar scanner tool begin command returns non-zero status"() {
        given: "a failing sonar scanner executable"
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 1)
        and: "a set up sonar scanner extension"
        buildFile << forceAddSonarScannerObjectToExtension(fakeSonarScannerExec)

        when: "running the sonarScannerBegin task"
        def result = runTasksWithFailure("sonarScannerBegin")

        then: "should fail on execution with non-zero exit value"
        def e = rootCause(result.failure)
        e.getClass().name == ExecException.name
        e.message.contains(fakeSonarScannerExec.absolutePath)
        e.message.contains("exit value 1")
    }
}
