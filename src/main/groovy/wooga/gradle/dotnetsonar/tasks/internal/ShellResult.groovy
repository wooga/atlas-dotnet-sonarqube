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

import org.gradle.process.ExecResult

class ShellResult {

    private final ExecResult execResult
    private final ByteArrayOutputStream stdOut;
    private final ByteArrayOutputStream stdErr;

    ShellResult(ExecResult execResult, ByteArrayOutputStream stdOut, ByteArrayOutputStream stdErr) {
        this.execResult = execResult
        this.stdOut = stdOut
        this.stdErr = stdErr
    }

    void throwsOnFailure() {
        execResult.assertNormalExitValue().rethrowFailure()
    }

    ExecResult getExecResult() {
        return execResult
    }

    ByteArrayOutputStream getStdOut() {
        return stdOut
    }

    String getStdOutString() {
        return stdOut.toString()
    }

    ByteArrayOutputStream getStdErr() {
        return stdOut
    }

    String getStdErrString() {
        return stdErr.toString()
    }
}
