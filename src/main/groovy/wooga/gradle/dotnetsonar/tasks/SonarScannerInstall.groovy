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

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import wooga.gradle.dotnetsonar.tasks.internal.SonarScannerInstaller
import wooga.gradle.dotnetsonar.tasks.traits.SonarScannerInstallSpec

class SonarScannerInstall extends DefaultTask implements SonarScannerInstallSpec {

    private final RegularFileProperty sonarScannerEntrypoint = objects.fileProperty()

    @Internal
    Provider<RegularFile> getSonarScannerEntrypoint() {
        return sonarScannerEntrypoint
    }

    SonarScannerInstall() {
        sonarScannerEntrypoint.convention(installDir.map { installDir ->
            def sonarScanner = SonarScannerInstaller.findSonarScannerEntrypoint(installDir.asFile)
            return sonarScanner.map {installDir.file(it.absolutePath) }.orElse(null)
        })

        onlyIf {
            def lacksScanner = !sonarScannerEntrypoint.present
            if(!lacksScanner) {
                logger.info("SonarScanner already installed in " +
                        "${sonarScannerEntrypoint.get().asFile.parentFile.absolutePath}, skipping"
                )
            }
            return lacksScanner
        }
    }

    @TaskAction
    def run() {
        def installationDir = installDir.get()
        def scannerInstaller = SonarScannerInstaller.gradleBased(project)

        def executable = scannerInstaller.install(new URL(installURL.get()), installationDir.asFile)
        sonarScannerEntrypoint.set(executable)
    }
}
