package wooga.gradle.dotnetsonar

import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec
import wooga.gradle.dotnetsonar.utils.SpecFakes

import static wooga.gradle.dotnetsonar.utils.SpecUtils.wasExecutedAfter
import static wooga.gradle.dotnetsonar.utils.SpecUtils.wasExecutedBefore

class SonarScannerExtensionIntegrationSpec extends PluginIntegrationSpec {

    def "registers custom build task for sonar scanner"() {
        given: "a sonnar scanner executable"
        File fakeScannerExec = SpecFakes.argReflectingFakeExecutable("sonarscanner")
        and: "custom task registering itself on sonar scanner extension"
        buildFile << """
        ${forceAddSonarScannerObjectToExtension(fakeScannerExec)}
        ${setupSonarScannerExtension("sonarScannerExt")}
        tasks.register("custom") {
            sonarScannerExt.registerBuildTask(it)
        }
        """

        when: "executing custom task"
        def result = runTasks("custom")
        then: "sonarScannerBegin is executed before custom task"
        result.wasExecuted(":sonarScannerBegin")
        wasExecutedBefore(result.standardOutput, ":sonarScannerBegin", ":custom")
        and: "sonarScannerEnd is executed after custom task"
        result.wasExecuted(":sonarScannerEnd")
        wasExecutedAfter(result.standardOutput, ":sonarScannerEnd", ":custom")
    }



}
