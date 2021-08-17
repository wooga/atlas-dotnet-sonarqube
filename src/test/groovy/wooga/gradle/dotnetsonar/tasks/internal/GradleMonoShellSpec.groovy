package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.process.ExecSpec
import spock.lang.Specification

import static wooga.gradle.dotnetsonar.utils.SpecFakes.argReflectingFakeExecutable
import static wooga.gradle.dotnetsonar.utils.SpecFakes.fakeShell
import static wooga.gradle.dotnetsonar.utils.SpecUtils.emptyTmpFile

class GradleMonoShellSpec extends Specification {

    def "sets up execution specification to be ran using given mono executable"() {
        given: "a mono executable"
        def monoExec = emptyTmpFile("mono")
        and: "a executable file to be executed using mono"
        def runExec = argReflectingFakeExecutable("executable", 0)
        and: "a base shell"
        def shell = fakeShell()

        when: "executing given file using gradle mono shell"
        def monoShell = new GradleMonoShell(shell, monoExec)
        monoShell.execute { ExecSpec exec ->
            exec.executable = runExec.absolutePath
            exec.args = ["arg1", "arg2"]
        }

        then: "spec should run using mono"
        shell.lastExecSpec.executable == monoExec.absolutePath
        and: "executable file and its arguments must be present in order in the argument list"
        shell.lastExecSpec.args == [runExec.absolutePath, "arg1", "arg2"]
    }
}
