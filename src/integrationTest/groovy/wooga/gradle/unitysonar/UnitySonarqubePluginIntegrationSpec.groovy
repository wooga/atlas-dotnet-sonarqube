package wooga.gradle.unitysonar

import com.wooga.gradle.test.IntegrationSpec
import com.wooga.gradle.test.run.result.GradleRunResult
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter

class UnitySonarqubePluginIntegrationSpec extends IntegrationSpec {

    def setup() {
        buildFile << "${applyPlugin(UnitySonarqubePlugin)}\n"
    }

    def "sonarqube task run tests and sonar build"() {
        given: "applied unity-sonarqube plugin"

        when:
        def result = runTasks("sonarqube", "--dry-run")

        then:
        def run = new GradleRunResult(result)
        run["test"]
        run["sonarBuildUnity"]
        run["sonarqube"].wasExecutedAfter("test")
        run["sonarqube"].wasExecutedAfter("sonarBuildUnity")
    }

    def "sonarBuildUnity task runs in expected order"() {
        given: "applied unity-sonarqube plugin"

        when:
        def result = runTasks("sonarBuildUnity", "--dry-run")

        then:
        def run = new GradleRunResult(result)
        run["asdfBinstubsDotnet"].wasExecutedBefore("sonarBuildUnity")
        run["generateSolution"].wasExecutedBefore("sonarBuildUnity")
        run["sonarScannerBegin"].wasExecutedBefore("sonarBuildUnity")
        run["sonarScannerEnd"].wasExecutedAfter("sonarBuildUnity")
    }

    def "sonarqube build task runs after unity tests"() {
        given: "applied sonarqube plugin"

        when:
        def result = runTasks("sonarBuildUnity", "test", "--dry-run")

        then:
        def run = new GradleRunResult(result)
        run["test"].wasExecutedBefore("sonarBuildUnity")
    }

    def "#task uses asdf-installed dotnet as default executable"() {
        given: "applied unity-sonarqube plugin"
        when:
        getter.write(buildFile)
        def tasksResult = runTasks("asdfBinstubsDotnet", getter.taskName)
        def query = getter.generateQuery(this, tasksResult)

        then:
        query.matches(new File(projectDir, "bin/dotnet").absolutePath)

        where:
        task << ["sonarBuildUnity", "sonarScannerBegin", "sonarScannerEnd"]
        getter = new PropertyGetterTaskWriter("${task}.executable")
    }

    def "#task uses extension-set dotnet as executable"() {
        given: "applied unity-sonarqube plugin"
        when:
        def query = runPropertyQuery(getter, setter)

        then:
        query.matches(expectedExecutable)

        where:
        task = "sonarBuildUnity"
        expectedExecutable = "folder/dot_net"
        getter = new PropertyGetterTaskWriter("${task}.executable")
        setter = new PropertySetterWriter("unitySonarqube", "buildDotnetExecutable")
                .set(expectedExecutable, String)
    }

}
