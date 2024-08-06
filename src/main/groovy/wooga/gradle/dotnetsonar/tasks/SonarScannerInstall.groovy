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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import wooga.gradle.dotnetsonar.SonarScannerExtension
import wooga.gradle.dotnetsonar.tasks.internal.SonarScannerInstaller

class SonarScannerInstall extends DefaultTask {

    public static final String EXECUTABLE_NAME = SonarScannerInstaller.EXECUTABLE_NAME

    private Property<String> sourceURL
    private Property<String> version
    private DirectoryProperty installationDir

    static void configureDefaultInstall(SonarScannerInstall scannerInstallTask) {
        def project = scannerInstallTask.project
        scannerInstallTask.with {
            version.convention("5.2.2.33595")
            installationDir.convention(project.layout.buildDirectory.dir("bin/net-sonarscanner"))
        }
        scannerInstallTask.onlyIf {
            SonarScannerExtension sonarScannerExt = project.extensions.getByType(SonarScannerExtension)
            return !sonarScannerExt.sonarScanner.present
        }
    }

    SonarScannerInstall() {
        this.sourceURL = project.objects.property(String)
        this.version = project.objects.property(String)
        this.installationDir = project.objects.directoryProperty()
    }

    @TaskAction
    def run() {
        SonarScannerExtension sonarScannerExt = project.extensions.getByType(SonarScannerExtension)
        def scannerExec = downloadSonarScannerFiles()
        sonarScannerExt.sonarScanner = sonarScannerExt.createSonarScanner(scannerExec)
    }

    private File downloadSonarScannerFiles() {
        def version = version.get()
        def dotnetVersion = SonarScannerInstaller.DOTNET_VERSION
        def installationDir = installationDir.map{it.dir("sonarscanner-${version}")}.get()
        def scannerInstaller = SonarScannerInstaller.gradleBased(project)

        return sourceURL.map{urlStr ->
            scannerInstaller.install(new URL(urlStr), installationDir.asFile)
        }.orElse(
                project.provider { scannerInstaller.install(version, dotnetVersion, installationDir.asFile) }
        ).get()
    }

    @Input @Optional
    Property<String> getSourceURL() {
        return sourceURL
    }

    @Input
    Property<String> getVersion() {
        return version
    }

    @InputDirectory
    DirectoryProperty getInstallationDir() {
        return installationDir
    }

    void setSourceURL(String sourceURL) {
        this.sourceURL.set(sourceURL)
    }

    void setVersion(String version) {
        this.version.set(version)
    }

    void setInstallationDir(String installationDir) {
        this.installationDir.set(project.layout.projectDirectory.dir(installationDir))
    }

    void setInstallationDir(File installationDir) {
        this.installationDir.set(installationDir)
    }
}
