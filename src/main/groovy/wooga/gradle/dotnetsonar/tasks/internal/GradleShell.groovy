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
import org.gradle.process.ExecSpec

class GradleShell implements Shell {

    private final Project project

    GradleShell(Project project) {
        this.project = project
    }

    @Override
    public ShellResult execute(boolean logging=true, Closure execSpecClosure) {
        def stdOut = new ByteArrayOutputStream()
        def stdErr = new ByteArrayOutputStream()
        try {
            def execResult = project.exec { ExecSpec execSpec ->
                execSpec.standardOutput = stdOut
                execSpec.errorOutput = stdErr
                execSpec.ignoreExitValue = true
                execSpecClosure(execSpec)
            }
            stdOut.flush()
            stdErr.flush()
            if(logging) {
                project.logger.info(stdOut.toString())
                project.logger.error(stdErr.toString())
            }
            return new ShellResult(execResult, stdOut, stdErr)
        } finally {
            stdOut.close()
            stdErr.close()
        }
    }


}
