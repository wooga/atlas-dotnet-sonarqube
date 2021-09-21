package wooga.gradle.dotnetsonar.git

import nebula.test.ProjectSpec
import org.ajoberstar.grgit.Branch
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Remote
import org.ajoberstar.grgit.service.BranchService
import org.ajoberstar.grgit.service.RemoteService
import org.gradle.api.provider.Provider
import spock.lang.Unroll
import wooga.gradle.dotnetsonar.DotNetSonarqubePlugin
import wooga.gradle.github.base.GithubPluginExtension

class RepositoryInfoFactorySpec extends ProjectSpec {

    def "creates repository info based on github extension and property"() {
        given: "configured github plugin with branch property"
        def companyName = "company"
        def repoName = "repo"
        project.ext["github.repositoryName"] = "${companyName}/${repoName}"
        and: "set git.branch property"
        def branchName = 'branchName'
        project.ext["git.branch"] = branchName

        and: "project with GrGit and GithubPlugin applied"
        project.plugins.apply(DotNetSonarqubePlugin)
        def githubPlugin = project.extensions.getByType(GithubPluginExtension)
        def grgit = project.extensions.getByType(Grgit)


        when: "creating new repository info"
        def infoProvider = RepositoryInfoFactory.fromRemoteWithLocalFallback(project, githubPlugin, grgit)

        then:
        def info = infoProvider.get()
        info.repositoryName == repoName
        info.ownerName == companyName
        info.branchName == branchName
    }

    @Unroll
    def "creates repository info with expected branch"() {
        given: "grgit installation"
        def git = mockedGrgitOnBranch(localBranch, "https://www.github.com/company/repo.git")
        and: "providers for external info"
        def repoProvider = project.provider { "company/repo" }
        def branchProvider = project.provider { branchProp }

        and: "mocked git remote"
        def gitRemote = project.provider { hasRemote ? mockedGitRemoteWithPR(repoProvider.get(), prId, remoteBranch) : null }

        when: "creating new repository info"
        def infoFactory = new RepositoryInfoFactory(project, git)
        def infoProvider = infoFactory.fromRemoteWithLocalFallback(repoProvider, branchProvider, gitRemote)

        then:
        def info = infoProvider.get()
        info.ownerName == "company"
        info.repositoryName == "repo"
        info.branchName == expBranch

        where:
        branchProp | localBranch | hasRemote | remoteBranch | prId | expBranch
        "branch"   | null        | true      | "rbranch"    | -1   | "branch"
        "branch"   | "lbranch"   | true      | "lbranch"    | -1   | "branch"
        null       | "lbranch"   | true      | "lbranch"    | -1   | "lbranch"
        null       | "PR-10"     | true      | "rbranch"    | 10   | "rbranch"
        "branch"   | null        | false     | null         | -1   | "branch"
        "branch"   | "lbranch"   | false     | null         | -1   | "branch"
        null       | "lbranch"   | false     | null         | -1   | "lbranch"
        null       | "PR-10"     | false     | null         | 10   | "PR-10"
    }

    @Unroll
    def "creates repository info with expected company and repository names"() {
        given: "grgit installation"
        def git = mockedGrgitOnBranch("branch", "https://www.github.com/${repositoryInLocalRemote}.git")
        and: "providers for external info"
        def repoProvider = project.provider { repoProp }
        def branchProvider = project.provider { "branch" }

        and: "mocked git remote"
        //repository name does not needs git remote
        def gitRemote = project.provider { null } as Provider<RemoteGit>

        when: "creating new repository info"
        def infoFactory = new RepositoryInfoFactory(project, git)
        def infoProvider = infoFactory.fromRemoteWithLocalFallback(repoProvider, branchProvider, gitRemote)

        then:
        def info = infoProvider.get()
        info.ownerName == expRepo.split("/")[0]
        info.repositoryName == expRepo.split("/")[1]
        info.branchName == "branch"

        where:
        //local remote is url of the first remote
        repoProp     | repositoryInLocalRemote | expRepo
        "pcmp/prepo" | "cmp/repo"              | "pcmp/prepo"
        "pcmp/prepo" | null                    | "pcmp/prepo"
        null         | "cmp/repo"              | "cmp/repo"
    }

    def mockedGitRemoteWithPR(String repoName, int prId, String prBranchName) {
        def remoteMock = GroovyMock(RemoteGit)
        remoteMock.repositoryName >> repoName
        remoteMock.getPRBranch(prId) >> prBranchName
        return remoteMock
    }

    def mockedGrgitOnBranch(String currentBranchName, String remoteURL = "github.com:cmp/repo") {
        def gitMock = GroovyMock(Grgit)

        def branchService = GroovyMock(BranchService)
        def branchMock = GroovyMock(Branch)
        gitMock.branch >> branchService
        branchService.current() >> branchMock
        branchMock.name >> currentBranchName

        def remoteService = GroovyMock(RemoteService)
        def remoteMock = GroovyMock(Remote)
        gitMock.remote >> remoteService
        remoteMock.url >> remoteURL
        remoteService.list() >> [remoteMock]

        return gitMock
    }


}
