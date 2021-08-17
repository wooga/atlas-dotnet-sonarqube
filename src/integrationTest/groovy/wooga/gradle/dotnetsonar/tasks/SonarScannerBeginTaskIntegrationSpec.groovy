package wooga.gradle.dotnetsonar.tasks

import org.gradle.process.internal.ExecException
import spock.lang.Unroll
import wooga.gradle.dotnetsonar.SonarScannerExtension
import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec
import wooga.gradle.dotnetsonar.utils.FakeExecutable

import static wooga.gradle.dotnetsonar.utils.SpecFakes.argReflectingFakeExecutable
import static wooga.gradle.dotnetsonar.utils.SpecUtils.rootCause

class SonarScannerBeginTaskIntegrationSpec extends PluginIntegrationSpec {


    def "task executes sonar scanner tool begin command with extension properties"() {
        given: "a sonar scanner executable"
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 0)
        and: "a set up sonar scanner extension"
        buildFile << forceAddObjectsToExtension(fakeSonarScannerExec)
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
