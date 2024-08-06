package wooga.gradle.dotnetsonar.tasks.traits

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner

trait SonarScannerSpec implements BaseSpec {

    private final Property<String> dotnetExecutable = objects.property(String)

    @Input
    @Optional
    Property<String> getDotnetExecutable() {
        return dotnetExecutable
    }

    void setDotnetExecutable(String dotnetExecutable) {
        this.dotnetExecutable.set(dotnetExecutable)
    }

    void setDotnetExecutable(Provider<?> dotnetExecutable) {
        this.dotnetExecutable.set(dotnetExecutable.map {
            if(it instanceof RegularFile) {
                return it.asFile.absolutePath
            }
            if(it instanceof File) {
                return it.absolutePath
            }
            if(it instanceof String || it instanceof GString) {
                return it.toString()
            }
            throw new IllegalArgumentException("DotnetExecutable must be a Provider<RegularFile|File|String>")
        })
    }

    void setDotnetExecutable(File dotnetExecutable) {
        this.dotnetExecutable.set(dotnetExecutable.absolutePath)
    }

    void setDotnetExecutable(RegularFile dotnetExecutable) {
        this.dotnetExecutable.set(dotnetExecutable.asFile.absolutePath)
    }

    private final RegularFileProperty sonarScannerExecutable = objects.fileProperty()

    @InputFile
    @Optional
    RegularFileProperty getSonarScannerExecutable() {
        return sonarScannerExecutable
    }

    void setSonarScannerExecutable(Provider<RegularFile> sonarScannerExecutable) {
        this.sonarScannerExecutable.set(sonarScannerExecutable)
    }

    void setSonarScannerExecutable(RegularFile sonarScannerExecutable) {
        this.sonarScannerExecutable.set(sonarScannerExecutable)
    }

    void setSonarScannerExecutable(File sonarScannerExecutable) {
        this.sonarScannerExecutable.set(sonarScannerExecutable)
    }

    Provider<SonarScanner> getSonarScanner() {
        sonarScannerExecutable.map {
            new SonarScanner(it.asFile)
        }
    }
}
