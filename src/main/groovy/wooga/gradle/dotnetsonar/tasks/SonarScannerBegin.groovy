package wooga.gradle.dotnetsonar.tasks


import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.sonarqube.gradle.ActionBroadcast
import org.sonarqube.gradle.SonarQubeProperties
import wooga.gradle.dotnetsonar.SonarScannerExtension

class SonarScannerBegin extends DefaultTask {

    static final void defaultSonarProperties(Project project, SonarQubeProperties properties) {
        properties.with {
            property("sonar.login", System.getenv('SONAR_TOKEN'))
            property("sonar.host.url", System.getenv('SONAR_HOST'))
            //would be better if this was associated to github repository, see atlas-plugins
            property("sonar.projectKey", project.rootProject.name)
            property("sonar.projectName", project.rootProject.name)
            //property("sonar.sources", ".")
            if(project.version != null) {
                property("sonar.version", project.version.toString())
            }
        }
    }

    static final void unityDefaultProperties(Project project, SonarQubeProperties properties) {
        defaultSonarProperties(project, properties)
        properties.with {
            property("sonar.login", System.getenv('SONAR_TOKEN'))
            property("sonar.host.url", System.getenv('SONAR_HOST'))
            property("sonar.exclusions", "Assets/Paket.Unity3D/**")
            property("sonar.cpd.exclusions", "Assets/Tests/**")
            property("sonar.coverage.exclusions", "Assets/Tests/**")
            property("sonar.cs.nunit.reportsPaths", "build/reports/unity/*/*.xml")
            property("sonar.cs.opencover.reportsPaths", "build/codeCoverage/**/*.xml")
        }
    }

    @TaskAction
    def run() {
        def sonarScannerExt = project.extensions.getByType(SonarScannerExtension)
        def sonarQubeProperties = sonarScannerExt.sonarQubeProperties
        def sonarScanner = sonarScannerExt.sonarScanner.orElseThrow {
            throw new IllegalStateException("couldn't find SonarScanner on Gradle nor its executable on PATH")
        }
        assertKeyOnMap(sonarQubeProperties, "sonar.projectKey")
        assertKeyOnMap(sonarQubeProperties, "sonar.version")
        sonarScanner.begin(
                sonarQubeProperties["sonar.projectKey"].toString(),
                sonarQubeProperties["sonar.projectName"].toString(),
                sonarQubeProperties["sonar.version"]?.toString(),
                sonarQubeProperties)
    }

    private static <T> void assertKeyOnMap(Map<T,?> map, T key) {
        if(!map.containsKey(key)) {
            throw new IllegalArgumentException("SonarqubeBegin needs a set ${key} property on the sonarqube extension")
        }
    }

    void setActionBroadcast(ActionBroadcast<SonarQubeProperties> actionBroadcast) {
        this.actionBroadcast = actionBroadcast
    }
}
