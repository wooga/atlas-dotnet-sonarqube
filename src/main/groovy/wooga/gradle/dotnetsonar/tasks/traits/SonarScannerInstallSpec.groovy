package wooga.gradle.dotnetsonar.tasks.traits

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory

trait SonarScannerInstallSpec extends BaseSpec{

    private final Property<String> installURL = objects.property(String)

    @Input
    Property<String> getInstallURL() {
        return installURL
    }

    void setInstallURL(String sourceURL) {
        this.installURL.set(sourceURL)
    }

    void setInstallURL(Provider<String> sourceURL) {
        this.installURL.set(sourceURL)
    }

    private final Property<String> version = objects.property(String)

    @Input
    Property<String> getVersion() {
        return version
    }

    void setVersion(String version) {
        this.version.set(version)
    }

    void setVersion(Provider<String> version) {
        this.version.set(version)
    }

    private final DirectoryProperty installDir = objects.directoryProperty()

    @InputDirectory
    DirectoryProperty getInstallDir() {
        return installDir
    }

    void setInstallDir(Provider<Directory> installationDir) {
        this.installDir.set(installationDir)
    }

    void setInstallDir(Directory installationDir) {
        this.installDir.set(installationDir)
    }

    void setInstallDir(File installationDir) {
        this.installDir.set(installationDir)
    }
}
