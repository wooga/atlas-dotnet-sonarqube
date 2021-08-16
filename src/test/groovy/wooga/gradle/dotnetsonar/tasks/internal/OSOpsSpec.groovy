package wooga.gradle.dotnetsonar.tasks.internal

import nebula.test.ProjectSpec
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Unroll

import java.nio.file.Paths

import static wooga.gradle.dotnetsonar.utils.SpecUtils.blocksWindowsFile
import static wooga.gradle.dotnetsonar.utils.SpecUtils.emptyTmpFile

class OSOpsSpec extends ProjectSpec {

    @Shared
    private Shell shell

    def setup() {
        shell = new GradleShell(project)
    }

    @Unroll
    @IgnoreIf({ !System.getProperty("os.name").toLowerCase().contains("windows") })
    def "finds external executable #filename on windows path"() {
        given: "a windows OS"
        and: "a executable file name"
        when: "searching for executable file name on path"
        def maybeFile = OSOps.findInOSPath(new GradleShell(project), filename)

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
    @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
    def "finds external executable #filename on unix path"() {
        given: "a unix OS"
        and: "a executable file name"
        when: "searching for executable file name on path"
        def maybeFile = OSOps.findInOSPath(new GradleShell(project), filename)

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
    @IgnoreIf({ !System.getProperty("os.name").toLowerCase().contains("windows") })
    def "unblocks #file windows file"() {
        given: "a blocked NTFS windows file"
        if(blocked) {
            blocksWindowsFile(shell, file)
        }
        when: "file is unblocked via powershell"
        OSOps.unblockFile(new GradleShell(project), file)

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

    @IgnoreIf({ !System.getProperty("os.name").toLowerCase().contains("windows") })
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
