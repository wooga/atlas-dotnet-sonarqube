/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.dotnetsonar.tasks.internal

import org.gradle.api.Project
import org.gradle.process.ExecSpec

class SonarScanner implements Serializable {

    static SonarScanner nativeBased(Project project, File executable, File workingDir) {
        return new SonarScanner(new GradleShell(project), executable, workingDir)
    }

    static SonarScanner monoBased(Project project, File executable, File monoExecutable, File workingDir) {
        return new SonarScanner(MonoShell.forProject(project, monoExecutable), executable, workingDir)
    }

    private Shell shell;
    private File executable;
    private File workingDir;

    private SonarScanner(Shell shell, File executable, File workingDir) {
        this.shell = shell
        this.executable = executable
        this.workingDir = workingDir
    }

    public void begin(String sonarProjectKey, String sonarProjectName, String version, Map<String, Object> sonarqubeProperties) {
       shell.execute { ExecSpec execSpec ->
           execSpec.workingDir = workingDir
           execSpec.executable = executable.absolutePath
           execSpec.args("begin")
           execSpec.args("-k:${sonarProjectKey}")
           if(sonarProjectName != null) {
               execSpec.args("-n:${sonarProjectName}")
           }
           if(version != null) {
               execSpec.args("-v:${version}")
           }
           addPropertiesArgs(execSpec, sonarqubeProperties)
        }.throwsOnFailure()
    }

    public void end(String loginToken=null) {
        shell.execute { ExecSpec execSpec ->
            execSpec.workingDir = workingDir
            execSpec.executable = executable.absolutePath
            execSpec.args("end")
            if(loginToken != null) {
                addPropertiesArgs(execSpec, ["sonar.login": loginToken])
            }
        }.throwsOnFailure()
    }

    static void addPropertiesArgs(ExecSpec execSpec, Map<String, Object> sonarqubeProperties) {
        sonarqubeProperties.entrySet().findAll {
            it.key != "sonar.projectKey" && //replaced by -k
            it.key != "sonar.projectName" && //replaced by -n
            it.key != "sonar.projectVersion" && //replace by -v
            it.key != "sonar.working.directory" //automatically set by SonarScanner and cannot be overridden
        }.forEach() { propEntry ->
                execSpec.args("-d:${propEntry.key}=${propEntry.value.toString()}")
        }
    }
}
