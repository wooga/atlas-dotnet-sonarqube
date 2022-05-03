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

    static File runFirstParameterFakeExecutable(String fakeFilePath) {
        String osAwareFakePath = isWindows() && !fakeFilePath.endsWith(".bat")?
                "${fakeFilePath}.bat" :
                fakeFilePath
        def fakeExecFile = FakeExecutable.runFirstParam(Paths.get(osAwareFakePath))
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
