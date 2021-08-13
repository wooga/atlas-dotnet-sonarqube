package wooga.gradle.dotnetsonar.utils


import org.gradle.internal.file.PathToFileResolver

import java.nio.file.Paths

import static SpecUtils.isWindows

class SpecFakes {


    static File argReflectingFakeExecutable(String fakeFilePath, exitCode = 0) {
        String osAwareFakePath = isWindows() && !fakeFilePath.endsWith(".bat")?
                                    "${fakeFilePath}.bat" :
                                    fakeFilePath
        def fakeExecFile = FakeExecutable.argsReflector(Paths.get(osAwareFakePath), exitCode)
        fakeExecFile.deleteOnExit()
        return fakeExecFile
    }

    static PathToFileResolver fakeResolver() {
        return new PathToFileResolver() {
            @Override
            File resolve(Object o) { return o as File }
            @Override
            PathToFileResolver newResolver(File file) { return this }
            @Override
            boolean canResolveRelativePath() { return false }
        }
    }

    static FakeShell fakeShell() {
        return new FakeShell()
    }
}
