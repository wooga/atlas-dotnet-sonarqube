package wooga.gradle.dotnetsonar.git


import org.gradle.api.provider.Provider
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import wooga.gradle.github.base.GithubPluginExtension

class GitCredentials {

    final String username
    final String password
    final String token

    static Provider<GitCredentials> fromGithubExtension(GithubPluginExtension ext) {
        //a non-empty provider if any credentials are available
        Provider hasCredsProvider = ext.username
                                        .orElse(ext.password)
                                        .orElse(ext.token).orElse("")

        return hasCredsProvider.map {
            if(it == "" && !hasExternalCredentials()) {
                return null
            }
            return new GitCredentials(ext.username.getOrNull(), ext.password.getOrNull(), ext.token.getOrNull())
        }
    }

    GitCredentials(String username, String password, String token) {
        this.username = username
        this.password = password
        this.token = token
    }

    static boolean hasExternalCredentials() {
        try {
            GitHubBuilder.fromCredentials()
            return true
        } catch(IOException _) {
            return false
        }
    }

    boolean exists() {
        boolean innerCreds = (username && password) || (username && token) || token
        return innerCreds || hasExternalCredentials()
    }

    GitHub createGithubClient() {
        def builder = new GitHubBuilder()

        if (getUsername() && getPassword()) {
            builder = builder.withPassword(username, password)
        } else if (getUsername() && getToken()) {
            builder = builder.withOAuthToken(token, username)

        } else if (getToken()) {
            builder = builder.withOAuthToken(token)

        } else {
            builder = GitHubBuilder.fromCredentials()
        }
        return builder.build()
    }
}
