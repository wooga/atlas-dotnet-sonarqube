package wooga.gradle.dotnetsonar

import com.wooga.gradle.PropertyLookup

class DotNetSonarqubePluginConventions {

    static PropertyLookup scannerInstallUrl = new PropertyLookup(
            "SONAR_SCANNER_SCANNER_INSTALL_URL",
            "sonarScanner.scannerInstallUrl",
            null
    )

    static PropertyLookup scannerInstallDir = new PropertyLookup(
            "SONAR_SCANNER_SCANNER_INSTALL_DIR",
            "sonarScanner.scannerInstallDir",
            null
    )

    static PropertyLookup scannerInstallVersion = new PropertyLookup(
            "SONAR_SCANNER_SCANNER_INSTALL_VERSION",
            "sonarScanner.sonarScannerVersion",
            '5.15.1.88158'
    )

    static PropertyLookup scannerDotnetExecutable = new PropertyLookup(
            "SONAR_SCANNER_SCANNER_DOTNET_EXECUTABLE",
            "sonarScanner.scannerDotNetExecutable",
            'dotnet' //system dotnet
    )

    static PropertyLookup scannerExecutable = new PropertyLookup(
            "SONAR_SCANNER_SONAR_SCANNER_EXECUTABLE",
            "sonarScanner.scannerExecutable",
            null
    )
}
