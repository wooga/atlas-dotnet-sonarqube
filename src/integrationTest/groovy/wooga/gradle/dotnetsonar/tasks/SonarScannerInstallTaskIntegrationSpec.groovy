package wooga.gradle.dotnetsonar.tasks

import spock.lang.Unroll
import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec
import wooga.gradle.dotnetsonar.utils.SpecFakes

class SonarScannerInstallTaskIntegrationSpec extends PluginIntegrationSpec {

    //again take care with too big file path length on windows, MS does not likes these.
    def "installs sonar scanner with default values"() {
        given: "installed dotnet sonarscanner plugin"
        and: "no pre-existent sonarscanner executable on PATH"

        when: "running sonar scanner installation task"
        def result = runTasksSuccessfully("sonarScannerInstall")

        then: "sonar scanner should be executed"
        result.wasExecuted(":sonarScannerInstall")
        and: "dotnet sonar scanner package should be installed at default directory"
        def defaultVersion = "5.2.2.33595"
        def defaultInstallDir = "build/bin/net-sonarscanner/"
        new File(projectDir,
                "${defaultInstallDir}/sonarscanner-${defaultVersion}/${SonarScannerInstall.EXECUTABLE_NAME}").exists()
    }

    @Unroll
    def "installs sonar scanner version #version-net46 in #installDir"() {
        given: "installed dotnet sonarscanner plugin"
        and: "no pre-existent sonarscanner executable on PATH"
        and: "configured sonar scanner installation task"
        buildFile << """
        sonarScannerInstall {
            version = "${version}"
            installationDir = "${installDir}"
        }
        """
        when: "running sonar scanner installation task"
        def result = runTasksSuccessfully("sonarScannerInstall")

        then: "sonar scanner should be executed"
        result.wasExecuted(":sonarScannerInstall")
        and: "dotnet sonar scanner package should be installed at default directory"
        new File(projectDir,
                "${installDir}/sonarscanner-${version}/${SonarScannerInstall.EXECUTABLE_NAME}").exists()
        //https://github.com/SonarSource/sonar-scanner-msbuild/releases/download//sonar-scanner-msbuild-4.7.1.2311-netcoreapp2.0.zip
        where:
        version         | installDir
        "5.2.2.33595"   | "bin/fdlr"
        "5.2.1.31210"   | "fdlr"
        "4.7.1.2311"    | "fdlr"
    }

    def "doesnt install sonar scanner if it already exists on extension"() {
        given: "installed dotnet sonarscanner plugin"
        and: "pre-existent sonarscanner executable"
        def scannerExec = SpecFakes.argReflectingFakeExecutable("sonarscanner.bat")
        buildFile << forceAddSonarScannerObjectToExtension(scannerExec)

        when: "running sonar scanner installation task"
        def result = runTasksSuccessfully("sonarScannerInstall")

        then: "sonar scanner should be skipped"
        result.wasExecuted(":sonarScannerInstall")
        result.wasSkipped(":sonarScannerInstall")
    }
}
