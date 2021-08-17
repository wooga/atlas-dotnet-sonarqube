package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project
import org.gradle.process.ExecSpec

class GradleMonoShell implements Shell {

    private Shell baseShell;
    private File monoExecutable


    static GradleMonoShell forProject(Project project, File monoExecutable) {
        Shell baseShell = new GradleShell(project)
        return new GradleMonoShell(baseShell, monoExecutable)
    }

    GradleMonoShell(Shell baseShell, File monoExecutable) {
        this.baseShell = baseShell
        this.monoExecutable = monoExecutable
    }

    @Override
    public ShellResult execute(boolean logging=true, Closure execSpecClosure) {
        return baseShell.execute { ExecSpec exec ->
            execSpecClosure(exec)
            exec.args = [exec.executable] + exec.args
            exec.executable = monoExecutable.absolutePath
        }
    }
}
