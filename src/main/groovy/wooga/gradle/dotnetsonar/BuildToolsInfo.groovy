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

package wooga.gradle.dotnetsonar

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider

class BuildToolsInfo {
    private RegularFileProperty dotnetExecutable
    private RegularFileProperty msBuildExecutable

    BuildToolsInfo(Project project) {
        this.dotnetExecutable = project.objects.fileProperty()
        this.msBuildExecutable = project.objects.fileProperty()
    }

    RegularFileProperty getDotnetExecutable() {
        return dotnetExecutable
    }

    void setDotnetExecutable(Provider<RegularFile> dotnetExecutable) {
        this.dotnetExecutable.set(dotnetExecutable)
    }

    void setDotnetExecutable(RegularFile dotnetExecutable) {
        this.dotnetExecutable.set(dotnetExecutable)
    }

    void setDotnetExecutable(File dotnetExecutable) {
        this.dotnetExecutable.set(dotnetExecutable)
    }

    RegularFileProperty getMsBuildExecutable() {
        return msBuildExecutable
    }

    void setMsBuildExecutable(Provider<RegularFile> msBuildExecutable) {
        this.msBuildExecutable.set(msBuildExecutable)
    }

    void setMsBuildExecutable(RegularFile msBuildExecutable) {
        this.msBuildExecutable.set(msBuildExecutable)
    }

    void setMsBuildExecutable(File msBuildExecutable) {
        this.msBuildExecutable.set(msBuildExecutable)
    }

}
