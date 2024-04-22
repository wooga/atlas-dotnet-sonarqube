package wooga.gradle.unitysonar

import com.wooga.gradle.PropertyLookup

class UnitySonarqubePluginConventions {

    static PropertyLookup buildDotnetVersion = new PropertyLookup(
            "UNITY_SONARQUBE_BUILD_DOTNET_VERSION",
            "unitySonarqube.buildDotnetVersion",
            "7.0.100"
    )

    static PropertyLookup buildDotnetExecutable = new PropertyLookup(
            "UNITY_SONARQUBE_BUILD_DOTNET_EXECUTABLE",
            "unitySonarqube.buildDotnetExecutable",
            null
    )

}
