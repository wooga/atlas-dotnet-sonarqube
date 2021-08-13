package wooga.gradle.dotnetsonar.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import wooga.gradle.dotnetsonar.SonarScannerExtension
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner
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
        sonarScannerExt.sonarScanner = downloadSonarScanner()
    }

    private SonarScanner downloadSonarScanner() {
        def version = version.get()
        def dotnetVersion = SonarScannerInstaller.DOTNET_VERSION
        def installationDir = installationDir.map{it.dir("sonarscanner-${version}")}.get()
        def scannerInstaller = SonarScannerInstaller.gradleBased(project)

        def scannerExec = sourceURL.map{urlStr ->
            scannerInstaller.install(new URL(urlStr), installationDir.asFile)
        }.orElse(
                project.provider { scannerInstaller.install(version, dotnetVersion, installationDir.asFile) }
        ).get()
        return SonarScanner.gradleBased(project, scannerExec, project.projectDir)
    }

    @Input @Optional
    Property<String> getSourceURL() {
        return sourceURL
    }

    @Input
    Property<String> getVersion() {
        return version
    }

    @Input
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
