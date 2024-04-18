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
import spock.lang.Unroll
import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec

import java.nio.file.Paths

import static wooga.gradle.dotnetsonar.utils.SpecUtils.rootCause
import static wooga.gradle.dotnetsonar.utils.SpecUtils.wrapValueBasedOnType

class BuildSolutionTaskIntegrationSpec extends PluginIntegrationSpec {

    def setup() {
        def fakeSonarScanner = FakeExecutables.argsReflector("sonarscanner", 0)
        buildFile << forceAddObjectsToExtension(fakeSonarScanner.executable)
    }

    @Unroll
    def "Builds a C# solution with dotnet #subtool #extraArgs command"() {
        given: "a msbuild executable"
        def fakeBuild = FakeExecutables.argsReflector("dotnet-build", 0)
        and: "fake solution file"
        new File(projectDir, solutionPath).with {
            parentFile.mkdirs()
            createNewFile()
        }
        and: "build file with configured task"
        buildFile << """
        project.tasks.create("solutionBuild", ${BuildSolution.name}) {
            executable = ${wrapValueBasedOnType(fakeBuild.executable.absolutePath, File)}
            solution = ${wrapValueBasedOnType(solutionPath, File)}
            environment = ${wrapValueBasedOnType(environment, Map)}
            additionalArguments.addAll(${wrapValueBasedOnType(extraArgs, List)})
        }
        """
        when:
        def result = runTasksSuccessfully("solutionBuild")

        then:
        def buildResult = fakeBuild.firstResult(result.standardOutput)
        def args = [Paths.get(projectDir.absolutePath, solutionPath).toString()] + extraArgs
        buildResult.args == subtool + args
        buildResult.envs.entrySet().containsAll(environment.entrySet())

        where:
        solutionPath       | subtool   | environment | extraArgs
        "dir/solution.sln" | ["build"] | [:]         | []
        "dir/solution.sln" | ["build"] | ["b": "c"]  | ["-arg", "/arg:value"]
    }

    @Unroll
    def "BuildSolution task fails if tool returns non-zero status"() {
        given: "a build executable"
        def fakeDotnet = FakeExecutables.argsReflector("dotnet", 1)
        and: "fake solution file"
        new File(projectDir, solutionPath).with {
            parentFile.mkdirs()
            createNewFile()
        }
        and: "build file with configured task"
        buildFile << """
        project.tasks.create("solutionBuild", ${BuildSolution.name}) {
            executable = ${wrapValueBasedOnType(fakeDotnet.executable.absolutePath, File)}
            solution = ${wrapValueBasedOnType(solutionPath, File)}
        }
        """
        when:
        def result = runTasksWithFailure("solutionBuild")

        then: "should fail on execution with non-zero exit value"
        def e = rootCause(result.failure)
        e.getClass().name == GradleException.name
        e.message.contains("exit value 1")
        where:
        solutionPath = "solution.sln"
    }
}
