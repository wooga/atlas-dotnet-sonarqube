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

import java.util.function.Consumer

class MonoShell implements Shell {

    private Shell baseShell;
    private File monoExecutable


    static MonoShell forProject(Project project, File monoExecutable) {
        Shell baseShell = new GradleShell(project)
        return new MonoShell(baseShell, monoExecutable)
    }

    MonoShell(Shell baseShell, File monoExecutable) {
        this.baseShell = baseShell
        this.monoExecutable = monoExecutable
    }

    @Override
    public ShellResult execute(boolean logging=true, Consumer<ExecSpec> execSpecClosure) {
        return baseShell.execute { ExecSpec exec ->
            execSpecClosure(exec)
            exec.args = [exec.executable] + exec.args
            exec.executable = monoExecutable.absolutePath
        }
    }
}
