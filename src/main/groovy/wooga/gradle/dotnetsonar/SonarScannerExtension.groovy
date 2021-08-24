package wooga.gradle.dotnetsonar

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
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
    public static final String BEGIN_TASK_NAME = "sonarScannerBegin"
    public static final String END_TASK_NAME = "sonarScannerEnd"

    private final Project project;
    private final SonarScannerFactory scannerFactory;
    private final ActionBroadcast<SonarQubeProperties> actionBroadcast;

    private final SonarScannerInstallInfo installInfo
    private final RegularFileProperty sonarScannerExecutable
    private final RegularFileProperty monoExecutable
    private final MapProperty<String, ?> sonarQubeProperties
    private final Provider<SonarScanner> sonarScanner;

    SonarScannerExtension(Project project, ActionBroadcast<SonarQubeProperties> actionBroadcast) {
        this.installInfo = new SonarScannerInstallInfo(project)
        this.monoExecutable = project.objects.fileProperty()
        this.sonarScannerExecutable = project.objects.fileProperty()
        this.sonarQubeProperties = project.objects.mapProperty(String, Object)
        sonarQubeProperties.convention(project.provider{
            this.computeSonarProperties(project)
        })

        this.project = project
        this.actionBroadcast = actionBroadcast
        this.scannerFactory = SonarScannerFactory.withPathFallback(project, monoExecutable.map{it.asFile})
        this.sonarScanner = createSonarScannerProvider(project, scannerFactory)
    }

    /**
     * Registers tasks as sonar scanner build tasks, meaning that these tasks will be executed in between
     * SonarScannerBegin and SonarScannerEnd tasks
     * @param task tasks to be registered
     */
    void registerBuildTask(Task... task) {
        def extProject = project
        task.each {it ->
            it.dependsOn(extProject.tasks.withType(SonarScannerBegin))
            it.finalizedBy(extProject.tasks.withType(SonarScannerEnd))
        }
    }
    /**
     * Convenience method for filling up sonarscanner installation info. Only used if sonar scanner is downloaded from remote.
     * @param installInfoOps closure operating over install info object
     */
    void installInfo(Closure installInfoOps) {
        def opsDuplicate = installInfoOps.clone() as Closure
        opsDuplicate.delegate = this.installInfo
        opsDuplicate.setResolveStrategy(Closure.DELEGATE_FIRST)
        opsDuplicate(this.installInfo)
    }

    Provider<SonarScanner> getSonarScanner() {
        return sonarScanner
    }

    Map<String, Object> computeSonarProperties(Project project) {
        def actionBroadcastMap = new HashMap<String, ActionBroadcast<SonarQubeProperties>>()
        actionBroadcastMap[project.path] = actionBroadcast
        def propertyComputer = new SonarPropertyComputer(actionBroadcastMap, project)
        def properties = propertyComputer.computeSonarProperties()
        return properties.collectEntries {
            return it.value != null && it.value != ""? [it.key, it.value] : []
        }.findAll {it.key != null && it.value != null }
    }

    Provider<SonarScanner> createSonarScannerProvider(Project project, SonarScannerFactory factory) {
        def workingDir = project.projectDir

        def scannerFromFile = sonarScannerExecutable.map( { RegularFile scannerExec ->
            factory.fromExecutable(scannerExec.asFile, workingDir)
        }.memoize())
        def scannerFromPath = project.provider({ factory.fromPath(workingDir).orElse(null) }.memoize())
        def scannerFromRemote = installInfo.provideScannerFromRemote(project, factory, workingDir)
        return scannerFromFile.orElse(scannerFromPath).orElse(scannerFromRemote)
    }

    RegularFileProperty getMonoExecutable() {
        return monoExecutable
    }

    RegularFileProperty getSonarScannerExecutable() {
        return sonarScannerExecutable
    }

    MapProperty<String, ?> getSonarQubeProperties() {
        return sonarQubeProperties;
    }

    void setMonoExecutable(RegularFileProperty monoExecutable) {
        this.monoExecutable.set(monoExecutable)
    }

    void setMonoExecutable(File monoExecutable) {
        this.monoExecutable.set(monoExecutable)
    }

    void setSonarScannerExecutable(File sonarScannerExecutable) {
        this.sonarScannerExecutable.set(sonarScannerExecutable)
    }

    void setSonarScannerExecutable(RegularFileProperty sonarScannerExecutable) {
        this.sonarScannerExecutable.set(sonarScannerExecutable)
    }


}
