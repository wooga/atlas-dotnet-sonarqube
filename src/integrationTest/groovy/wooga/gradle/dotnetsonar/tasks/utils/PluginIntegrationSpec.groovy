package wooga.gradle.dotnetsonar.tasks.utils

import nebula.test.IntegrationSpec
import wooga.gradle.dotnetsonar.DotNetSonarqubePlugin
import wooga.gradle.dotnetsonar.SonarScannerExtension
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner

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

    String forceAddSonarScannerObjectToExtension(File sonarScannerExec) {
        return """
        def setupSonarScannerFile() {
            ${setupSonarScannerExtension("sonarScannerExt")}
            sonarScannerExt.sonarScanner = SonarScanner.gradleBased(project, 
                                                ${wrapValueBasedOnType(sonarScannerExec.absolutePath, File)},  
                                                project.projectDir)
        }
        setupSonarScannerFile()
        """
    }
}
