/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.process.ExecSpec

class OSOps {

    private static String osName = System.getProperty("os.name").toLowerCase()

    static boolean isWindows() {
        return osName.contains("windows")
    }

    static Provider<RegularFile> findInOSPathProvider(Project project, String windowsFileName, String unixFileName) {
        project.provider {
            def maybeExecutable = findInOSPath(project, windowsFileName, unixFileName).map {executable ->
                project.layout.projectDirectory.file(executable.absolutePath)
            }
            return maybeExecutable.orElse(null)
        }
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

    static Provider<RegularFile> findInOSPathProvider(Project project, String fileName) {
        project.provider {
            def maybeExecutable = findInOSPath(project, fileName).map {executable ->
                project.layout.projectDirectory.file(executable.absolutePath)
            }
            return maybeExecutable.orElse(null)
        }
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
