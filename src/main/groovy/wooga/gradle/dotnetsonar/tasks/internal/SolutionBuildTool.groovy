package wooga.gradle.dotnetsonar.tasks.internal

interface SolutionBuildTool {

    public void buildSolution(File solution);
    public void buildSolution(File solution, Map<String, ?> environment);
    public void buildSolution(File solution, Map<String, ?> environment, List<?> extraArgs);

}