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
