package wooga.gradle.dotnetsonar.tasks.internal

import spock.lang.Specification
import wooga.gradle.dotnetsonar.utils.FakeShell

class DotNetSpec extends Specification {

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
        dotnet.buildSolution(solution, environment)

        then:
        shell.lastExecSpec.executable == executable.absolutePath
        shell.lastExecSpec.args == ["build", solution.absolutePath]
        shell.lastExecSpec.environment["envVar"] == "envValue"

        where:
        executablePath  | solutionPath  | environment
        "dotnet.exe"    |"solution.sln" | [envVar: "envValue"]
    }
}
