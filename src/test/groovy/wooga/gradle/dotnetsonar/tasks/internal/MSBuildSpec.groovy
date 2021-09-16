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
