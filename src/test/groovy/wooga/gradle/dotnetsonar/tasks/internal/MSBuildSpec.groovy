package wooga.gradle.dotnetsonar.tasks.internal

import spock.lang.Specification
import wooga.gradle.dotnetsonar.utils.FakeShell

class MSBuildSpec extends Specification {

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
        msbuild.buildSolution(solution, environment)

        then:
        shell.lastExecSpec.executable == executable.absolutePath
        shell.lastExecSpec.args == [solution.absolutePath]
        shell.lastExecSpec.environment["envVar"] == "envValue"

        where:
        executablePath  | solutionPath  | environment
        "msbuild.exe"   |"solution.sln" | [envVar: "envValue"]
    }
}
