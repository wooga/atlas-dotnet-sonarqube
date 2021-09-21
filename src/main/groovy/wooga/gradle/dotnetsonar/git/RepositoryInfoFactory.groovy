package wooga.gradle.dotnetsonar.git

import org.ajoberstar.grgit.Branch
import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import wooga.gradle.github.base.GithubPluginExtension

class RepositoryInfoFactory {


    static Provider<RepositoryInfo> fromRemoteWithLocalFallback(Project project,
                                                                GithubPluginExtension githubExt, Grgit grgit) {
        def repoInfoFactory = new RepositoryInfoFactory(project, grgit)
        def branchPropProvider = project.provider {
            project.hasProperty("git.branch")?
                    project.property("git.branch").toString() : null
        }
        def ghRemote = GithubRemote.providerFromExtension(githubExt)
        project.afterEvaluate {
            if(!ghRemote.present) {
                project.logger.warn("Couldn't create a Github client, " +
                        "please verify if the github extension is configurated, " +
                        "and if is there any credentials set for github conenction ")
            }
        }
        return repoInfoFactory.fromRemoteWithLocalFallback(githubExt.repositoryName, branchPropProvider, ghRemote)
    }

    final Project project
    final Grgit git

    RepositoryInfoFactory(Project project, Grgit git) {
        this.project = project
        this.git = git
    }

    Provider<RepositoryInfo> fromRemoteWithLocalFallback(Provider<String> repoNameProvider,
                                                         Provider<String> branchProvider,
                                                         Provider<RemoteGit> gitRemote) {
        def currentBranch = git.branch.current()
        def localRepoName = project.provider {
            git.remote.list().find {it.name == "origin"}?: null
        }.map{
            GithubRemoteFactory.repositoryNameFromGitRemote(it)
        }
        def repositoryName = repoNameProvider.orElse(localRepoName)
        def branchName = branchProvider.
                orElse(gitRemote.map {resolveRemoteBranchName(currentBranch, it) }).
                orElse(currentBranch.name)
        return repositoryName.map {repoName ->
            fromFullRepositoryName(repoName, branchName.get())
        }
    }

    static RepositoryInfo fromFullRepositoryName(String fullRepoName, String branchName) {
        String[] nameParts = fullRepoName.split("/")
        def companyName = nameParts[0]
        def repoName = nameParts[1]
        return new RepositoryInfo(companyName, repoName, branchName)
    }

    private static String resolveRemoteBranchName(Branch currentBranch, RemoteGit remoteGit) {
        if (remoteGit != null && currentBranch.name.toUpperCase().startsWith("PR-")) {
            return resolveBranchFromPR(currentBranch, remoteGit)
        }
        return null
    }

    private static String resolveBranchFromPR(Branch currentBranch, RemoteGit remoteGit) {
        def maybePrNumber = currentBranch.name.replace("PR-", "").trim()
        if (maybePrNumber.isNumber()) {
            def prNumber = Integer.valueOf(maybePrNumber)
            return remoteGit.getPRBranch(prNumber)
        }
        return null
    }
}
