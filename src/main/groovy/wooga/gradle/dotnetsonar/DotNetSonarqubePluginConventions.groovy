package wooga.gradle.dotnetsonar

import com.wooga.gradle.PropertyLookup

class DotNetSonarqubePluginConventions {
    static PropertyLookup sonarScannerExecutable = new PropertyLookup(
            "SONAR_SCANNER_SONAR_SCANNER_EXECUTABLE",
            "sonarScanner.sonarScannerExecutable",
            null
    )

    static PropertyLookup monoExecutable = new PropertyLookup(
            ["SONAR_SCANNER_MONO_EXECUTABLE", "SONAR_SCANNER_DOTNET_EXECUTABLE"],
            ["sonarScanner.monoExecutable", "sonarScanner.dotnetExecutable"],
            null
    )

    static PropertyLookup msbuildExecutable = new PropertyLookup(
            ["SONAR_SCANNER_BUILD_TOOLS_MSBUILD_EXECUTABLE"],
            "sonarScanner.buildTools.msBuildExecutable",
            null
    )

    static PropertyLookup dotnetExecutable = new PropertyLookup(
            ["SONAR_SCANNER_BUILD_TOOLS_DOTNET_EXECUTABLE"],
            ["sonarScanner.buildTools.dotnetExecutable"],
            null
    )
}
