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

package wooga.gradle.dotnetsonar

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.TaskProvider
import wooga.gradle.dotnetsonar.tasks.SonarScannerBegin
import wooga.gradle.dotnetsonar.tasks.SonarScannerEnd
import wooga.gradle.dotnetsonar.tasks.traits.SonarScannerSpec


class SonarScannerExtension implements SonarScannerSpec {

    private final Project project;

    final SonarScannerInstallInfo installInfo

    private final MapProperty<String, ?> sonarQubeProperties = project.objects.mapProperty(String, Object)

    MapProperty<String, ?> getSonarQubeProperties() {
        return sonarQubeProperties;
    }

    SonarScannerExtension(Project project) {
        this.project = project
        this.installInfo = objects.newInstance(SonarScannerInstallInfo)
    }

    /**
     * Registers tasks as sonar scanner build tasks, meaning that these tasks will be executed in between
     * SonarScannerBegin and SonarScannerEnd tasks
     * @param task tasks to be registered
     */
    void registerBuildTask(TaskProvider<? extends Task>... tasks) {
        tasks.each {taskProvider ->
            taskProvider.configure { task -> this.registerBuildTask(task) }
        }
    }

    /**
     * Registers tasks as sonar scanner build tasks, meaning that these tasks will be executed in between
     * SonarScannerBegin and SonarScannerEnd tasks
     * @param task tasks to be registered
     */
    void registerBuildTask(Task... task) {
        def extProject = project
        task.each {it ->
            it.dependsOn(extProject.tasks.withType(SonarScannerBegin))
            it.finalizedBy(extProject.tasks.withType(SonarScannerEnd))
        }
    }
    /**
     * Convenience method for filling up sonarscanner installation info. Only used if sonar scanner is downloaded from remote.
     * @param installInfoOps closure operating over @{SonarScannerInstallInfo} object
     */
    void installInfo(@DelegatesTo(SonarScannerInstallInfo)
                     @ClosureParams(value=SimpleType, options=["wooga.gradle.dotnetsonar.SonarScannerInstallInfo"])
                     Closure installInfoOps) {
        this.installInfo.with(installInfoOps)
    }


}
