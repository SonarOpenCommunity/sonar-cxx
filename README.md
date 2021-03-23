|     |     |     |
| --- | --- | --- |
| **SonarCloud** / SonarSource SA<br>(Technical Debt analysis) | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.sonarsource.sonarqube-plugins.cxx%3Acxx&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.sonarsource.sonarqube-plugins.cxx%3Acxx) | ![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.sonarsource.sonarqube-plugins.cxx%3Acxx&metric=coverage) |
| **DeepCode** / DeepCode AG<br>(real-time AI powered semantic code analysis) | [![deepcode](https://www.deepcode.ai/api/gh/badge?key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwbGF0Zm9ybTEiOiJnaCIsIm93bmVyMSI6IlNvbmFyT3BlbkNvbW11bml0eSIsInJlcG8xIjoic29uYXItY3h4IiwiaW5jbHVkZUxpbnQiOmZhbHNlLCJhdXRob3JJZCI6MTU1ODMsImlhdCI6MTYwMTI4MjcwOH0.Wz0G-HIoHfLfP1SjzxUnbyA598JfjKkQTsBqGG4Kleo)](https://www.deepcode.ai/app/gh/SonarOpenCommunity/sonar-cxx/_/dashboard?utm_content=gh%2FSonarOpenCommunity%2Fsonar-cxx) |
| **JProfiler** / ej-technologies GmbH<br>(when it comes to profiling: [Java profiler](https://www.ej-technologies.com/products/jprofiler/overview.html) tool) | [![JProfiler](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png)](https://www.ej-technologies.com/products/jprofiler/overview.html)|
| **Travis CI**<br>(Linux Build and Integration Tests) | [![Build Status](https://travis-ci.org/SonarOpenCommunity/sonar-cxx.svg?branch=master)](https://travis-ci.org/SonarOpenCommunity/sonar-cxx) |
| **AppVeyor CI**<br>(Windows Build and Deployment) | [![Build status](https://ci.appveyor.com/api/projects/status/f6p12h9n59w01770/branch/master?svg=true)](https://ci.appveyor.com/project/SonarOpenCommunity/sonar-cxx/branch/master) | [Download latest snapshot](https://ci.appveyor.com/project/SonarOpenCommunity/sonar-cxx/branch/master/artifacts) |

# SonarQube C++ Community plugin (_cxx plugin_)

[SonarQube](https://www.sonarqube.org) is an open platform to manage code quality. This plugin
adds C++ support to SonarQube with the focus on integration of existing C++ tools.

The sensors for reading reports can be used with this _cxx plugin_ or [SonarCFamily](https://www.sonarsource.com/cpp/) plugin.

## License
This plugin is free software; you can redistribute it and/or modify it under the terms of the [GNU Lesser General Public License](https://github.com/SonarOpenCommunity/sonar-cxx/blob/master/LICENSE) as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

## Features
* parser supporting
  * `C++03`, `C++11`, `C++14`,`C++17`, `C++20`
  * `C89`, `C99`, `C11`, `C17`
* compiler specific extensions
  * Microsoft extensions: `C++/CLI`, `Attributed ATL`
  * GNU extensions
  * `CUDA` extensions
* Microsoft Windows and Linux for runtime environment

Sensors for **static and dynamic code analysis**:
* **Cppcheck** warnings support (http://cppcheck.sourceforge.net/)
  - [sonar.cxx.cppcheck.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.cppcheck.reportPaths)
* **GCC/G++** warnings support (https://gcc.gnu.org/)
  - [sonar.cxx.gcc.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.gcc.reportPaths)
* **Visual Studio** and **Core Guideline Checker** warnings support (https://www.visualstudio.com/)
  - [sonar.cxx.vc.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.vc.reportPaths)
* **Clang Static Analyzer** support (https://clang-analyzer.llvm.org/)
  - [sonar.cxx.clangsa.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.clangsa.reportPaths)
* **Clang-Tidy** warnings support (http://clang.llvm.org/extra/clang-tidy/)
  - [sonar.cxx.clangtidy.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.clangtidy.reportPaths)
* **Infer** warnings support (https://fbinfer.com/)
  - [sonar.cxx.infer.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.infer.reportPaths)
* **PC-Lint** warnings support (http://www.gimpel.com/)
  - [sonar.cxx.pclint.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.pclint.reportPaths)
* **RATS** (https://github.com/andrew-d/rough-auditing-tool-for-security)
  - [sonar.cxx.rats.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.rats.reportPaths)
* **Valgrind** (http://valgrind.org/)
  - [sonar.cxx.valgrind.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.valgrind.reportPaths)
* **Vera++** (https://bitbucket.org/verateam/vera/wiki/Home)
  - [sonar.cxx.vera.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.vera.reportPaths)
* **Dr. Memory** warnings support (http://drmemory.org/)
  - [sonar.cxx.drmemory.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.drmemory.reportPaths)
* [Generic Issue Import Format](https://docs.sonarqube.org/latest/analysis/generic-issue/) support
* any other tool can be integrated
  - [sonar.cxx.other.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.other.reportPaths)

**Test framework** sensors for:
* **XUnit** file format
  - [sonar.cxx.xunit.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.xunit.reportPaths)
* **Google Test (gtest)** file format (https://github.com/google/googletest)
  - [sonar.cxx.xunit.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.xunit.reportPaths)
* **Boost.Test** file format (https://www.boost.org/doc/libs/release/libs/test/)
  - [sonar.cxx.xunit.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.xunit.reportPaths) with [sonar.cxx.xslt](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.xslt)
* **CppTest** file format (https://cpptest.sourceforge.io/)
  - [sonar.cxx.xunit.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.xunit.reportPaths) with [sonar.cxx.xslt](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.xslt) 
* **CppUnit** file format (https://sourceforge.net/projects/cppunit/)
  - [sonar.cxx.xunit.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.xunit.reportPaths) with [sonar.cxx.xslt](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.xslt)
* **VSTest** file format (https://github.com/microsoft/vstest)
  - [sonar.cxx.vstest.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.vstest.reportPaths)
* **NUnit** file format (https://nunit.org/)
  - [sonar.cxx.nunit.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.nunit.reportPaths)
* [Generic Test Data](https://docs.sonarqube.org/latest/analysis/generic-test/) support
* extensions over XSLT possible
  - [sonar.cxx.xslt](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.xslt)

**Coverage** sensors for:
* **Visual Studio** coverage reports (https://www.visualstudio.com/)
  - [sonar.cxx.vscoveragexml.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.vscoveragexml.reportPaths)
* **BullseyeCoverage** reports (http://www.bullseye.com/)
  - [sonar.cxx.bullseye.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.bullseye.reportPaths)
* **Cobertura** coverage reports (http://cobertura.github.io/cobertura/)
   * **gcov / gcovr** coverage reports --xml https://gcovr.com/en/stable/guide.html
   * **OpenCppCoverage** --export_type=cobertura (https://github.com/OpenCppCoverage/OpenCppCoverage/)
   * [sonar.cxx.cobertura.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.cobertura.reportPaths)
* **Testwell CTC++** coverage reports (https://www.verifysoft.com/en_ctcpp.html)
  - [sonar.cxx.ctctxt.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.ctctxt.reportPaths)
* [Generic Coverage](https://docs.sonarqube.org/latest/analysis/generic-test/) support

Simple to **customize**
* custom rules by [regular expression template](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/CXX-Custom-Regex-Rules) possible
* custom rules by [XPath template rule](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/CXX-Custom-XPath-Rules) possible
* [extend CXX repositories](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/CXX-Custom-Template-Rules) with custom rules
* easy 3rd party tool integration with XML rule definitions and reports possible
  - [sonar.cxx.other.reportPaths](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/sonar.cxx.other.reportPaths)
* provide the ability to add custom rules
  * [Writing a SonarQube plugin](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/CXX-Custom-Rules) in Java that uses SonarQube APIs to add new rules

## Quickstart
1. [Setup a SonarQube instance](https://docs.sonarqube.org/latest/setup/overview/)
2. [Install the Plugin](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Install-the-Plugin)
3. [Run an analysis](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Scan-Source-Code)

## Resources
- [Latest release](https://github.com/SonarOpenCommunity/sonar-cxx/releases)
- [Download latest snapshot](https://ci.appveyor.com/project/SonarOpenCommunity/sonar-cxx/branch/master/artifacts)
- [Documentation](https://github.com/SonarOpenCommunity/sonar-cxx/wiki)
- [Issue Tracker](https://github.com/SonarOpenCommunity/sonar-cxx/issues)

## Contributing
You are welcome to contribute. [Help is needed](https://github.com/SonarOpenCommunity/sonar-cxx/blob/master/CONTRIBUTING.md).

## Alternatives
That's not the only choice when you are looking for C++ support in SonarQube there is also
* the commercial [SonarCFamily plugin from SonarSource](https://www.sonarsource.com/cpp)
* the commercial [C/C++ plugin from CppDepend](http://www.cppdepend.com/sonarplugin)
* the [Coverity plugin](https://github.com/coverity/coverity-sonar-plugin)
* the commercial [PVS-Studio plugin](https://www.viva64.com/en/pvs-studio-download)

Choose whatever fits your needs.
