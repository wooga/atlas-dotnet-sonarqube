package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project
import org.gradle.process.ExecSpec

import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ


class OSOps {

    private static String osName = System.getProperty("os.name").toLowerCase()

    static boolean isWindows() {
        return osName.contains("windows")
    }

    static Optional<File> findInOSPath(Project project, String fileName) {
        return findInOSPath(new GradleShell(project), fileName)
    }

    //No windows permissions are directly being set, as nothing gone wrong on it so far.
    //However I do not have a comprehensive understanding of windows permissions. If something goes wrong,
    // this may help:
    //https://stackoverflow.com/questions/664432/how-do-i-programmatically-change-file-permissions/13892920#13892920
//    static void setFilePermissions(File scannerFile, PosixFilePermission... permissions) {
//        if(!isWindows()) {
//            def permissionsSet = permissions.inject(EnumSet.noneOf(PosixFilePermission))
//            { enumSet, permission ->
//                enumSet.add(permission)
//                return enumSet
//            }
//            Files.setPosixFilePermissions(scannerFile.toPath(), permissionsSet)
//        }
//    }

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
            return Optional.of(new File(result.stdOutString.trim()))
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
