package wooga.gradle.dotnetsonar.tasks.internal

import nebula.test.ProjectSpec
import org.gradle.process.ExecSpec

class SonarScannerInstallerSpec extends ProjectSpec {

    //WARNING: this test may fail on windows if the resulting executable path is too big.
    // this causes some extracted dlls to be named on legacy windows path names, MS is just fucked up man.
    def "sonar scanner is installed from github for given version"() {
        given: "a sonar scanner and dotnet version"
        def version = "5.2.2.33595"
        def dotnetVersion = "net46"
        and: "a installation directory"
        def installDir = project.layout.buildDirectory.dir("sonarscanner").get().asFile

        when: "sonar scanner is installed"
        def scannerInstaller = SonarScannerInstaller.gradleBased(project)
        def scannerExec = scannerInstaller.install(version, dotnetVersion, installDir)

        then: "sonar scanner executable should be executing"
        scannerExec.exists()
        scannerExec.canExecute()
        scannerExec.name == "SonarScanner.MSBuild.exe"
        def execRes = new GradleShell(project).execute { ExecSpec it ->
            it.executable = scannerExec.absolutePath
        }
        execRes.stdOutString.contains("Using the .NET Framework version of the Scanner for MSBuild")
        execRes.execResult.exitValue == 1
    }
}
