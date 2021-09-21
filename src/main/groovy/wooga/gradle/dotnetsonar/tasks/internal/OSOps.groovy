package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project
import org.gradle.process.ExecSpec

class OSOps {

    private static String osName = System.getProperty("os.name").toLowerCase()

    static boolean isWindows() {
        return osName.contains("windows")
    }

    static Optional<File> findInOSPath(Project project, String windowsFileName, String unixFileName) {
        if(isWindows()) {
            return findInOSPath(project, windowsFileName)
        } else {
            return findInOSPath(project, unixFileName)
        }
    }

    static Optional<File> findInOSPath(Project project, String fileName) {
        return findInOSPath(new GradleShell(project), fileName)
    }

    static Optional<File> findInOSPath(Shell shell, String fileName) {
        ShellResult result
        if(isWindows()) {
            result = shell.execute { ExecSpec execSpec ->
                execSpec.executable = "where.exe"
                execSpec.args = [windowsExecutableName(fileName)]
            }
        } else {
            result = shell.execute { ExecSpec execSpec ->
                execSpec.executable = "which"
                execSpec.args = [fileName]
            }
        }
        result.execResult.rethrowFailure()
        if(result.execResult.exitValue == 0) {
            return Optional.of(new File(result.stdOutString.readLines()[0].trim()))
        } else {
            return Optional.empty()
        }
    }

    static void unblockFile(Shell shell, File file) {
        if(isWindows()) {
            if(!file.exists()) {
                throw new FileNotFoundException("File ${file.absolutePath} does not exists")
            }
            shell.execute { ExecSpec exec ->
                exec.executable = "powershell.exe"
                exec.args = ["""Unblock-File "${file.absolutePath}" """]
            }.throwsOnFailure()
        }
    }


    static String windowsExecutableName(String name) {
        return name.endsWith(".exe")? name : "${name}.exe"
    }
}
