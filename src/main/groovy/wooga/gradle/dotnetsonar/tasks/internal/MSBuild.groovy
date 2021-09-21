package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project

class MSBuild implements SolutionBuildTool {
    private final File executable
    private final Shell shell

    static MSBuild gradleBased(Project project, File executable) {
        return new MSBuild(new GradleShell(project), executable)
    }

    MSBuild(Shell shell, File executable) {
        this.shell = shell
        this.executable = executable
    }

    public void buildSolution(File solution, Map<String, ?> environment = new HashMap<>(), List<?> extraArgs=[]) {
        shell.execute { execSpec ->
            execSpec.executable = executable.absolutePath
            execSpec.environment(environment)
            execSpec.args(*extraArgs)
            execSpec.args(solution.absolutePath)
        }.throwsOnFailure()
    }

    File getExecutable() {
        return executable
    }
}
