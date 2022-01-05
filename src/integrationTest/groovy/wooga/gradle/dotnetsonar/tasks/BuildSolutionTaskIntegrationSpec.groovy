package wooga.gradle.dotnetsonar.tasks

import org.gradle.process.internal.ExecException
import spock.lang.Unroll
import wooga.gradle.dotnetsonar.tasks.utils.PluginIntegrationSpec

import java.nio.file.Paths

import static wooga.gradle.dotnetsonar.utils.FakeExecutable.executionResults
import static wooga.gradle.dotnetsonar.utils.SpecFakes.argReflectingFakeExecutable
import static wooga.gradle.dotnetsonar.utils.SpecUtils.rootCause
import static wooga.gradle.dotnetsonar.utils.SpecUtils.wrapValueBasedOnType

class BuildSolutionTaskIntegrationSpec extends PluginIntegrationSpec {

    def setup() {
        def fakeSonarScannerExec = argReflectingFakeExecutable("sonarscanner", 0)
        buildFile << forceAddObjectsToExtension(fakeSonarScannerExec)
    }

    @Unroll
    def "Builds a C# solution with dotnet #subtool #extraArgs command"() {
        given: "a msbuild executable"
        def fakeBuildExec = argReflectingFakeExecutable("dotnet", 0)
        and: "build file with configured task"
        buildFile << """
        project.tasks.create("solutionBuild", ${BuildSolution.name}) {
            dotnetExecutable = ${wrapValueBasedOnType(fakeBuildExec.absolutePath, File)}
            solution = ${wrapValueBasedOnType(solutionPath, File)}
            environment = ${wrapValueBasedOnType(environment, Map)}
            extraArgs = ${wrapValueBasedOnType(extraArgs, List)}
        }
        """
        when:
        def result = runTasksSuccessfully("solutionBuild")

        then:
        def buildResult = executionResults(fakeBuildExec, result)
        buildResult.args == subtool + [Paths.get(projectDir.absolutePath, solutionPath).toString()] + extraArgs
        buildResult.envs.entrySet().containsAll(environment.entrySet())

        where:
        solutionPath       | subtool   | environment | extraArgs
        "dir/solution.sln" | ["build"] | [:]         | []
        "dir/solution.sln" | ["build"] | ["b": "c"]  | ["-arg", "/arg:value"]
    }


    @Unroll
    def "Builds a C# solution with msbuild #extraArgs command"() {
        given: "a msbuild executable"
        def fakeBuildExec = argReflectingFakeExecutable("msBuild", 0)
        and: "build file with configured task"
        buildFile << """
        project.tasks.create("solutionBuild", ${BuildSolution.name}) {
            msBuildExecutable = ${wrapValueBasedOnType(fakeBuildExec.absolutePath, File)}
            solution = ${wrapValueBasedOnType(solutionPath, File)}
            environment = ${wrapValueBasedOnType(environment, Map)}
            extraArgs = ${wrapValueBasedOnType(extraArgs, List)}
        }
        """
        when:
        def result = runTasksSuccessfully("solutionBuild")

        then:
        def buildResult = executionResults(fakeBuildExec, result)
        buildResult.args == extraArgs + [Paths.get(projectDir.absolutePath, solutionPath).toString()]
        buildResult.envs.entrySet().containsAll(environment.entrySet())

        where:
        solutionPath       | environment | extraArgs
        "solution.sln"     | ["a": "b"]  | []
        "solution.sln"     | [:]         | ["-arg", "/arg:value"]
    }

    @Unroll
    def "#tool build task fails if tool returns non-zero status"() {
        given: "a build executable"
        def fakeMsBuildExec = argReflectingFakeExecutable(tool, 1)
        and: "build file with configured task"
        buildFile << """
        project.tasks.create("solutionBuild", ${BuildSolution.name}) {
            ${tool}Executable = ${wrapValueBasedOnType(fakeMsBuildExec.absolutePath, File)}
            solution = ${wrapValueBasedOnType("solution.sln", File)}
        }
        """
        when:
        def result = runTasksWithFailure("solutionBuild")

        then: "should fail on execution with non-zero exit value"
        def e = rootCause(result.failure)
        e.getClass().name == ExecException.name
        e.message.contains(fakeMsBuildExec.absolutePath)
        e.message.contains("exit value 1")
        where:
        tool << ["msBuild", "dotnet"]
    }
}
