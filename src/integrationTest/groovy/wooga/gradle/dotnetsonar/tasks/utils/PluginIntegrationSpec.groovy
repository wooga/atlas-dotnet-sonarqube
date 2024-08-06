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

package wooga.gradle.dotnetsonar.tasks.utils

import nebula.test.IntegrationSpec
import wooga.gradle.dotnetsonar.DotNetSonarqubePlugin
import wooga.gradle.dotnetsonar.SonarScannerExtension
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner
import wooga.gradle.dotnetsonar.tasks.internal.SonarScannerFactory
import wooga.gradle.dotnetsonar.utils.SpecFakes

import static wooga.gradle.dotnetsonar.utils.SpecUtils.isWindows
import static wooga.gradle.dotnetsonar.utils.SpecUtils.wrapValueBasedOnType

class PluginIntegrationSpec extends IntegrationSpec {

    def setup() {
        buildFile << """
        import ${SonarScannerExtension.name}
        import ${SonarScanner.name}
        import ${SonarScannerFactory.name}
        group = 'test'
        ${applyPlugin(DotNetSonarqubePlugin)}
        """.stripIndent()
    }

    String createSonarScannerFromExecutable(String scannerVarName, File executable) {
        return """
        ${forceAddMonoOnNonWindows()}
        def createSonarScanner() {
            ${getSonarScannerExtension("ext")}
            def factory = SonarScannerFactory.withPathFallback(project, ext.monoExecutable.asFile)
            return factory.fromExecutable(${wrapValueBasedOnType(executable, File)}, project.projectDir)
        }
        def ${scannerVarName} = createSonarScanner()
        """
    }

    String getSonarScannerExtension(String extensionVarName) {
        return """
        SonarScannerExtension ${extensionVarName} = project.extensions.getByType(SonarScannerExtension)
        """
    }

    String forceAddObjectsToExtension(File sonarScannerExec) {
        return """
        ${forceAddMonoOnNonWindows()}
        def setupSonarScannerFile() {
            ${getSonarScannerExtension("sonarScannerExt")}
            sonarScannerExt.sonarScannerExecutable = ${wrapValueBasedOnType(sonarScannerExec.absolutePath, File)} 
        }
        setupSonarScannerFile()
        """
    }

    String forceAddMonoOnNonWindows() {
        if(!isWindows()) {
            File monoExec = SpecFakes.runFirstParameterFakeExecutable("mono")
            return """
            sonarScanner {
                monoExecutable = ${wrapValueBasedOnType(monoExec.absolutePath, File)}
            }
            """
        } else {
            return ""
        }
    }
}
