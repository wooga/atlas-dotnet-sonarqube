package wooga.gradle.dotnetsonar.tasks.internal

import nebula.test.ProjectSpec

import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ
import static wooga.gradle.dotnetsonar.utils.SpecUtils.execDotnetApp

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
        scannerExec.canRead()
        scannerExec.name == "SonarScanner.MSBuild.exe"
        def execRes = execDotnetApp(project, scannerExec)
        execRes.stdOutString.contains("Using the .NET Framework version of the Scanner for MSBuild")
        execRes.execResult.exitValue == 1
    }
}
