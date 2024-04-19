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

package wooga.gradle.dotnetsonar.tasks

import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import wooga.gradle.dotnet.tasks.Build

class BuildSolution extends Build {

    private final RegularFileProperty solution = objects.fileProperty()

    @InputFile
    @Optional
    RegularFileProperty getSolution() {
        return solution
    }

    void setSolution(Provider<RegularFile> solution) {
        this.solution.set(solution)
    }

    void setSolution(RegularFile solution) {
        this.solution.set(solution)
    }

    void setSolution(File solution) {
        this.solution.set(solution)
    }

    BuildSolution() {
        this.target.convention(solution.map {it.asFile.absolutePath})
    }
}
