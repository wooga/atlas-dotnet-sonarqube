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

import org.gradle.api.Action
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.process.internal.DefaultExecSpec
import org.gradle.process.internal.ExecException
import wooga.gradle.dotnetsonar.tasks.internal.Shell
import wooga.gradle.dotnetsonar.tasks.internal.ShellResult

import java.util.function.Consumer

class FakeShell implements Shell {

    private ExecSpec lastExecSpec
    private int exitStatus

    FakeShell(int exitStatus=0) {
        this.exitStatus = exitStatus
    }

    @Override
    ShellResult execute(boolean logging=true, Consumer<ExecSpec> execSpecClosure) {
        ExecSpec spec = new DefaultExecSpec(SpecFakes.fakeResolver())
        execSpecClosure(spec)
        this.lastExecSpec = spec
        return new ShellResult(new ExecResult() {
                @Override
                int getExitValue() { return exitStatus }
                @Override
                ExecResult assertNormalExitValue() throws ExecException {
                    if(exitStatus > 0) {
                        throw new ExecException("")
                    }
                    return this
                }
                @Override
                ExecResult rethrowFailure() throws ExecException { return this }
            }, null, null)
    }

    ExecSpec getLastExecSpec() {
        return lastExecSpec
    }
}
