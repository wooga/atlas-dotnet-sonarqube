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

class SonarScannerSpec extends Specification {

    def "runs sonar scanner begin with given properties"() {
        given: "a Sonar Scanner executable with a working dir"
        File scannerExec = new File("sonarscanner.exe")
        and: "a sonarqube project key, project version number, and properties"

        when: "setting up execution for given sonar scanner executable begin with project key, version and properties"
        def sonarScanner = new SonarScanner(scannerExec)
        def args = sonarScanner.beginArgs(projectKey, projectName, version, sonarProperties)

        def expectedArgs = ["begin", "-k:${projectKey}", "-n:${projectName}", "-v:${version}"]
        expectedArgs.addAll(sonarProperties.entrySet().collect {"-d:${it.key}=${it.value}"})

        then:
        args == [scannerExec.absolutePath] + expectedArgs

        where:
        projectKey      | projectName   | version   | sonarProperties
        "i_m_a_project" | "projectName" | "0.123.1" | ["sonar.prop": "propvalue"]
        "i_m_a_project" | "pjName"      | "0.123.1" | [:]
    }


    @Unroll
    def "ignores redundant property #property on begin"() {
        given: "a Sonar Scanner executable with a working dir"
        File scannerExec = new File("sonarscanner.exe")
        and: "one of sonarqube properties that should be ignored by SonarScanner"
        def propToBeIgnored = [property: value]

        when: "setting up execution for given sonar scanner executable begin with project key, version and properties"
        def sonarScanner = new SonarScanner(scannerExec)
        def args = sonarScanner.beginArgs("pjkey", "pjname", "pjversion", propToBeIgnored)

        then:
        !args.contains("-d:${property}=${value}")

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
        and: "a sonarqube login token"
        def sonarToken = "token"

        when: "setting up execution for given sonar scanner executable begin with project key, version and properties"
        def sonarScanner = new SonarScanner(scannerExec)
        def args = sonarScanner.endArgs(sonarToken)

        then:
        args == [scannerExec.absolutePath, "end", "-d:sonar.login=${sonarToken}"]
    }
}
