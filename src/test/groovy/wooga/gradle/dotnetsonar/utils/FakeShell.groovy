package wooga.gradle.dotnetsonar.utils


import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.process.internal.DefaultExecSpec
import org.gradle.process.internal.ExecException
import wooga.gradle.dotnetsonar.tasks.internal.Shell
import wooga.gradle.dotnetsonar.tasks.internal.ShellResult

class FakeShell implements Shell {

    private ExecSpec lastExecSpec
    private int exitStatus

    FakeShell(int exitStatus=0) {
        this.exitStatus = exitStatus
    }

    @Override
    ShellResult execute(boolean logging=false, Closure execSpecClosure) {
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
