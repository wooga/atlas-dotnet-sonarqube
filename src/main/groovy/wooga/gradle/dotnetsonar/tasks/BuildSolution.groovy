package wooga.gradle.dotnetsonar.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import wooga.gradle.dotnetsonar.tasks.internal.DotNet
import wooga.gradle.dotnetsonar.tasks.internal.MSBuild
import wooga.gradle.dotnetsonar.tasks.internal.OSOps
import wooga.gradle.dotnetsonar.tasks.internal.SolutionBuildTool

class BuildSolution extends DefaultTask {

    static void configureDefaultMSBuild(BuildSolution buildTask) {
        def projectDir = buildTask.project.layout.projectDirectory
        buildTask.with {
            OSOps.findInOSPath(buildTask.project, "MSBuild.exe", "msbuild").ifPresent {msbuildExec ->
                msBuildExecutable.convention(projectDir.file(msbuildExec.absolutePath))
            }
            solution.convention(projectDir.file("${project.name}.sln"))
        }
    }

    static void configureDefaultDotNetBuild(BuildSolution buildTask) {
        def projectDir = buildTask.project.layout.projectDirectory
        buildTask.with {
            OSOps.findInOSPath(buildTask.project, "dotnet").ifPresent {dotnetExec ->
                dotnetExecutable.convention(projectDir.file(dotnetExec.absolutePath))
            }
            solution.convention(projectDir.file("${project.name}.sln"))
        }
    }

    private Property<SolutionBuildTool> buildTool
    private RegularFileProperty msBuildExecutable
    private RegularFileProperty dotnetExecutable
    private RegularFileProperty solution
    private MapProperty<String, ?> environment

    BuildSolution() {
        this.buildTool = project.objects.property(SolutionBuildTool)
        this.msBuildExecutable = project.objects.fileProperty()
        this.dotnetExecutable = project.objects.fileProperty()
        this.solution = project.objects.fileProperty()
        this.environment = project.objects.mapProperty(String, Object)
    }

    @TaskAction
    void run() {
        def resolvedBuildTool = buildTool.
                orElse(dotnetExecutable.map{DotNet.gradleBased(getProject(), it.asFile)}).
                orElse(msBuildExecutable.map{MSBuild.gradleBased(getProject(), it.asFile)}).
                get()
        resolvedBuildTool.buildSolution(solution.get().asFile, environment.get())
    }

    @Input
    RegularFileProperty getSolution() {
        return solution
    }

    @Optional @Input
    MapProperty<String, ?> getEnvironment() {
        return environment
    }

    @Input @Optional
    Property<SolutionBuildTool> getBuildTool() {
        return buildTool
    }

    @Input @Optional
    RegularFileProperty getDotnetExecutable() {
        return dotnetExecutable
    }

    @Input @Optional
    RegularFileProperty getMsBuildExecutable() {
        return msBuildExecutable
    }

    void setSolution(File solution) {
        this.solution.set(solution)
    }

    void setSolution(String solutionPath) {
        this.setSolution(project.file(solutionPath))
    }

    void setEnvironment(Map<String, ?> environment) {
        this.environment.set(environment)
    }

    void addEnvironment(String key, Object value) {
        this.environment.put(key, value)
    }

    void setBuildTool(SolutionBuildTool buildTool) {
        this.buildTool.set(buildTool)
    }

    void setDotnetExecutable(File file) {
        dotnetExecutable.set(file)
    }

    void setDotnetExecutable(String filePath) {
        setDotnetExecutable(new File(filePath))
    }

    void setMsBuildExecutable(File file) {
        msBuildExecutable.set(file)
    }

    void setMsBuildExecutable(String filePath) {
        setMsBuildExecutable(new File(filePath))
    }
}
