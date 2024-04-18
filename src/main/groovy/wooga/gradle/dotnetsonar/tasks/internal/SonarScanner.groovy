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

import com.wooga.gradle.io.ExecSpec

class SonarScanner implements Serializable {

    private File executable;

    SonarScanner(File executable) {
        this.executable = executable
    }

    List<String> beginArgs(String sonarProjectKey, String sonarProjectName, String version, Map<String, Object> sonarqubeProperties) {
        def args = new ArrayList<String>()
        args.add(executable.absolutePath)
        args.add("begin")
        args.add("-k:${sonarProjectKey}")
        if(sonarProjectName != null) {
            args.add("-n:${sonarProjectName}")
        }
        if(version != null) {
            args.add("-v:${version}")
        }
        addPropertiesArgs(args, sonarqubeProperties)
        return args
    }

    List<String> endArgs(String loginToken=null) {
        def args = new ArrayList<String>()
        args.add(executable.absolutePath)
        args.add("end")
        if(loginToken != null) {
            addPropertiesArgs(args, ["sonar.login": loginToken])
        }
        return args
    }

    static void addPropertiesArgs(List<String> args, Map<String, Object> sonarqubeProperties) {
        sonarqubeProperties.entrySet().findAll {
            it.key != "sonar.projectKey" && //replaced by -k
            it.key != "sonar.projectName" && //replaced by -n
            it.key != "sonar.projectVersion" && //replace by -v
            it.key != "sonar.working.directory" //automatically set by SonarScanner and cannot be overridden
        }.forEach() { propEntry ->
                args.add("-d:${propEntry.key}=${propEntry.value.toString()}")
        }
    }
}
