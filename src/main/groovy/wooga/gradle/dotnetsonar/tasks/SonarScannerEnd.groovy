package wooga.gradle.dotnetsonar.tasks


import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import wooga.gradle.dotnetsonar.SonarScannerExtension

class SonarScannerEnd extends DefaultTask {

    @Input @Optional
    private Property<String> loginToken;

    SonarScannerEnd() {
        this.loginToken = project.objects.property(String)
    }

    @TaskAction
    def run() {
        def sonarScannerExt = project.extensions.getByType(SonarScannerExtension)
        def sonarScanner = sonarScannerExt.sonarScanner.orElseThrow {
            throw new IllegalStateException("couldn't find SonarScanner on Gradle nor its executable on PATH")
        }

        def token = loginToken.orElse(sonarScannerExt.sonarQubeProperties["sonar.login"].toString()).get()
        sonarScanner.end(token)
    }

    Property<String> getLoginToken() {
        return loginToken
    }

    void setLoginToken(String token) {
        this.loginToken.set(token)
    }
}
