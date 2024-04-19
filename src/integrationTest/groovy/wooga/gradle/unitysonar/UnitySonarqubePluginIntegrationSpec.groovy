package wooga.gradle.unitysonar

import com.wooga.gradle.test.IntegrationSpec
import com.wooga.gradle.test.run.result.GradleRunResult
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter

import static com.wooga.gradle.PlatformUtils.windows

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
        run[installDotnet].wasExecutedBefore("sonarBuildUnity")
        run["generateSolution"].wasExecutedBefore("sonarBuildUnity")
        run["sonarScannerBegin"].wasExecutedBefore("sonarBuildUnity")
        run["sonarScannerEnd"].wasExecutedAfter("sonarBuildUnity")

        where:
        installDotnet = isWindows()? "dotnetWindowsInstall" : "asdfBinstubsDotnet"
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
        def tasksResult = runTasks(installDotnet, getter.taskName)
        def query = getter.generateQuery(this, tasksResult)
        File dotnetExec = dotnetFile(projectDir)

        then:
        query.matches(dotnetExec.absolutePath)

        where:
        installDotnet = isWindows()? "dotnetWindowsInstall" : "asdfBinstubsDotnet"
        task << ["sonarBuildUnity", "sonarScannerBegin", "sonarScannerEnd"]
        getter = new PropertyGetterTaskWriter("${task}.executable")

        gradleHome = System.getenv("GRADLE_USER_HOME")? new File(System.getenv("GRADLE_USER_HOME")) : new File(System.getProperty("user.home"), ".gradle")
        dotnetFile = isWindows()?
                { _ -> new File(gradleHome, "net.wooga.unity-sonarqube/dotnet/7.0.100/dotnet.exe") }:
                {File projDir -> new File(projDir, "bin/dotnet") }
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
