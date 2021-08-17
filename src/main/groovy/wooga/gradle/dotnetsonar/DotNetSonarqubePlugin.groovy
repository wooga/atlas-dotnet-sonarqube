package wooga.gradle.dotnetsonar

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.sonarqube.gradle.ActionBroadcast
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubeProperties
import wooga.gradle.dotnetsonar.tasks.BuildSolution
import wooga.gradle.dotnetsonar.tasks.SonarScannerBegin
import wooga.gradle.dotnetsonar.tasks.SonarScannerEnd

import static SonarScannerExtension.*

class DotNetSonarqubePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def actionBroadcast = new ActionBroadcast<SonarQubeProperties>()
        def sonarScannerExt = project.extensions.create(SONARSCANNER_EXTENSION_NAME, SonarScannerExtension, project, actionBroadcast)
        def sonarQubeExt = project.extensions.create(SonarQubeExtension.SONARQUBE_EXTENSION_NAME, SonarQubeExtension, actionBroadcast)
        sonarQubeExt.properties { it ->
            defaultSonarProperties(project, it)
        }

        project.tasks.register(MS_BUILD_TASK_NAME, BuildSolution).configure { buildTask ->
            configureDefaultMSBuild(buildTask)
        }

        project.tasks.register(DOTNET_BUILD_TASK_NAME, BuildSolution).configure { buildTask ->
            configureDefaultDotNetBuild(buildTask)
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

        project.tasks.withType(BuildSolution).configureEach {
            sonarScannerExt.registerBuildTask(it)
        }
    }

    static final void defaultSonarProperties(Project project, SonarQubeProperties properties) {
        properties.with {
            property("sonar.login", System.getenv('SONAR_TOKEN'))
            property("sonar.host.url", System.getenv('SONAR_HOST'))
            //would be better if this was associated to github repository, see atlas-plugins
            property("sonar.projectKey", project.rootProject.name)
            property("sonar.projectName", project.rootProject.name)
            //property("sonar.sources", ".")
            if(project.version != null) {
                property("sonar.version", project.version.toString())
            }
        }
    }
}

/*
    steps:
    1. Create solution if not exists already //unity task
    2. Setup unity code coverage //unity task
    3. download dotnet-sonarscanner if not exists //done(ish)
    3. SonarBegin
    4. Build C# solution //done(ish)
    5. SonarEnd
* */


//    task(setupUnityProject, type:wooga.gradle.unity.tasks.Unity) {
//        args "-executeMethod", "UnityEditor.SyncVS.SyncSolution"
//        quit = true
//    }
//    tasks.withType(wooga.gradle.unity.tasks.Test) {
//        it.dependsOn setupCodeCoverage
//        it.args "-enableCodeCoverage"
//        it.args "-debugCodeOptimization"
//        it.args "-coverageResultsPath", "build/codeCoverage"
//        it.args "-coverageOptions", "generateAdditionalMetrics"
//    }
//    task setupCodeCoverage {
//        dependsOn setupUnityProject
//        doLast {
//            def manifest = new File(project.projectDir, 'Packages/manifest.json')
//            def jsonSlurper = new JsonSlurper()
//            def data = jsonSlurper.parse(manifest)
//            data.dependencies["com.unity.testtools.codecoverage"] = "1.1.0"
//            def json_str = JsonOutput.toJson(data)
//            def json_beauty = JsonOutput.prettyPrint(json_str)
//            manifest.write(json_beauty)
//        }
//    }

// configuration inspired by https://github.com/MirageNet/Mirage/blob/master/.github/workflows/main.yml
//        task(sonarBegin, type:Exec) {
//            dependsOn test
//            executable "packages/dotnet-framework-sonarscanner/tools/SonarScanner.MSBuild.exe"
//            args "begin"
//            args "/k:${rootProject.name}"
//            args "/v:${version}"
//            args "/d:sonar.login=${System.getenv('SONAR_TOKEN')}"
//            args "/d:sonar.host.url=${System.getenv('SONAR_HOST')}"
//            args "/d:sonar.exclusions=Assets/Paket.Unity3D/**"
//            args "/d:sonar.cpd.exclusions=Assets/Tests/**"
//            args "/d:sonar.coverage.exclusions=Assets/Tests/**"
//            args "/d:sonar.cs.nunit.reportsPaths=build/reports/unity/*/*.xml"
//            args "/d:sonar.cs.opencover.reportsPaths=build/codeCoverage/**/*.xml"
//        }
//
//        task(sonarMsbuild) {
//            dependsOn sonarBegin
//            doLast {
//                def contentPath =  "${unity.unityPath.getParent()}\\..\\Editor\\Data"
//                exec {
//                    executable "${contentPath}\\NetCore\\Sdk-2.2.107\\dotnet"
//                    environment "FrameworkPathOverride", "${contentPath}\\MonoBleedingEdge"
//                    args "build"
//                    args "${project.name}.sln"
//                }
//            }
//        }
//
//        task(sonarEnd, type:Exec) {
//            dependsOn sonarMsbuild
//            executable "packages/dotnet-framework-sonarscanner/tools/SonarScanner.MSBuild.exe"
//            args "end"
//            args "/d:sonar.login=${System.getenv('SONAR_TOKEN')}"
//        }
//
//        task(sonarqube) {
//            mustRunAfter test
//            dependsOn sonarBegin, sonarEnd, sonarMsbuild
//        }
//