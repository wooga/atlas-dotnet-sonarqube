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
import spock.lang.IgnoreIf
import spock.lang.Unroll

import java.nio.file.Paths

import static wooga.gradle.dotnetsonar.utils.SpecUtils.*

class OSOpsSpec extends ProjectSpec {

    @Unroll
    @IgnoreIf({ isWindows() })
    def "finds external executable with unix filename #filename on path"() {
        given: "a unix OS"
        and: "a executable file name"
        when: "searching for executable file name on path"
        def maybeFile = OSOps.findInOSPath(project, "win${filename}", filename)

        then: "return existing java.io.File object representing it"
        maybeFile.present == exists
        if(exists) {
            maybeFile.get().exists()
            maybeFile.get().name.endsWith(filename)
        }

        where:
        filename | exists
        "sh"     | true
        "another"| false
    }


    @Unroll
    @IgnoreIf({ !isWindows() })
    def "finds external executable with windows filename #filename on path"() {
        given: "a unix OS"
        and: "a executable file name"
        when: "searching for executable file name on path"
        def maybeFile = OSOps.findInOSPath(project, filename, "unix${filename}")

        then: "return existing java.io.File object representing it"
        maybeFile.present == exists
        if (exists) {
            maybeFile.get().exists()
            maybeFile.get().name.endsWith(filename)
        }

        where:
        filename | exists
        "cmd.exe"| true
        "cmd"    | true
        "asd.exe"| false
        "asd"    | false
    }

    @Unroll
    @IgnoreIf({ !isWindows() })
    def "finds external executable #filename on windows path"() {
        given: "a windows OS"
        and: "a executable file name"
        when: "searching for executable file name on path"
        def maybeFile = OSOps.findInOSPath(project, filename)

        then: "return existing java.io.File object representing it"
        maybeFile.present == exists
        if(exists) {
            maybeFile.get().exists()
            maybeFile.get().name.endsWith(filename)
        }
        where:
        filename | exists
        "cmd.exe"| true
        "cmd"    | true
        "asd.exe"| false
        "asd"    | false
    }

    @Unroll
    @IgnoreIf({ isWindows() })
    def "finds external executable #filename on unix path"() {
        given: "a unix OS"
        and: "a executable file name"
        when: "searching for executable file name on path"
        def maybeFile = OSOps.findInOSPath(project, filename)

        then: "return existing java.io.File object representing it"
        maybeFile.present == exists
        if(exists) {
            maybeFile.get().exists()
            maybeFile.get().name.endsWith(filename)
        }

        where:
        filename | exists
        "sh"     | true
        "another"| false
    }

    //https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/unblock-file?view=powershell-7.1
    @Unroll
    @IgnoreIf({ !isWindows() })
    def "unblocks #file windows file"() {
        given: "a blocked NTFS windows file"
        def shell = new GradleShell(project)
        if(blocked) {
            blocksWindowsFile(shell, file)
        }
        when: "file is unblocked via powershell"
        OSOps.unblockFile(shell, file)

        then: "NTFS lock stream file is gone"
        def parentDir = Paths.get(file.absolutePath).parent.toString()
        def res = shell.execute {
            it.executable = "cmd.exe"
            it.args = ["/K", """dir ${parentDir}"" /r"""]
        }
        res.stdOutString.contains("${file.name}")
        !res.stdOutString.contains("${file.name}:Zone.Identifier:\$DATA")

        where:
        file                               | blocked
        emptyTmpFile("blockedFile.bat")    | true
        emptyTmpFile("normalFile.bat")     | false
    }

    @IgnoreIf({ !isWindows() })
    def "fails when unblocking non-existing windows file"() {
        given: "inexistent windows file"
        def falseFile = new File("imnothere")

        when: "file is unblocked via powershell"
        OSOps.unblockFile(new GradleShell(project), falseFile)

        then: "FileNotFoundException is thrown"
        def e = thrown(FileNotFoundException)
        e.message == "File ${falseFile.absolutePath} does not exists"
    }

}
