package wooga.gradle.dotnetsonar

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UncheckedIOException
import org.gradle.api.provider.Provider
import org.sonarqube.gradle.ActionBroadcast
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubeProperties
import wooga.gradle.dotnetsonar.tasks.BuildSolution
import wooga.gradle.dotnetsonar.tasks.SonarScannerBegin
import wooga.gradle.dotnetsonar.tasks.SonarScannerEnd
import wooga.gradle.github.base.GithubBasePlugin
import wooga.gradle.github.base.GithubPluginExtension
import wooga.gradle.github.base.internal.DefaultGithubPluginExtension

import static SonarScannerExtension.*

class DotNetSonarqubePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply(GithubBasePlugin)
        def githubExt = project.extensions.findByType(DefaultGithubPluginExtension)

        def actionBroadcast = new ActionBroadcast<SonarQubeProperties>()
        def sonarScannerExt = project.extensions.create(SONARSCANNER_EXTENSION_NAME, SonarScannerExtension, project, actionBroadcast)
        def sonarQubeExt = project.extensions.create(SonarQubeExtension.SONARQUBE_EXTENSION_NAME, SonarQubeExtension, actionBroadcast)
        sonarQubeExt.properties { it ->
            defaultSonarProperties(project, githubExt, it)
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

    static final void defaultSonarProperties(Project project, GithubPluginExtension githubExt,
                                                              SonarQubeProperties properties) {
        def companyNameProvider = githubExt.repositoryName.map{String fullRepoName -> fullRepoName.split("/")[0]}
        def repoNameProvider = githubExt.repositoryName.map{String fullRepoName -> fullRepoName.split("/")[1]}
        def keyProvider = companyNameProvider.map { comp ->
            return repoNameProvider.map {repoName -> "${comp}_${repoName}"}.getOrNull()
        }
        def branchProvider = localBranchProviderWithPR(project, githubExt).map { it.trim().isEmpty() ? null : it }
        properties.with {
            property("sonar.login", System.getenv('SONAR_TOKEN'))
            property("sonar.host.url", System.getenv('SONAR_HOST'))
            property("sonar.projectKey", keyProvider.getOrNull())
            property("sonar.projectName", repoNameProvider.getOrNull())
            property("sonar.branch.name", branchProvider.getOrNull())
            if(project.version != null) {
                property("sonar.version", project.version.toString())
            }
        }
    }

    static Provider<String> localBranchProviderWithPR(Project project, GithubPluginExtension githubExt) {
        def clientProvider = emptyProviderForException(project, githubExt.clientProvider, UncheckedIOException)

        return githubExt.branchName.map {currentBranch ->
            return clientProvider.map {client ->
                def repository = client.getRepository(githubExt.repositoryName.get())
                if (currentBranch.toUpperCase().startsWith("PR-")) {
                    def maybePrNumber = currentBranch.replace("PR-", "").trim()
                    if (maybePrNumber.isNumber()) {
                        def prNumber = Integer.valueOf(maybePrNumber)
                        return repository.getPullRequest(prNumber).head.ref
                    }
                    return null
                }
            }.getOrElse(currentBranch)
        }
    }

    protected static <T> Provider<T> emptyProviderForException(Project project,
                                                               Provider<T> provider,
                                                               Class<? extends Throwable> exceptionClass) {
        return project.provider {
            try {
                return provider.get()
            }catch(Throwable e) {
                if(exceptionClass.isInstance(e)) {
                    return null
                }
                throw e
            }
        }
    }
}
