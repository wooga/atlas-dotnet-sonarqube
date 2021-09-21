package wooga.gradle.dotnetsonar

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.gradle.GrgitPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.sonarqube.gradle.ActionBroadcast
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubeProperties
import wooga.gradle.dotnetsonar.git.RepositoryInfo
import wooga.gradle.dotnetsonar.git.RepositoryInfoFactory
import wooga.gradle.dotnetsonar.tasks.BuildSolution
import wooga.gradle.dotnetsonar.tasks.SonarScannerBegin
import wooga.gradle.dotnetsonar.tasks.SonarScannerEnd
import wooga.gradle.github.GithubPlugin
import wooga.gradle.github.base.GithubPluginExtension

import static SonarScannerExtension.*

class DotNetSonarqubePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply(GithubPlugin)
        project.plugins.apply(GrgitPlugin)

        def githubExt = project.extensions.getByType(GithubPluginExtension)
        def grgit = project.extensions.getByType(Grgit)
        def repoInfo = RepositoryInfoFactory.fromRemoteWithLocalFallback(project, githubExt, grgit)

        def actionBroadcast = new ActionBroadcast<SonarQubeProperties>()
        def sonarScannerExt = project.extensions.create(SONARSCANNER_EXTENSION_NAME, SonarScannerExtension, project, actionBroadcast)
        def sonarQubeExt = project.extensions.create(SonarQubeExtension.SONARQUBE_EXTENSION_NAME, SonarQubeExtension, actionBroadcast)
        sonarQubeExt.properties { it ->
            defaultSonarProperties(project, repoInfo.get(), it)
        }

        project.tasks.register(MS_BUILD_TASK_NAME, BuildSolution).configure { buildTask ->
            configureDefaultMSBuild(buildTask)
        }

        project.tasks.register(DOTNET_BUILD_TASK_NAME, BuildSolution).configure { buildTask ->
            configureDefaultDotNetBuild(buildTask)
        }

        def beginTask = project.tasks.register(BEGIN_TASK_NAME, SonarScannerBegin) {beginTask ->
            beginTask.sonarScanner.convention(sonarScannerExt.sonarScanner)
            beginTask.sonarqubeProperties.convention(sonarScannerExt.sonarQubeProperties)
        }
        project.tasks.register(END_TASK_NAME, SonarScannerEnd) { endTask ->
            def tokenProvider = sonarScannerExt.sonarQubeProperties.map{it["sonar.login"].toString()}
            endTask.sonarScanner.convention(sonarScannerExt.sonarScanner)
            endTask.loginToken.convention(tokenProvider)
            endTask.dependsOn(beginTask)
        }

        project.tasks.withType(BuildSolution).configureEach {
            sonarScannerExt.registerBuildTask(it)
        }
    }

    static final void defaultSonarProperties(Project project,
                                             RepositoryInfo repoInfo,
                                             SonarQubeProperties properties) {
        properties.with {
            property("sonar.login", System.getenv('SONAR_TOKEN'))
            property("sonar.host.url", System.getenv('SONAR_HOST'))
            //would be better if this was associated to github repository, see atlas-plugins
            property("sonar.branch.name", repoInfo.branchName)
            property("sonar.projectKey", "${repoInfo.ownerName}_${repoInfo.repositoryName}")
            property("sonar.projectName", repoInfo.repositoryName)
            if(project.version != null) {
                property("sonar.version", project.version.toString())
            }
        }
    }
}
