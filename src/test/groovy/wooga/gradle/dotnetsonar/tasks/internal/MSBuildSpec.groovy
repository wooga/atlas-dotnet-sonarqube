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

package wooga.gradle.dotnetsonar.tasks.internal

import spock.lang.Specification
import spock.lang.Unroll
import wooga.gradle.dotnetsonar.utils.FakeShell

class MSBuildSpec extends Specification {

    @Unroll
    def "sets up execution specifications to build a solution with given MSBuild executable"() {
        given: "a MSBuild executable"
        def executable = new File(executablePath)
        and: "any solution"
        def solution = new File(solutionPath)
        and: "any environment"
        and: "a shell executor"
        def shell = new FakeShell()

        when: "setting up specification"
        def msbuild = new MSBuild(shell, executable)
        msbuild.buildSolution(solution, environment, extraArgs)

        then:
        shell.lastExecSpec.executable == executable.absolutePath
        shell.lastExecSpec.args == extraArgs + [solution.absolutePath]
        shell.lastExecSpec.environment.entrySet().containsAll(environment.entrySet())

        where:
        executablePath | solutionPath   | environment          | extraArgs
        "msbuild.exe"  | "solution.sln" | [:]                  | []
        "msbuild.exe"  | "solution.sln" | [envVar: "envValue"] | []
        "msbuild.exe"  | "solution.sln" | [envVar: "envValue"] | ["-arg"]
        "msbuild.exe"  | "solution.sln" | [envVar: "envValue"] | ["/arg"]
        "msbuild.exe"  | "solution.sln" | [envVar: "envValue"] | ["/arg", "-arg:value"]
    }
}
