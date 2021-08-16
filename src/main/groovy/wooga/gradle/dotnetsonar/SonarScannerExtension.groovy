package wooga.gradle.dotnetsonar

import org.gradle.api.Project
import org.gradle.api.Task
import org.sonarqube.gradle.ActionBroadcast
import org.sonarqube.gradle.SonarPropertyComputer
import org.sonarqube.gradle.SonarQubeProperties
import wooga.gradle.dotnetsonar.tasks.SonarScannerBegin
import wooga.gradle.dotnetsonar.tasks.SonarScannerEnd
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner

class SonarScannerExtension {

    public static final String SONARSCANNER_EXTENSION_NAME = "sonarScanner"
    public static final String MS_BUILD_TASK_NAME = "solutionMSBuild"
    public static final String DOTNET_BUILD_TASK_NAME = "solutionDotnetBuild"
    public static final String INSTALL_TASK_NAME = "sonarScannerInstall"
    public static final String BEGIN_TASK_NAME = "sonarScannerBegin"
    public static final String END_TASK_NAME = "sonarScannerEnd"

    private Project project;
    private SonarScanner sonarScanner;
    private Map<String, ?> sonarQubeProperties;
    private ActionBroadcast<SonarQubeProperties> actionBroadcast;


    SonarScannerExtension(Project project, ActionBroadcast<SonarQubeProperties> actionBroadcast) {
        this.project = project
        this.actionBroadcast = actionBroadcast
    }

    void registerBuildTask(Task... task) {
        def extProject = project
        task.each {it ->
            it.dependsOn(extProject.tasks.withType(SonarScannerBegin))
            it.finalizedBy(extProject.tasks.withType(SonarScannerEnd))
        }
    }

    Optional<SonarScanner> getSonarScanner() {
        if(sonarScanner == null) {
            this.sonarScanner = findOnPath()
        }
        return Optional.ofNullable(sonarScanner)
    }

    void setSonarScanner(SonarScanner sonarScanner) {
        this.sonarScanner = sonarScanner
    }

    Map<String, ?> getSonarQubeProperties() {
        if(sonarQubeProperties == null) {
            this.sonarQubeProperties = computeSonarProperties(project)
        }
        return sonarQubeProperties;
    }

    SonarScanner findOnPath() {
        return SonarScanner.fromPath(project, project.projectDir).orElse(null)
    }

    Map<String, ?> computeSonarProperties(Project project) {
        def actionBroadcastMap = new HashMap<String, ActionBroadcast<SonarQubeProperties>>()
        actionBroadcastMap[project.path] = actionBroadcast
        def propertyComputer = new SonarPropertyComputer(actionBroadcastMap, project)
        def properties = propertyComputer.computeSonarProperties()
        return properties.collectEntries {
            return it.value != null && it.value != ""? [it.key, it.value] : []
        }
    }
}
