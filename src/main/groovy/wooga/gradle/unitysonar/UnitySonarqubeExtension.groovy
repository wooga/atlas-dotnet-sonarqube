package wooga.gradle.unitysonar

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class UnitySonarqubeExtension implements BaseSpec {

    private final Property<String> buildDotnetVersion = objects.property(String)

    @Input
    @Optional
    Property<String> getBuildDotnetVersion() {
        return buildDotnetVersion
    }

    void setBuildDotnetVersion(String buildDotnetVersion) {
        this.buildDotnetVersion.set(buildDotnetVersion)
    }

    void setBuildDotnetVersion(Provider<String> buildDotnetVersion) {
        this.buildDotnetVersion.set(buildDotnetVersion)
    }

    private final Property<String> buildDotnetExecutable = objects.property(String)

    @Input
    @Optional
    Property<String> getBuildDotnetExecutable() {
        return buildDotnetExecutable
    }

    void setBuildDotnetExecutable(String buildDotnetExecutable) {
        this.buildDotnetExecutable.set(buildDotnetExecutable)
    }

    void setBuildDotnetExecutable(Provider<String> buildDotnetExecutable) {
        this.buildDotnetExecutable.set(buildDotnetExecutable)
    }

}
