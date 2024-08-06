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

import com.wooga.gradle.test.IntegrationSpec
import com.wooga.gradle.test.executable.FakeExecutables
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

    String forceAddObjectsToExtension(File sonarScannerExec) {
        return """
        ${forceAddFakeDotnet()}
        sonarScanner.sonarScannerExecutable = ${wrapValueBasedOnType(sonarScannerExec.absolutePath, File)} 
        """
    }

    String forceAddFakeDotnet() {
        def dotnetExec = FakeExecutables.runFirstParam("dotnet", true)
        return """
        sonarScanner {
            dotnetExecutable = ${wrapValueBasedOnType(dotnetExec.absolutePath, File)}
        }
        """
    }
}
