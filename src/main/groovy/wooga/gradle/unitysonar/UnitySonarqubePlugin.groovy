package wooga.gradle.unitysonar

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubeProperties
import wooga.gradle.asdf.AsdfPlugin
import wooga.gradle.asdf.AsdfPluginExtension
import wooga.gradle.asdf.internal.ToolVersionInfo
import wooga.gradle.dotnetsonar.DotNetSonarqubePlugin
import wooga.gradle.dotnetsonar.SonarScannerExtension
import wooga.gradle.dotnetsonar.tasks.BuildSolution
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.unity.UnityPluginExtension

class UnitySonarqubePlugin implements Plugin<Project> {

    Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.pluginManager.apply(UnityPlugin.class)
        project.pluginManager.apply(DotNetSonarqubePlugin.class)
        project.pluginManager.apply(AsdfPlugin.class)

        def unitySonarqube = createExtension("unitySonarqube")
        //uses same dotnet version for both sonarScanner and buildTask
        def asdf = configureAsdfWithTool("dotnet", unitySonarqube.buildDotnetVersion)
        def unity = configureUnityPlugin()
        configureSonarScanner(asdf)
        configureSonarqubePlugin(unity)


        def unityTestTask = project.tasks.named(UnityPlugin.Tasks.test.toString())
        def createSolutionTask = project.tasks.named(UnityPlugin.Tasks.generateSolution.toString())

        def sonarBuild = project.tasks.register("sonarBuildUnity", BuildSolution) { it ->
            it.dependsOn(createSolutionTask)
            it.mustRunAfter(unityTestTask)

            def buildDotnetDefault = unitySonarqube.buildDotnetExecutable.orElse(asdf.getTool("dotnet").getExecutable("dotnet"))
            it.executableName.convention(buildDotnetDefault)
            it.solution.convention(unity.projectDirectory.file("${project.name}.sln"))
            it.environment.put("FrameworkPathOverride", unity.monoFrameworkDir.map { it.asFile.absolutePath })

            def propsFix = createSolutionPropertyFixFile("/unity-sonarqube.project-fixes.props")
            it.additionalArguments.add("/p:CustomBeforeMicrosoftCommonProps=${propsFix.absolutePath}")
        }

        namedOrRegister("sonarqube", DefaultTask) { task ->
            task.dependsOn(unityTestTask, sonarBuild)
        }
    }

    UnitySonarqubeExtension createExtension(String extensionName) {
        def unitySonarqube = project.extensions.create(extensionName, UnitySonarqubeExtension)
        unitySonarqube.buildDotnetVersion.convention(UnitySonarqubePluginConventions.buildDotnetVersion.getStringValueProvider(project))
        return unitySonarqube
    }

    AsdfPluginExtension configureAsdfWithTool(String name, Provider<String> version) {
        def asdf = project.extensions.getByType(AsdfPluginExtension)
        asdf.tool(new ToolVersionInfo(name, version))
        return asdf
    }

    UnityPluginExtension configureUnityPlugin() {
        def unityExt = project.extensions.findByType(UnityPluginExtension)
        unityExt.enableTestCodeCoverage = true
        return unityExt
    }

    SonarScannerExtension configureSonarScanner(AsdfPluginExtension asdf) {
        def sonarScanner = project.extensions.findByType(SonarScannerExtension)

        def asdfDotnet = asdf.getTool("dotnet")
        sonarScanner.dotnetExecutable.convention(asdfDotnet.getExecutable("dotnet"))
        return sonarScanner
    }

    SonarQubeExtension configureSonarqubePlugin(UnityPluginExtension unity) {
        def sonarExt = project.extensions.findByType(SonarQubeExtension)
        sonarExt.properties({
            def assetsDir = unity.assetsDir.get().asFile
            def reportsDir = unity.reportsDir.get().asFile
            def relativeAssetsDir = project.projectDir.relativePath(assetsDir)
            addPropertyIfNotExists(it, "sonar.cpd.exclusions", "${relativeAssetsDir}/**/Tests/**")
            addPropertyIfNotExists(it, "sonar.coverage.exclusions", "${relativeAssetsDir}/**/Tests/**")
            addPropertyIfNotExists(it, "sonar.exclusions", "${relativeAssetsDir}/Paket.Unity3D/**")
            addPropertyIfNotExists(it, "sonar.cs.nunit.reportsPaths", "${reportsDir.path}/**/*.xml")
            addPropertyIfNotExists(it, "sonar.cs.opencover.reportsPaths", "${reportsDir.path}/**/*.xml")
        })
        return sonarExt
    }

    protected static void addPropertyIfNotExists(SonarQubeProperties properties, String key, Object value) {
        if (!properties.properties.containsKey(key)) {
            properties.property(key, value)
        }
    }

    protected static File createSolutionPropertyFixFile(String resourceFileName) {
        def propsFixResource = UnitySonarqubePlugin.class.getResourceAsStream(resourceFileName)
        def propsFixTmpFile = File.createTempFile("unity-sonarqube-", ".project-fixes.props")
        propsFixTmpFile.text = propsFixResource.text
        return propsFixTmpFile
    }

    protected <T extends Task> TaskProvider<T> namedOrRegister(String taskName, Class<T> type = DefaultTask.class, Action<T> configuration = { it -> }) {
        try {
            return project.tasks.named(taskName, type, configuration)
        } catch (UnknownTaskException ignore) {
            return project.tasks.register(taskName, type, configuration)
        }
    }


}