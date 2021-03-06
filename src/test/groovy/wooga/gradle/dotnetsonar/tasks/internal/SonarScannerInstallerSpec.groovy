/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.dotnetsonar.tasks.internal

import nebula.test.ProjectSpec

import java.nio.file.Files
import java.nio.file.Paths

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
        and: "sonar scanner inner binaries should be able to execute"
        Files.walk(Paths.get(installDir.absolutePath)).
                filter {it.toFile().isFile() && it.parent.fileName.toString() == "bin"}.
                allMatch {it.toFile().exists() && it.toFile().canExecute() && it.toFile().canRead()}
    }
}
