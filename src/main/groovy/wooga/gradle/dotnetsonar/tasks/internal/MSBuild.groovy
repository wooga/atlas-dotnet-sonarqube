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
