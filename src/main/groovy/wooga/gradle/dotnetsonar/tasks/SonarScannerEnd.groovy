package wooga.gradle.dotnetsonar.tasks


import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import wooga.gradle.dotnetsonar.SonarScannerExtension
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner

class SonarScannerEnd extends DefaultTask {

    private final Property<SonarScanner> sonarScanner;
    private final Property<String> loginToken;

    SonarScannerEnd() {
        this.sonarScanner = project.objects.property(SonarScanner)
        this.loginToken = project.objects.property(String)
    }

    @TaskAction
    def run() {
        def sonarScanner = sonarScanner.get()
        def token = loginToken.get()
        sonarScanner.end(token)
    }

    @Input
    Property<SonarScanner> getSonarScanner() {
        return sonarScanner
    }

    @Input
    Property<String> getLoginToken() {
        return loginToken
    }

    void setLoginToken(String token) {
        this.loginToken.set(token)
    }

    void setSonarScanner(SonarScanner sonarScanner) {
        this.sonarScanner.set(sonarScanner)
    }
}
