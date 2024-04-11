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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.sonarqube.gradle.ActionBroadcast
import org.sonarqube.gradle.SonarPropertyComputer
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubeProperties
import wooga.gradle.dotnetsonar.tasks.BuildSolution
import wooga.gradle.dotnetsonar.tasks.SonarScannerBegin
import wooga.gradle.dotnetsonar.tasks.SonarScannerEnd
import wooga.gradle.dotnetsonar.tasks.internal.DotNet
import wooga.gradle.dotnetsonar.tasks.internal.MSBuild
import wooga.gradle.dotnetsonar.tasks.internal.OSOps
import wooga.gradle.github.base.GithubBasePlugin
import wooga.gradle.github.base.GithubPluginExtension
import wooga.gradle.github.base.internal.DefaultGithubPluginExtension

import static SonarScannerExtension.*

class DotNetSonarqubePlugin implements Plugin<Project> {

    protected Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.plugins.apply(GithubBasePlugin)
        def githubExt = project.extensions.findByType(DefaultGithubPluginExtension)
        def actionBroadcast = new ActionBroadcast<SonarQubeProperties>()
        def sonarScannerExt = createSonarScannerExtension(actionBroadcast)
        def sonarQubeExt = project.extensions.create(SonarQubeExtension.SONARQUBE_EXTENSION_NAME, SonarQubeExtension, actionBroadcast)
        sonarQubeExt.properties { it ->
            defaultSonarProperties(githubExt, it)
        }

        project.tasks.withType(BuildSolution).configureEach {task ->
            def projectDir = project.layout.projectDirectory
            task.msBuildExecutable.convention(sonarScannerExt.buildTools.msBuildExecutable)
            task.dotnetExecutable.convention(sonarScannerExt.buildTools.dotnetExecutable)
            task.solution.convention(projectDir.file("${project.name}.sln"))

            sonarScannerExt.registerBuildTask(task)
        }

        project.tasks.register(MS_BUILD_TASK_NAME, BuildSolution).configure { task ->
            task.buildTool.convention(task.msBuildBuildTool(sonarScannerExt.buildTools.msBuildExecutable.asFile))
        }

        project.tasks.register(DOTNET_BUILD_TASK_NAME, BuildSolution).configure { task ->
            task.buildTool.convention(task.dotnetBuildTool(sonarScannerExt.buildTools.dotnetExecutable.asFile))
        }

        def beginTask = project.tasks.register(BEGIN_TASK_NAME, SonarScannerBegin) {beginTask ->
            beginTask.sonarScanner.convention(sonarScannerExt.sonarScanner)
            beginTask.sonarqubeProperties.convention(sonarScannerExt.sonarQubeProperties)
        }

        project.tasks.register(END_TASK_NAME, SonarScannerEnd) { endTask ->
            def tokenProvider = sonarScannerExt.sonarQubeProperties.map{it["sonar.login"].toString()}
            endTask.sonarScanner.convention(sonarScannerExt.sonarScanner)
            endTask.loginToken.convention(tokenProvider)
            endTask.dependsOn(beginTask)
        }
    }

    SonarScannerExtension createSonarScannerExtension(ActionBroadcast<SonarQubeProperties> actionBroadcast) {

        def extension = project.extensions.create(SONARSCANNER_EXTENSION_NAME, SonarScannerExtension, project)
        def resolvedProperties = project.provider{ computeSonarProperties(actionBroadcast) }
        extension.sonarQubeProperties.convention(resolvedProperties)
        extension.sonarScannerExecutable.convention(DotNetSonarqubePluginConventions.sonarScannerExecutable.getFileValueProvider(project))

        DotNetSonarqubePluginConventions.monoExecutable.defaultValue = {
            OSOps.findInOSPath(project, "dotnet")
            .or { OSOps.findInOSPath(project, "mono") }
                .map {it.absolutePath }
            .orElse(null)
        }
        extension.monoExecutable.convention(DotNetSonarqubePluginConventions.monoExecutable.getFileValueProvider(project))

        DotNetSonarqubePluginConventions.msbuildExecutable.defaultValue = {
            OSOps.findInOSPath(project, "MSBuild.exe", "msbuild")
                .map {it.absolutePath }
            .orElse(null)
        }
        extension.buildTools.msBuildExecutable.convention(
                DotNetSonarqubePluginConventions.msbuildExecutable.getFileValueProvider(project)
        )

        DotNetSonarqubePluginConventions.msbuildExecutable.defaultValue = {
            OSOps.findInOSPath(project, "dotnet")
                    .map {it.absolutePath }
                    .orElse(null)
        }
        extension.buildTools.dotnetExecutable.convention(
                DotNetSonarqubePluginConventions.dotnetExecutable.getFileValueProvider(project)
        )

        return extension
    }



    //Wizardry from the sonarqube plugin. Needed for resolving the properties.
    Map<String, Object> computeSonarProperties(ActionBroadcast<SonarQubeProperties> actionBroadcast) {
        def actionBroadcastMap = new HashMap<String, ActionBroadcast<SonarQubeProperties>>()
        actionBroadcastMap[project.path] = actionBroadcast
        def propertyComputer = new SonarPropertyComputer(actionBroadcastMap, project)
        def properties = propertyComputer.computeSonarProperties()
        return properties.collectEntries {
            return it.value != null && it.value != ""? [it.key, it.value] : []
        }.findAll {it.key != null && it.value != null }
    }

    void defaultSonarProperties(GithubPluginExtension githubExt, SonarQubeProperties properties) {
        def companyNameProvider = githubExt.repositoryName.map{String fullRepoName -> fullRepoName.split("/")[0]}
        def repoNameProvider = githubExt.repositoryName.map{String fullRepoName -> fullRepoName.split("/")[1]}
        def keyProvider = companyNameProvider.map { comp ->
            return repoNameProvider.map {repoName -> "${comp}_${repoName}"}.getOrNull()
        }
        //branch.name should be empty if this branch is from a PR, so that the github PR can be set up.
        def branchProvider = githubExt.branchName.
                map {it -> it.empty? null : it}.
                map{isPR(it)? null : it}
        properties.with {
            property("sonar.login", System.getenv('SONAR_TOKEN'))
            property("sonar.host.url", System.getenv('SONAR_HOST'))
            property("sonar.projectKey", keyProvider.getOrNull())
            property("sonar.projectName", repoNameProvider.getOrNull())
            property("sonar.branch.name", branchProvider.getOrNull())
            if(project.version != null) {
                property("sonar.version", project.version.toString())
            }
        }
    }

    //PR branches are identified as PR-XX where XX is any number.
    static boolean isPR(String currentBranch) {
        def maybePrNumber = currentBranch.replace("PR-", "").trim()
        return currentBranch.toUpperCase().startsWith("PR-") && maybePrNumber.isNumber()
    }


}
