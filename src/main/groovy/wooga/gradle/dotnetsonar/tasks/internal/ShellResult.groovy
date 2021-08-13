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
