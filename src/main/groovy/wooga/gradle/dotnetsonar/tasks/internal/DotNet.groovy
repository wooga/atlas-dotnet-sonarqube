package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project

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

    public void buildSolution(File solution, Map<String, ?> environment = new HashMap<>()) {
        shell.execute { execSpec ->
            execSpec.executable = executable.absolutePath
            execSpec.environment(environment)
            execSpec.args("build")
            execSpec.args(solution.absolutePath)
        }.throwsOnFailure()
    }

    File getExecutable() {
        return executable
    }
}
