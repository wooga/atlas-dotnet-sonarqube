package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission

class SonarScannerInstaller {

    //this executable only exists for .NET [4.6, 5)
    public static final String EXECUTABLE_NAME = "SonarScanner.MSBuild.exe";
    public static final String DOTNET_VERSION = "net46";
    private static final String defaultBaseURL = "https://github.com/SonarSource/sonar-scanner-msbuild/releases/download"

    private Downloader downloader
    private GradleUnzipper unzipper

    static SonarScannerInstaller gradleBased(Project project) {
        return new SonarScannerInstaller(new Downloader(), GradleUnzipper.gradleBased(project))
    }

    SonarScannerInstaller(Downloader downloader, GradleUnzipper unzipper) {
        this.unzipper = unzipper
        this.downloader = downloader
    }

    File install(String version, String dotnetVersion, File installationDir) {
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

        def scannerFile = findScannerExecutableFile(installationDir)
        if(!OSOps.isWindows()) {
            Files.setPosixFilePermissions(scannerFile.toPath(), EnumSet.of(
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.OTHERS_EXECUTE))
        }
        return scannerFile
    }

    private static File findScannerExecutableFile(File installationDir) {
        def installedFiles = Files.walk(Paths.get(installationDir.absolutePath))
        def maybeScannerExec = installedFiles.filter { path ->
            def file = path.toFile()
            return file.isFile() && file.absolutePath.endsWith(EXECUTABLE_NAME)
        }.findFirst()
        return maybeScannerExec.orElseThrow {
            new FileNotFoundException("Couldn't find sonar-scanner executable in installed package")
        }.toFile()
    }
}
