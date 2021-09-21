package wooga.gradle.dotnetsonar.git

class RepositoryInfo {
    final String ownerName
    final String repositoryName
    final String branchName

    public RepositoryInfo(String ownerName, String repositoryName, String branchName) {
        this.ownerName = ownerName
        this.repositoryName = repositoryName
        this.branchName = branchName
    }

}
