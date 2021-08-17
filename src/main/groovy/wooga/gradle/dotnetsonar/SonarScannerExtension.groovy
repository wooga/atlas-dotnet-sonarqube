package wooga.gradle.dotnetsonar

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.sonarqube.gradle.ActionBroadcast
import org.sonarqube.gradle.SonarPropertyComputer
import org.sonarqube.gradle.SonarQubeProperties
import wooga.gradle.dotnetsonar.tasks.SonarScannerBegin
import wooga.gradle.dotnetsonar.tasks.SonarScannerEnd
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner
import wooga.gradle.dotnetsonar.tasks.internal.SonarScannerFactory

class SonarScannerExtension {

    public static final String SONARSCANNER_EXTENSION_NAME = "sonarScanner"
    public static final String MS_BUILD_TASK_NAME = "solutionMSBuild"
    public static final String DOTNET_BUILD_TASK_NAME = "solutionDotnetBuild"
    public static final String INSTALL_TASK_NAME = "sonarScannerInstall"
    public static final String BEGIN_TASK_NAME = "sonarScannerBegin"
    public static final String END_TASK_NAME = "sonarScannerEnd"

    private final Project project;
    private final Property<SonarScanner> sonarScanner
    private final RegularFileProperty monoExecutable
    private Map<String, ?> sonarQubeProperties
    private ActionBroadcast<SonarQubeProperties> actionBroadcast;
    private SonarScannerFactory scannerFactory;

    SonarScannerExtension(Project project, ActionBroadcast<SonarQubeProperties> actionBroadcast) {
        this.sonarScanner = project.objects.property(SonarScanner)
        this.monoExecutable = project.objects.fileProperty()
        this.project = project
        this.actionBroadcast = actionBroadcast
        this.scannerFactory = SonarScannerFactory.withPATHFallback(project, monoExecutable.map{it.asFile})
    }

    void registerBuildTask(Task... task) {
        def extProject = project
        task.each {it ->
            it.dependsOn(extProject.tasks.withType(SonarScannerBegin))
            it.finalizedBy(extProject.tasks.withType(SonarScannerEnd))
        }
    }

    SonarScanner createSonarScanner(File sonarScannerExec) {
        return scannerFactory.fromExecutable(sonarScannerExec, project.projectDir)
    }

    Provider<SonarScanner> getSonarScanner() {

        def factory = this.scannerFactory //groovy "this" can be as dumb as JS one sometimes.
        def proj = this.project
        return sonarScanner.orElse(project.provider {
            factory.fromPath(proj.projectDir).orElse(null)
        })
    }

    void setSonarScanner(SonarScanner sonarScanner) {
        this.sonarScanner.set(sonarScanner)
    }

    void setSonarScanner(Provider<SonarScanner> sonarScanner) {
        this.sonarScanner.set(sonarScanner)
    }

    RegularFileProperty getMonoExecutable() {
        return monoExecutable
    }

    void setMonoExecutable(File monoExecutable) {
        this.monoExecutable.set(monoExecutable)
    }

    Map<String, ?> getSonarQubeProperties() {
        if(sonarQubeProperties == null) {
            this.sonarQubeProperties = computeSonarProperties(project)
        }
        return sonarQubeProperties;
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
