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

package wooga.gradle.dotnetsonar.utils

import nebula.test.functional.ExecutionResult
import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils

import java.nio.file.Path

import static SpecUtils.isWindows

class FakeExecutable {

    static File runFirstParam(Path fakeFilePath, boolean overwrites=true) {
        File fakeExec = setupExecutableFile(fakeFilePath, overwrites)
        if(isWindows()) {
            //https://stackoverflow.com/questions/935609/batch-parameters-everything-after-1
            fakeExec << """
                @echo off
                echo [[${fakeExec.name}]]
                for /f "tokens=1,* delims= " %%a in ("%*") do set ALL_BUT_FIRST=%%b 
                call %1 %ALL_BUT_FIRST%
                echo [[end ${fakeExec.name}]]
                exit %errorlevel%
            """.stripIndent()
        } else {
            //running subscript with '.' in practice includes it on the mother script, including its parameters
            fakeExec <<
                    """#!/usr/bin/env bash
                echo [[mono]]
                fst_param=\$1
                shift
                . "\${fst_param}"
                echo [[end mono]]
                exit \$?
            """.stripIndent()
        }
    }

    static File argsReflector(Path fakeFilePath, int exitCode, boolean overwrites=true) {
        File fakeExec = setupExecutableFile(fakeFilePath, overwrites)
        if (isWindows()) {
            fakeExec << """
                @echo off
                echo [[${fakeExec.name}]]
                echo [[arguments]]
                echo %*
                echo [[environment]]
                set
                echo [[end]]
                echo [[end ${fakeExec.name}]]
                exit ${exitCode}
            """.stripIndent()
        } else {
            fakeExec << """
                #!/usr/bin/env bash
                echo [[${fakeExec.name}]]
                echo [[arguments]]
                echo \$@
                echo [[environment]]
                env
                echo [[end]]
                echo [[end ${fakeExec.name}]]
                exit ${exitCode}
            """.stripIndent()
        }
    }

    private static File setupExecutableFile(Path fakeFilePath, boolean overwrites) {
        File fakeExec = fakeFilePath.toFile()
        if (fakeExec.exists()) {
            if (overwrites) {
                fakeExec.delete()
            } else {
                throw new IllegalArgumentException("File ${fakeFilePath} already exists")
            }
        }
        fakeExec.createNewFile()
        fakeExec.executable = true
        return fakeExec
    }

    static Result lastExecutionResults(ExecutionResult executionResult) {
        return lastExecutionResults(executionResult.standardOutput)
    }

    static Result lastExecutionResults(String stdout) {
        return new Result(stdout)
    }


    static Result executionResults(File file, ExecutionResult executionResult) {
        return new Result(file, executionResult.standardOutput)
    }

    static class Result {
        private final File file;
        private final ArrayList<String> args;
        private final Map<String, String> envs;

        Result(String stdOutput) {
            this(null, stdOutput)
        }

        Result(File file, String stdOutput) {
            this.file = file
            this.args = loadArgs(file, stdOutput)
            this.envs = loadEnvs(file, stdOutput)
        }

        ArrayList<String> getArgs() {
            return args
        }

        Map<String, String> getEnvs() {
            return envs
        }

        private static ArrayList<String> loadArgs(File file, String stdOutput) {
            def logs = stdOutput
            if(file != null) {
                logs = substringBetween(stdOutput, "[[${file.name}]]", "[[end ${file.name}]]")
            }
            loadArgs(logs)
        }

        private static Map<String, String> loadEnvs(File file, String stdOutput) {
            def logs = stdOutput
            if(file != null) {
                logs = substringBetween(stdOutput, "[[${file.name}]]", "[[end ${file.name}]]")
            }
            return loadEnvs(logs)
        }

        private static ArrayList<String> loadArgs(String stdOutput) {
            def lastExecutionOffset = stdOutput.lastIndexOf("[[arguments]]")
            if(lastExecutionOffset < 0) {
                System.out.println(stdOutput)
                throw new IllegalArgumentException("stdout does not contains a execution of a file generated by " +
                        "FakeExecutable.argsReflector")
            }
            def lastExecTailString = stdOutput.substring(lastExecutionOffset)
            def argsString = substringBetween(lastExecTailString, "[[arguments]]", "[[environment]]").
                                replace("[[arguments]]", "")
            def parts = argsString.split(" ").
                    findAll {!StringUtils.isEmpty(it) }.collect{ it.trim() }
            return parts
        }

        private static Map<String, String> loadEnvs(String stdOutput) {
            def argsString = substringBetween(stdOutput, "[[environment]]", "[[end]]").
                    replace("[[environment]]", "")
            def parts = argsString.split(System.lineSeparator()).
                    findAll {!StringUtils.isEmpty(it) }.collect{ it.trim() }
            return parts.collectEntries {
                return it.split("=", 2)
            }
        }
    }

    private static String substringBetween(String base, String from, String to) {
        def customArgsIndex = base.indexOf(from)
        def tailString = base.substring(customArgsIndex)
        def endIndex = tailString.indexOf(to)
        return tailString.substring(0, endIndex)
    }
}
