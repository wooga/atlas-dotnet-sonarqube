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

import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import wooga.gradle.dotnetsonar.tasks.traits.SonarScannerInstallSpec

class SonarScannerInstallInfo implements SonarScannerInstallSpec {

    private final Property<String> dotnetFramework = objects.property(String)

    Property<String> getDotnetFramework() {
        return dotnetFramework
    }

    void setDotnetFramework(String dotnetVersion) {
        this.dotnetFramework.set(dotnetVersion)
    }

    void setDotnetFramework(Provider<String> dotnetVersion) {
        this.dotnetFramework.set(dotnetVersion)
    }

    Provider<String> getVersionBasedSourceUrl() {
        return version.map {
            "https://github.com/SonarSource/sonar-scanner-msbuild/releases/download/${it}/sonar-scanner-msbuild-${it}-${dotnetFramework.get()}.zip".toString()
        }
    }

}
