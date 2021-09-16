package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project
import org.gradle.process.ExecSpec

class DotNet implements SolutionBuildTool {

    private final File executable
    private final Shell shell

    static DotNet gradleBased(Project project, File executable) {
        return new DotNet(new GradleShell(project), executable)
    }

    DotNet(Shell shell, File executable) {
        this.shell = shell
        this.executable = executable
    }

    public void buildSolution(File solution, Map<String, ?> environment = new HashMap<>(), List<?> extraArgs=[]) {
        shell.execute { ExecSpec execSpec ->
            execSpec.executable = executable.absolutePath
            execSpec.environment(environment)
            execSpec.args("build")
            execSpec.args(*extraArgs)
            execSpec.args(solution.absolutePath)
        }.throwsOnFailure()
    }

    File getExecutable() {
        return executable
    }
}
