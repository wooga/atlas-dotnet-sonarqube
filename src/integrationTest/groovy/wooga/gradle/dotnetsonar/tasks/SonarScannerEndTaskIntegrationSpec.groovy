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

package wooga.gradle.dotnetsonar.tasks

import com.wooga.gradle.test.executable.FakeExecutables
import org.gradle.api.GradleException
import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec

import static wooga.gradle.dotnetsonar.utils.SpecUtils.rootCause

class SonarScannerEndTaskIntegrationSpec extends PluginIntegrationSpec {

    def "executes sonar scanner tool end command with extension properties"() {
        given: "a applied dotnet sonar scanner plugin"
        and: "a sonar scanner executable"
        def fakeSonarScannerExec = FakeExecutables.argsReflector("sonarscanner", 0)
        and: "a set up sonar scanner extension"
        buildFile << forceAddObjectsToExtension(fakeSonarScannerExec.executable)
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

        then: "executes sonar scanner tool end command with sonarscanner extension login property"
        def execResult = fakeSonarScannerExec.firstResult(result.standardOutput)
        execResult.args == ["end", "-d:sonar.login=${loginToken}"]
    }

    def "executes sonar scanner tool end command with task properties"() {
        given: "a applied dotnet sonar scanner plugin"
        and: "a sonar scanner executable"
        def fakeSonarScannerExec = FakeExecutables.argsReflector("sonarscanner", 0)
        and: "a set up sonar scanner extension (for sonarScannerBegin task)"
        buildFile << forceAddObjectsToExtension(fakeSonarScannerExec.executable)
        and: "a configurated sonarScannerEnd task"
        def loginToken = "loginToken"
        buildFile << """
        sonarScannerEnd {
            loginToken = "${loginToken}"
        }
        """
        when: "running sonarScannerEnd task"
        def result = runTasksSuccessfully("sonarScannerEnd")

        then: "executes sonar scanner tool end command with sonarscanner extension login property"
        def execResult = fakeSonarScannerExec.firstResult(result.standardOutput)
        execResult.args == ["end", "-d:sonar.login=${loginToken}"]
    }


    def "task fails if sonar scanner tool end command returns non-zero status"() {
        given: "a failing sonar scanner executable"
        def fakeSonarScannerExec = FakeExecutables.argsReflector("sonarscanner", 1)
        and: "a set up sonar scanner extension"
        buildFile << forceAddObjectsToExtension(fakeSonarScannerExec.executable)

        when: "running the sonarScannerEnd task"
        def result = runTasksWithFailure("sonarScannerEnd")

        then: "should fail on execution with non-zero exit value"
        def e = rootCause(result.failure)
        e.getClass().name == GradleException.name
        e.message.contains("exit value 1")
    }

}
