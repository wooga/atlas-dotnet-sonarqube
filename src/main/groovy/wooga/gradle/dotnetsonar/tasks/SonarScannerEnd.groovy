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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import wooga.gradle.dotnet.tasks.DotnetTask
import wooga.gradle.dotnetsonar.tasks.internal.SonarScanner
import wooga.gradle.dotnetsonar.tasks.traits.SonarScannerSpec

class SonarScannerEnd extends SonarScannerTask {

    private final Property<String> loginToken;

    SonarScannerEnd() {
        this.loginToken = project.objects.property(String)
        additionalArguments.addAll(sonarScanner.map {sonarScanner ->
            def token = loginToken.getOrNull()
            return sonarScanner.endArgs(token)
        })
    }

    @Input
    Property<String> getLoginToken() {
        return loginToken
    }

    void setLoginToken(String token) {
        this.loginToken.set(token)
    }
}
