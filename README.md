atlas-dotnet-sonarqube
=============

![Wooga Internal](https://img.shields.io/badge/wooga-internal-lightgray.svg?style=flat-square)
[![Gradle Plugin ID](https://img.shields.io/badge/gradle-net.wooga.github-brightgreen.svg?style=flat-square)](https://plugins.gradle.org/plugin/net.wooga.plugins)
[![Build Status](https://img.shields.io/travis/wooga/atlas-plugins/master.svg?style=flat-square)](https://travis-ci.org/wooga/atlas-plugins)
[![Coveralls Status](https://img.shields.io/coveralls/wooga/atlas-plugins/master.svg?style=flat-square)](https://coveralls.io/github/wooga/atlas-plugins?branch=master)
[![Apache 2.0](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](https://raw.githubusercontent.com/wooga/atlas-plugins/master/LICENSE)
[![GitHub tag](https://img.shields.io/github/tag/wooga/atlas-plugins.svg?style=flat-square)]()
[![GitHub release](https://img.shields.io/github/release/wooga/atlas-plugins.svg?style=flat-square)]()

This plugin wraps over [sonarscanner for .NET](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-msbuild/) providing sonarqube scan functionality for gradle dotnet projects. It tries to find a sonarscanner executable on PATH, and downloads it if none are found.

SonarScanner for .NET requires a build to be run during its execution, either the `dotnet` or `MSBuild` applications can be used.

Usage
=====

**build.gradle**

```groovy
plugins {
  id "net.wooga.gradle.dotnet-sonarqube" version "0.1.0"
}
```
The `sonarqube` extension can be configured in a similar way to [sonar-scanner-gradle](https://github.com/SonarSource/sonar-scanner-gradle). However, keep in mind that property restrictions for [SonarScanner for .NET](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-msbuild/) still applies. Sensible defaults are provided, with most being the same as the sonar-scanner-gradle plugin, and sonarqube host and access token can be provided using the `SONAR_HOST` and `SONAR_TOKEN` environment variables as well.

**build.gradle**

```groovy
sonarqube {
  properties {
    property "sonar.exclusions", "Assets/Paket.Unity3D/"
    property "sonar.cpd.exclusions", "Assets/Tests/"
    property "sonar.coverage.exclusions", "Assets/Tests/"
    property "sonar.cs.nunit.reportsPaths", "build/reports/unity/*/*.xml"
    property "sonar.cs.opencover.reportsPaths", "build/codeCoverage/**/*.xml"  
  }
}
```
The `solutionMSBuild` and `solutionDotnetBuild` tasks builds a solution using `MSBuild.exe` or `dotnet` respectively. It tries find the required executables on PATH, but you can use custom files using the `msBuildExcutable` or `dotnetExecutable` properties. The solution is by default a solution named after the gradle project, located in the project root, but it can be customized using the `solution` property.
Both `solutionMSBuild` and `solutionDotnetBuild` are instances of the `BuildSolution` task class.

```groovy
solutionMSBuild {
  msBuildExecutable = file("../msbuildexecutable")
  solution = "solution.sln"
  environment = ["envvar":"envvalue"]
}

solutionDotnetBuild {
  dotnetExecutable = file("../dotnetexecutable")
  solution = "solution.sln"
  environment = ["envvar":"envvalue"]
}
``` 

The analysis itself will be run when calling any `BuildSolution`-derivated task. Keep in mind that it doesn't run tests or creates coverage files, so make sure that those already exists and are indicated by the adequate Sonarqube properties.
If you wish to run each sonarscanner step by yourself, you can use the `sonarScannerBegin` and `sonarScannerEnd` to call the begin and end steps for [SonarScanner for .NET](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-msbuild/), respectively.

### Testing/Development

The resulting project structure is prepared as a normal groovy gradle plugin. Additional to the normal `test` task the plugin also adds a `integrationTest` task with code coverage and reporting. As test framework `Spock` will be added to the classpath. The jocoo plugin is used for code coverage. The integration test results are merged with the unit test results. All our plugins push the coverage report to coveralls. The coveralls plugin is connected to jacoco and only needs the `COVERALLS_TOKEN` in the environment. The plugin will also generate an idea project with the integration tests configured as seperate module1
The tasks `final` and `candidate` will publish the plugin to the gradle plugins repository with the help of the `plugin-publish` plugin and create a formal gitub release with the `net.wooga.github` plugin. The `snapshot` task will only publish to the local maven repository.

Documentation
=============

- [API docs](https://wooga.github.io/atlas-plugins/docs/api/)
- [Release Notes](RELEASE_NOTES.md)

Gradle and Java Compatibility
=============================

Tested with openJDK8

| Gradle Version  | Works  |
| :-------------: | :----: |
| < 5.0           | ![no]  |
| 5.0             | ![yes] |
| 5.1             | ![yes] |
| 5.2             | ![yes] |
| 5.3             | ![yes] |
| 5.4             | ![yes] |
| 5.5             | ![yes] |
| 5.6             | ![yes] |
| 5.6             | ![yes] |
| 6.0             | ![yes] |
| 6.1             | ![yes] |
| 6.2             | ![yes] |
| 6.3             | ![yes] |
| 6.4             | ![yes] |
| 6.5             | ![yes] |
| 6.6             | ![yes] |
| 6.6             | ![yes] |
| 6.7             | ![yes] |
| 6.8             | ![yes] |
| 6.9             | ![yes] |
| 7.0             | ![yes] |

Development
===========

[Code of Conduct](docs/Code-of-conduct.md)

LICENSE
=======

Copyright 2021 Wooga GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
