package wooga.gradle.dotnetsonar.tasks

import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import wooga.gradle.dotnet.tasks.DotnetTask
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner
import wooga.gradle.dotnetsonar.tasks.traits.SonarScannerSpec

abstract class SonarScannerTask extends DotnetTask implements SonarScannerSpec {

    SonarScannerTask() {
        this.executableName.convention(dotnetExecutable)
    }

    @Internal
    Provider<SonarScanner> getSonarScanner() {
        return sonarScannerExecutable.map {new SonarScanner(it.asFile) }
    }
}
