package wooga.gradle.dotnetsonar.git


import org.ajoberstar.grgit.Remote
import org.gradle.api.provider.Provider
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import wooga.gradle.github.base.GithubPluginExtension

class GithubRemoteFactory {
    static final String DOMAIN = "github.com"

    GitCredentials githubCreds

    GithubRemoteFactory(GitCredentials githubCreds) {
        this.githubCreds = githubCreds
    }

    GithubRemote fromRepositoryName(String repoFullName) {
        def ghClient = githubCreds.createGithubClient()
        def ghRepo = ghClient.getRepository(repoFullName)
        return new GithubRemote(ghClient, ghRepo)
    }

    static String repositoryNameFromGitRemote(Remote remote) {
        def remoteURL = remote.url
        def domainIndex = remoteURL.indexOf(DOMAIN)
        def urlAfterDomain = remoteURL.substring(domainIndex + DOMAIN.length() + 1)
        return urlAfterDomain.replace(".git", "")
    }
}

class GithubRemote implements RemoteGit {

    GitHub client
    GHRepository repository

    static Provider<GithubRemote> providerFromExtension(GithubPluginExtension githubPluginExtension) {
        def githubCreds = GitCredentials.fromGithubExtension(githubPluginExtension)
        return githubCreds.map {creds ->
            if(githubPluginExtension.repositoryName.present && creds.exists()) {
                def remoteFactory = new GithubRemoteFactory(creds)
                return remoteFactory.fromRepositoryName(githubPluginExtension.repositoryName.get())
            }
            return null
        }
    }

    GithubRemote(GitHub client, GHRepository repository) {
        this.client = client
        this.repository = repository
    }

    @Override
    String getPRBranch(int prId) {
        return repository.getPullRequest(prId).head.ref
    }

    @Override
    String getRepositoryName() {
        return repository.fullName
    }
}
