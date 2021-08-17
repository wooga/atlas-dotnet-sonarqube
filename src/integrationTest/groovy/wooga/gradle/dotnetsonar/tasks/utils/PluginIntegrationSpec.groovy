package wooga.gradle.dotnetsonar.tasks.utils

import nebula.test.IntegrationSpec
import wooga.gradle.dotnetsonar.DotNetSonarqubePlugin
import wooga.gradle.dotnetsonar.SonarScannerExtension
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner
import wooga.gradle.dotnetsonar.utils.SpecFakes

import static wooga.gradle.dotnetsonar.utils.SpecUtils.isWindows
import static wooga.gradle.dotnetsonar.utils.SpecUtils.wrapValueBasedOnType

class PluginIntegrationSpec extends IntegrationSpec {

    def setup() {
        buildFile << """
        import ${SonarScannerExtension.name}
        import ${SonarScanner.name}
        group = 'test'
        ${applyPlugin(DotNetSonarqubePlugin)}
        """.stripIndent()
    }

    String setupSonarScannerExtension(String extensionVarName) {
        return """
        SonarScannerExtension ${extensionVarName} = project.extensions.getByType(SonarScannerExtension)
        """
    }

    String forceAddObjectsToExtension(File sonarScannerExec) {
        return """
        ${forceAddMonoOnNonWindows()}
        def setupSonarScannerFile() {
            ${setupSonarScannerExtension("sonarScannerExt")}
            sonarScannerExt.sonarScanner = sonarScannerExt.createSonarScanner(${wrapValueBasedOnType(sonarScannerExec.absolutePath, File)}) 
        }
        setupSonarScannerFile()
        """
    }

    String forceAddMonoOnNonWindows() {
        if(!isWindows()) {
            File monoExec = SpecFakes.runFirstParameterFakeExecutable("mono")
            return """
            def setupMono() {
                ${setupSonarScannerExtension("sonarScannerExt")}
                sonarScannerExt.monoExecutable = ${wrapValueBasedOnType(monoExec.absolutePath, File)}
            }
            setupMono()
            """
        } else {
            return ""
        }
    }
}
