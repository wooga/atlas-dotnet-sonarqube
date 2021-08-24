package wooga.gradle.dotnetsonar

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner
import wooga.gradle.dotnetsonar.tasks.internal.SonarScannerFactory

class SonarScannerInstallInfo {
    private Property<String> sourceURL
    private Property<String> version
    private DirectoryProperty installDir

    SonarScannerInstallInfo(Project project) {
        def buildDir = project.layout.buildDirectory
        this.sourceURL = project.objects.property(String)
        this.version = project.objects.property(String).convention("5.2.2.33595")
        this.installDir = project.objects.directoryProperty().convention(buildDir.dir("bin/net-sonarscanner"))
    }

    Provider<SonarScanner> provideScannerFromRemote(Project project, SonarScannerFactory factory, File workingDir) {
        return sourceURL.
        map({String sourceURL ->
            factory.fromRemoteURL(project, new URL(sourceURL), installDir.get().asFile, workingDir)
        }.memoize()).
        orElse(project.provider({
            factory.fromRemote(project, version.get(), installDir.get().asFile, workingDir)
        }.memoize()))
    }

    Property<String> getSourceURL() {
        return sourceURL
    }

    Property<String> getVersion() {
        return version
    }

    DirectoryProperty getInstallDir() {
        return installDir
    }

    void setSourceURL(Property<String> sourceURL) {
        this.sourceURL.set(sourceURL)
    }

    void setSourceURL(String sourceURL) {
        this.sourceURL.set(sourceURL)
    }

    void setVersion(Property<String> version) {
        this.version.set(version)
    }

    void setVersion(String version) {
        this.version.set(version)
    }

    void setInstallDir(DirectoryProperty installDir) {
        this.installDir.set(installDir)
    }

    void setInstallDir(File installDir) {
        this.installDir.set(installDir)
    }

    void setInstallDir(String installDir) {
        this.installDir.set(new File(installDir))
    }
}