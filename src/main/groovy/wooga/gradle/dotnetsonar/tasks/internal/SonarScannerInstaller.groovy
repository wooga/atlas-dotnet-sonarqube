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

import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

class SonarScannerInstaller {

    //this executable only exists for .NET [4.6, 5)
    public static final String EXECUTABLE_NAME = "SonarScanner.MSBuild.exe";
    public static final String DLL_NAME = "SonarScanner.MSBuild.dll";
    public static final String DOTNET_VERSION = "net46";
    public static final String defaultBaseURL = "https://github.com/SonarSource/sonar-scanner-msbuild/releases/download"

    private Downloader downloader
    private GradleUnzipper unzipper

    static SonarScannerInstaller gradleBased(Project project) {
        return new SonarScannerInstaller(new Downloader(), GradleUnzipper.gradleBased(project))
    }

    SonarScannerInstaller(Downloader downloader, GradleUnzipper unzipper) {
        this.unzipper = unzipper
        this.downloader = downloader
    }

    File install(String version, String dotnetVersion=DOTNET_VERSION, File installationDir) {
        def scannerPkg = "sonar-scanner-msbuild-${version}-${dotnetVersion}.zip"
        def scannerURL = "${defaultBaseURL}/${version}/${scannerPkg}"
        return install(new URL(scannerURL), installationDir)
    }

    File install(URL source, File installationDir) {
        if(!installationDir.exists()) {
            installationDir.mkdirs()
        }
        if(!installationDir.directory) {
            throw new IllegalArgumentException("installationDir should be a directory")
        }
        def zippedFile = new File(installationDir, "dotnet-sonarscanner.zip")
        downloader.download(source, zippedFile, true)
        unzipper.unzip(zippedFile, installationDir)
        zippedFile.delete()
        def executableFiles = findScannerExecutableFiles(installationDir)
        executableFiles.every {
            it.readable = true
            it.executable = true
        }
        def sonarScannerEntrypoint = findSonarScannerEntrypoint(installationDir)
        return sonarScannerEntrypoint.orElseThrow {
            new FileNotFoundException("Couldn't find sonar-scanner executable in installed package")
        }
    }
    public static Optional<File> findSonarScannerEntrypoint(File installationDir, boolean forceExecutable=false) {
        def targetFiles = forceExecutable?
                ["SonarScanner.MSBuild.exe"] :
                ["SonarScanner.MSBuild.dll", "SonarScanner.MSBuild.exe"]

        Files.walk(installationDir.toPath()).filter {
            it.fileName.toString() in targetFiles
        }
        .map {it.toFile()}
        .findFirst()
    }

    public static List<File> findScannerExecutableFiles(File installationDir) {
        if(!installationDir.directory) {
            return []
        }
        def installedFiles = Files.walk(Paths.get(installationDir.absolutePath))
        return installedFiles.filter { path ->
            def file = path.toFile()
            def isBaseExecutable = file.isFile() && file.absolutePath.endsWith(EXECUTABLE_NAME)
            def isFromBinFolder = file.isFile() && path.parent.fileName.toString() == "bin"
            def isDotExeFile = file.isFile() && path.parent.fileName.toString().endsWith(".exe")
            return isBaseExecutable || isFromBinFolder || isDotExeFile
        }.map{it.toFile()}.collect(Collectors.toList())
    }
}
