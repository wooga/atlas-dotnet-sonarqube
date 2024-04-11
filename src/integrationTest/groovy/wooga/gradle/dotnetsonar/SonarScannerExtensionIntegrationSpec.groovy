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

package wooga.gradle.dotnetsonar

import spock.lang.Unroll
import wooga.gradle.dotnetsonar.tasks.internal.SonarScannerInstaller
import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec
import wooga.gradle.dotnetsonar.utils.SpecFakes

import static wooga.gradle.dotnetsonar.tasks.internal.SonarScannerInstaller.defaultBaseURL
import static wooga.gradle.dotnetsonar.utils.SpecUtils.*

class SonarScannerExtensionIntegrationSpec extends PluginIntegrationSpec {

    def "registers custom build task for sonar scanner"() {
        given: "a sonnar scanner executable"
        File fakeScannerExec = SpecFakes.argReflectingFakeExecutable("sonarscanner")
        and: "custom task registering itself on sonar scanner extension"
        buildFile << """
        ${forceAddObjectsToExtension(fakeScannerExec)}
        ${getSonarScannerExtension("sonarScannerExt")}
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


    def "downloads sonar scanner app if it isn't available from properties or path"() {
        given: "a applied dotnet sonar scanner plugin"
        and: "unset sonarScannerExecutable property in the extension"

        when: "running tasks dependent on sonar scanner executable"
        runTasks("sonarScannerBegin")

        then: "dotnet sonar scanner package should be installed at default directory"
        def defaultInstallDir = "build/bin/net-sonarscanner/"
        new File(projectDir,
            "${defaultInstallDir}/${SonarScannerInstaller.EXECUTABLE_NAME}").exists()
    }

    @Unroll
    def "installs sonar scanner version #version-net46 in #installDir"() {
        given: "installed dotnet sonarscanner plugin"
        and: "no pre-existent sonarscanner executable on PATH"
        and: "configured sonar scanner extension"
        buildFile << """
        sonarScanner {
            installInfo {
                version = "${version}"
                installDir = "${installDir}"
            }
        }
        """
        when: "running tasks dependent on sonar scanner executable"
        runTasks("sonarScannerBegin")

        then: "dotnet sonar scanner package should be installed at default directory"
        new File(projectDir,
                "${installDir}/${SonarScannerInstaller.EXECUTABLE_NAME}").exists()
        where:
        version         | installDir
        "5.2.2.33595"   | "bin/fdlr"
        "5.2.1.31210"   | "fdlr"
        "4.7.1.2311"    | "fdlr"
    }

    @Unroll
    def "installs sonar scanner from URL in #installDir"() {
        given: "installed dotnet sonarscanner plugin"
        and: "no pre-existent sonarscanner executable on PATH"
        and: "configured sonar scanner extension"
        buildFile << """
        sonarScanner {
            installInfo {
                sourceURL = "${url}"
                installDir = "${installDir}"
            }
        }
        """
        when: "running tasks dependent on sonar scanner executable"
        runTasks("sonarScannerBegin")

        then: "dotnet sonar scanner package should be installed at default directory"
        new File(projectDir,
                "${installDir}/${SonarScannerInstaller.EXECUTABLE_NAME}").exists()
        where:
        url                                                                             | installDir
        "${defaultBaseURL}/5.2.1.31210/sonar-scanner-msbuild-5.2.1.31210-net46.zip"     | "bin/fdlr"
        "${defaultBaseURL}/5.1.0.28487/sonar-scanner-msbuild-5.1.0.28487-net46.zip"     | "fdlr"
    }

    def "doesn't install sonar scanner if executable already exists on extension"() {
        given: "installed dotnet sonarscanner plugin"
        and: "pre-existent sonarscanner executable"
        def sonarScannerExec = SpecFakes.argReflectingFakeExecutable("sonarscanner.bat")
        buildFile << """
        sonarScanner {
            sonarScannerExecutable = ${wrapValueBasedOnType(sonarScannerExec.absolutePath, File)}
            installInfo {
                version = "5.2.1.31210"
            }
        }
        """

        when: "running tasks dependent on sonar scanner executable"
        runTasks("sonarScannerBegin")

        then: "sonar scanner should not be downloaded"
        def defaultInstallDir = "build/bin/net-sonarscanner/"
        !new File(projectDir,
                "${defaultInstallDir}/${SonarScannerInstaller.EXECUTABLE_NAME}").exists()
    }
}
