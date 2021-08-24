package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project
import org.gradle.process.ExecSpec

class GradleShell implements Shell {

    private final Project project

    GradleShell(Project project) {
        this.project = project
    }

    @Override
    public ShellResult execute(boolean logging=true, Closure execSpecClosure) {
        def stdOut = new ByteArrayOutputStream()
        def stdErr = new ByteArrayOutputStream()
        try {
            def execResult = project.exec { ExecSpec execSpec ->
                execSpec.standardOutput = stdOut
                execSpec.errorOutput = stdErr
                execSpec.ignoreExitValue = true
                execSpecClosure(execSpec)
            }
            stdOut.flush()
            stdErr.flush()
            if(logging) {
                project.logger.info(stdOut.toString())
                project.logger.error(stdErr.toString())
            }
            return new ShellResult(execResult, stdOut, stdErr)
        } finally {
            stdOut.close()
            stdErr.close()
        }
    }


}
