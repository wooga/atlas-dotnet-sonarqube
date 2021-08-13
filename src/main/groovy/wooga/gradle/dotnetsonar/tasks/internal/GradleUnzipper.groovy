package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project
import org.gradle.api.file.CopySpec

class GradleUnzipper {

    private Shell shell
    private Project project

    static GradleUnzipper gradleBased(Project project) {
        return new GradleUnzipper(new GradleShell(project), project)
    }

    public GradleUnzipper(Shell shell, Project project) {
        this.shell = shell
        this.project = project
    }

    def unzip(File zippedFile, File installationDir) {
        OSOps.unblockFile(shell, zippedFile)
        def zipContents = project.zipTree(zippedFile)
        project.copy { CopySpec spec ->
            spec.from(zipContents)
            spec.into(installationDir)
        }
        return installationDir
    }
}
