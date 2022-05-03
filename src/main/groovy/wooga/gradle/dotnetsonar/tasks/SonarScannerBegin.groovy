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

package wooga.gradle.dotnetsonar.tasks


import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner

class SonarScannerBegin extends DefaultTask {

    private final Property<SonarScanner> sonarScanner;
    private final MapProperty<String, Object> sonarqubeProperties;

    SonarScannerBegin() {
        this.sonarScanner = project.objects.property(SonarScanner)
        this.sonarqubeProperties = project.objects.mapProperty(String, Object)
    }

    @TaskAction
    def run() {
        def properties = sonarqubeProperties.get()
        def sonarScanner = sonarScanner.get()
        assertKeyOnMap(properties, "sonar.projectKey")
        assertKeyOnMap(properties, "sonar.version")
        sonarScanner.begin(
                properties["sonar.projectKey"].toString(),
                properties["sonar.projectName"]?.toString(),
                properties["sonar.version"]?.toString(),
                properties)
    }

    private static <T> void assertKeyOnMap(Map<T,?> map, T key) {
        if(!map.containsKey(key)) {
            throw new IllegalArgumentException("SonarqubeBegin needs a set ${key} property")
        }
    }

    @Input
    Property<SonarScanner> getSonarScanner() {
        return sonarScanner
    }

    @Input
    MapProperty<String, Object> getSonarqubeProperties() {

        return sonarqubeProperties
    }

    void setSonarScanner(SonarScanner sonarScanner) {
        this.sonarScanner.set(sonarScanner)
    }

    void setSonarqubeProperties(MapProperty<String, Object> sonarqubeProperties) {
        this.sonarqubeProperties.set(sonarqubeProperties)
    }

    void setSonarqubeProperties(Map<String, Object> sonarqubeProperties) {
        this.sonarqubeProperties.set(sonarqubeProperties)
    }

}
