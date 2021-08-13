package wooga.gradle.dotnetsonar.tasks

import org.gradle.process.internal.ExecException
import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec
import wooga.gradle.dotnetsonar.utils.FakeExecutable

import static wooga.gradle.dotnetsonar.utils.SpecFakes.argReflectingFakeExecutable
import static wooga.gradle.dotnetsonar.utils.SpecUtils.rootCause

class SonarScannerEndTaskIntegrationSpec extends PluginIntegrationSpec {

    def "executes sonar scanner tool end command"() {
        given: "a applied dotnet sonar scanner plugin"
        and: "a sonar scanner executable"
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 0)
        and: "a set up sonar scanner extension"
        buildFile << forceAddSonarScannerObjectToExtension(fakeSonarScannerExec)
        and: "a configurated sonarqube extension"
        def loginToken = "loginToken"
        buildFile << """
        sonarqube {
            properties {
                property "sonar.login", "${loginToken}"
            }
        }
        """

        when: "running sonarScannerEnd task"
        def result = runTasksSuccessfully("sonarScannerEnd")

        then: "executes sonar scanner begin task"
        result.wasExecuted(":sonarScannerBegin")
        and: "executes sonar scanner tool end command with sonarscanner extension login property"
        def execResult = FakeExecutable.lastExecutionResults(result)
        execResult.args == ["end", "-d:sonar.login=${loginToken}"]
    }

    def "task fails if sonar scanner tool end command returns non-zero status"() {
        given: "a failing sonar scanner executable"
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 1)
        and: "a set up sonar scanner extension"
        buildFile << forceAddSonarScannerObjectToExtension(fakeSonarScannerExec)

        when: "running the sonarScannerEnd task"
        def result = runTasksWithFailure("sonarScannerEnd")

        then: "should fail on execution with non-zero exit value"
        def e = rootCause(result.failure)
        e.getClass().name == ExecException.name
        e.message.contains(fakeSonarScannerExec.absolutePath)
        e.message.contains("exit value 1")
    }

}
