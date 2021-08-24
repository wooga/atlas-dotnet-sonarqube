package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project
import org.gradle.api.provider.Provider

class SonarScannerFactory {

    private Project project;
    private Provider<? extends File> monoExecutable;

    public static SonarScannerFactory withPATHFallback(Project project, Provider<? extends File> monoExecutable) {
        def monoProvider = monoExecutable.orElse(project.provider {
            OSOps.findInOSPath(project, "mono").orElseThrow {
                 return new FileNotFoundException
                        ("Could not find 'mono' executable in OS PATH nor in SonarScannerExtension properties")
            }
        })
        return new SonarScannerFactory(project, monoProvider)
    }

    SonarScannerFactory(Project project, Provider<? extends File> monoExecutable) {
        this.project = project;
        this.monoExecutable = monoExecutable
    }

    public Optional<SonarScanner> fromPath(File workingDir) {
        def maybeExecutable = OSOps.findInOSPath(project, SonarScannerInstaller.EXECUTABLE_NAME)
        return maybeExecutable.map {executable -> fromExecutable(executable, workingDir) }
    }

    public SonarScanner fromExecutable(File scannerExec, File workingDir) {
        if(OSOps.isWindows()) {
            return SonarScanner.nativeBased(project, scannerExec, workingDir)
        } else {
            def monoExec = monoExecutable.get()
            return SonarScanner.monoBased(project, scannerExec, monoExec, workingDir)
        }
    }

}
