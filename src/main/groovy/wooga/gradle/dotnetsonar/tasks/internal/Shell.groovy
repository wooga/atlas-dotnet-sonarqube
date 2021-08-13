package wooga.gradle.dotnetsonar.tasks.internal


interface Shell {

    ShellResult execute(Closure execSpecClosure)
    ShellResult execute(boolean logging, Closure execSpecClosure)
}