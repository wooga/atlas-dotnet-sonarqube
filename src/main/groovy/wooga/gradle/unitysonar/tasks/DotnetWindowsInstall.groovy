package wooga.gradle.unitysonar.tasks

import com.wooga.gradle.ArgumentsSpec
import com.wooga.gradle.io.ExecSpec
import com.wooga.gradle.io.ProcessExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import wooga.gradle.dotnetsonar.tasks.internal.Downloader

class DotnetWindowsInstall extends DefaultTask implements ArgumentsSpec, ExecSpec {

    @InputFile
    final RegularFileProperty dotnetInstallScript = objects.fileProperty()
    @Input
    final Property<String> version = objects.property(String)
    @InputDirectory
    final DirectoryProperty installDir = objects.directoryProperty()
    @OutputFile
    final RegularFileProperty dotnetExecutable = objects.fileProperty()

    Provider<String> dotnetExecutable() {
        return dotnetExecutable.map {
            it.asFile.absolutePath
        }
    }

    DotnetWindowsInstall() {
        dotnetInstallScript.convention(layout.file(project.provider {
            def installer = File.createTempFile("dotnet-install", ".ps1")
            new Downloader().download("https://dot.net/v1/dotnet-install.ps1".toURL(), installer)
            return installer
        }))
        executableName.convention(dotnetInstallScript.asFile.map {it.absolutePath})
        internalArguments = version.zip(installDir) { version, installDir ->
            ["-Version", version, "-InstallDir", installDir.asFile.absolutePath, "-NoPath"]
        }
        dotnetExecutable.set(installDir.file("dotnet.exe"))
        onlyIf {
            def noDotnetExecutable = !installDir.file("dotnet").map {it.asFile.file }.getOrElse(false)
            if(!noDotnetExecutable) {
                logger.info("Dotnet executable already present in " +
                        "${dotnetExecutable.get().asFile.absolutePath}, skipping...")
            }
        }
    }

    @TaskAction
    def install() {
        ExecResult execResult = ProcessExecutor.from(this)
                .withArguments(this)
                .withWorkingDirectory(workingDirectory.getOrNull())
                .ignoreExitValue()
                .execute()
        execResult.assertNormalExitValue()
    }
}

