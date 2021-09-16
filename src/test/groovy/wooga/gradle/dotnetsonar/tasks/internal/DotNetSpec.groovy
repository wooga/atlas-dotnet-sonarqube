package wooga.gradle.dotnetsonar.tasks.internal

import spock.lang.Specification
import spock.lang.Unroll
import wooga.gradle.dotnetsonar.utils.FakeShell

class DotNetSpec extends Specification {

    @Unroll
    def "sets up execution specifications to build a solution with given dotnet executable"() {
        given: "a dotnet executable"
        def executable = new File(executablePath)
        and: "any solution"
        def solution = new File(solutionPath)
        and: "any environment"
        and: "a shell executor"
        def shell = new FakeShell()

        when: "setting up specification"
        def dotnet = new DotNet(shell, executable)
        dotnet.buildSolution(solution, environment, extraArgs)

        then:
        shell.lastExecSpec.executable == executable.absolutePath
        shell.lastExecSpec.args == ["build"] + extraArgs + [solution.absolutePath]
        shell.lastExecSpec.environment.entrySet().containsAll(environment.entrySet())

        where:
        executablePath | solutionPath   | environment          | extraArgs
        "dotnet.exe"   | "solution.sln" | [:]                  | []
        "dotnet.exe"   | "solution.sln" | [envVar: "envValue"] | []
        "dotnet.exe"   | "solution.sln" | [envVar: "envValue"] | ["-arg"]
        "dotnet.exe"   | "solution.sln" | [envVar: "envValue"] | ["/arg"]
        "dotnet.exe"   | "solution.sln" | [envVar: "envValue"] | ["-arg", "/arg:value"]
    }
}
