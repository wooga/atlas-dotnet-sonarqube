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
import org.gradle.api.file.CopySpec

class GradleUnzipper {

    private Shell shell
    private Project project

    static GradleUnzipper gradleBased(Project project) {
        return new GradleUnzipper(new GradleShell(project), project)
    }

    public GradleUnzipper(Shell shell, Project project) {
        this.shell = shell
        this.project = project
    }

    def unzip(File zippedFile, File installationDir) {
        OSOps.unblockFile(shell, zippedFile)
        def zipContents = project.zipTree(zippedFile)
        project.copy { CopySpec spec ->
            spec.from(zipContents)
            spec.into(installationDir)
        }
        return installationDir
    }
}
