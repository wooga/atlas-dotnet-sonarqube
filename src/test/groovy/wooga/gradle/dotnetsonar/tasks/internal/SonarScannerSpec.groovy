package wooga.gradle.dotnetsonar.tasks.internal

import spock.lang.Specification
import spock.lang.Unroll

import static wooga.gradle.dotnetsonar.utils.SpecFakes.fakeShell

class SonarScannerSpec extends Specification {

    def "runs sonar scanner begin with given properties"() {
        given: "a Sonar Scanner executable with a working dir"
        File scannerExec = new File("sonarscanner.exe")
        File workingDir = new File("a_dir")
        and: "a sonarqube project key, project version number, and properties"
        and: "a shell executor"
        def shell = fakeShell()

        when: "setting up execution for given sonar scanner executable begin with project key, version and properties"
        def sonarScanner = new SonarScanner(shell, scannerExec, workingDir)
        sonarScanner.begin(projectKey, projectName, version, sonarProperties)

        def expectedArgs = ["begin", "-k:${projectKey}", "-n:${projectName}", "-v:${version}"]
        expectedArgs.addAll(sonarProperties.entrySet().collect {"-d:${it.key}=${it.value}"})

        then:
        shell.lastExecSpec.executable == scannerExec.absolutePath
        shell.lastExecSpec.workingDir == workingDir
        shell.lastExecSpec.args == expectedArgs

        where:
        projectKey      | projectName   | version   | sonarProperties
        "i_m_a_project" | "projectName" | "0.123.1" | ["sonar.prop": "propvalue"]
        "i_m_a_project" | "pjName"      | "0.123.1" | [:]
    }


    @Unroll
    def "ignores redundant property #property on begin"() {
        given: "a Sonar Scanner executable with a working dir"
        File scannerExec = new File("sonarscanner.exe")
        File workingDir = new File("a_dir")
        and: "one of sonarqube properties that should be ignored by SonarScanner"
        def propToBeIgnored = [property: value]
        and: "a shell executor"
        def shell = fakeShell()

        when: "setting up execution for given sonar scanner executable begin with project key, version and properties"
        def sonarScanner = new SonarScanner(shell, scannerExec, workingDir)
        sonarScanner.begin("pjkey", "pjname", "pjversion", propToBeIgnored)

        then:
        !shell.lastExecSpec.args.contains("-d:${property}=${value}")

        where:
        property                    | value
        "sonar.projectKey"          | "pj_key"
        "sonar.projectName"         | "pjName"
        "sonar.projectVersion"      | "0.0.2"
        "sonar.working.directory"   | "dir"
    }

    def "runs sonar scanner end with given properties"() {
        given: "a Sonar Scanner executable with a working dir"
        File scannerExec = new File("sonarscanner.exe")
        File workingDir = new File("a_dir")
        and: "a sonarqube login token"
        def sonarToken = "token"
        and: "a shell executor"
        def shell = fakeShell()

        when: "setting up execution for given sonar scanner executable begin with project key, version and properties"
        def sonarScanner = new SonarScanner(shell, scannerExec, workingDir)
        sonarScanner.end(sonarToken)

        then:
        shell.lastExecSpec.executable == scannerExec.absolutePath
        shell.lastExecSpec.workingDir == workingDir
        shell.lastExecSpec.args == ["end", "-d:sonar.login=${sonarToken}"]
    }
}
