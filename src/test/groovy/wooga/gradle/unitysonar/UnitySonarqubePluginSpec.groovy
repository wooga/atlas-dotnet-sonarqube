package wooga.gradle.unitysonar


import nebula.test.ProjectSpec
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import spock.lang.Unroll
import wooga.gradle.dotnetsonar.SonarScannerExtension
import wooga.gradle.dotnetsonar.tasks.BuildSolution
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.unity.UnityPluginExtension

import static com.wooga.gradle.PlatformUtils.windows

class UnitySonarqubePluginSpec extends ProjectSpec {

    public static final String PLUGIN_NAME = 'net.wooga.unity-sonarqube'

    @Unroll("creates the task #taskName")
    def 'Creates needed tasks'(String taskName, Class taskType) {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.tasks.findByName(taskName)

        when:
        project.plugins.apply(PLUGIN_NAME)
        Task task
        project.afterEvaluate {
            task = project.tasks.findByName(taskName)
        }

        then:
        project.evaluate()
        taskType.isInstance(task)

        where:
        taskName          | taskType
        "sonarqube"       | DefaultTask
        "sonarBuildUnity" | BuildSolution
    }

    @Unroll("task #taskName has runtime dependencies")
    def 'Task has runtime dependencies'(String taskName, String[] dependencies) {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.tasks.findByName(taskName)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        Task task = project.tasks.findByName(taskName)
        dependencies.each {expectedDepName ->
            def taskDeps = task.getTaskDependencies().getDependencies(task)
            def actualDepName = taskDeps.find {it.name == expectedDepName }?.name
            assert expectedDepName == actualDepName
        }
        task.getFinalizedBy().getDependencies(task).collect{it.name } == finalizedBy

        where:
        taskName          | dependencies                                                    | finalizedBy
        "sonarqube"       | [UnityPlugin.Tasks.test.toString(), "sonarBuildUnity"]          | []
        "sonarBuildUnity" | [isWindows()? "dotnetWindowsInstall" : "asdfBinstubsDotnet", "generateSolution", "sonarScannerBegin"] | ["sonarScannerEnd"]

    }

    def "configures sonarqube extension"() {
        given: "project without plugin applied"
        assert !project.plugins.hasPlugin(PLUGIN_NAME)

        when: "applying atlas-build-unity plugin"
        project.plugins.apply(PLUGIN_NAME)
        project.evaluate()

        then:
        def sonarExt = project.extensions.getByType(SonarScannerExtension)
        def unityExt = project.extensions.getByType(UnityPluginExtension)
        and: "sonarqube extension is configured with defaults"
        def properties = sonarExt.sonarQubeProperties.get()
        def reportsDir = unityExt.reportsDir.get().asFile.path
        properties["sonar.exclusions"] == "Assets/Paket.Unity3D/**"
        properties["sonar.cpd.exclusions"] == "Assets/**/Tests/**"
        properties["sonar.coverage.exclusions"] == "Assets/**/Tests/**"
        properties["sonar.cs.nunit.reportsPaths"] == "${reportsDir}/**/*.xml"
        properties["sonar.cs.opencover.reportsPaths"] == "${reportsDir}/**/*.xml"
    }

    def "configures sonarBuildUnity task"() {
        given: "project without plugin applied"
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        and: "props file with fixes to run unity project on msbuild properly"

        when: "applying atlas-build-unity plugin"
        project.plugins.apply(PLUGIN_NAME)

        then:
        def unityExt = project.extensions.getByType(UnityPluginExtension)
        def buildTask = project.tasks.getByName("sonarBuildUnity") as BuildSolution

        //Executable depends on asdf binstubs task being executed. Its tested on UnitySonarqubePluginIntegrationSpec.
        buildTask.solution.get().asFile == new File(projectDir, "${project.name}.sln")
        buildTask.environment.getting("FrameworkPathOverride").getOrElse(null) ==
                unityExt.monoFrameworkDir.map { it.asFile.absolutePath }.getOrElse(null)
        buildTask.arguments.get().any {
            it.startsWith("/p:CustomBeforeMicrosoftCommonProps=") && it.endsWith(".project-fixes.props")
        }
    }
}
