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


import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input

class SonarScannerBegin extends SonarScannerTask {

    private final MapProperty<String, Object> sonarqubeProperties;

    @Input
    MapProperty<String, Object> getSonarqubeProperties() {
        return sonarqubeProperties
    }

    void setSonarqubeProperties(MapProperty<String, Object> sonarqubeProperties) {
        this.sonarqubeProperties.set(sonarqubeProperties)
    }

    void setSonarqubeProperties(Map<String, Object> sonarqubeProperties) {
        this.sonarqubeProperties.set(sonarqubeProperties)
    }


    SonarScannerBegin() {
        this.sonarqubeProperties = project.objects.mapProperty(String, Object)
        this.additionalArguments.addAll(sonarScanner.map {sonarScanner ->
            def properties = sonarqubeProperties.getOrElse([:])
            SonarScannerBegin.assertKeyOnMap(properties, "sonar.projectKey")
            SonarScannerBegin.assertKeyOnMap(properties, "sonar.version")
            return sonarScanner.beginArgs(
                        properties["sonar.projectKey"].toString(),
                        properties["sonar.projectName"]?.toString(),
                        properties["sonar.version"]?.toString(),
                        properties)
        })
    }

    private static <T> void assertKeyOnMap(Map<T,?> map, T key) {
        if(!map.containsKey(key)) {
            throw new IllegalArgumentException("SonarScannerBegin task needs a set ${key} property")
        }
    }
}
