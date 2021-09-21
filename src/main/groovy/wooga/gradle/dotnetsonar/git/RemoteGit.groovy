package wooga.gradle.dotnetsonar.git

interface RemoteGit {

    String getPRBranch(int prId)

    String getRepositoryName()

}