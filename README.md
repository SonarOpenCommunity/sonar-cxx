|     |     |     |
| --- | --- | --- |
| **Issue Stats** | [![Issue Stats](http://issuestats.com/github/SonarOpenCommunity/sonar-cxx/badge/pr)](http://issuestats.com/github/SonarOpenCommunity/sonar-cxx) | [![Issue Stats](http://issuestats.com/github/SonarOpenCommunity/sonar-cxx/badge/issue)](http://issuestats.com/github/SonarOpenCommunity/sonar-cxx) |
| **SonarCloud**<br>(Technical Debt analysis) | [![Quality Gate](https://sonarqube.com/api/badges/gate?key=org.sonarsource.sonarqube-plugins.cxx%3Acxx)](https://sonarcloud.io/dashboard?id=org.sonarsource.sonarqube-plugins.cxx%3Acxx) | ![Coverage](https://sonarqube.com/api/badges/measure?key=org.sonarsource.sonarqube-plugins.cxx%3Acxx&metric=coverage) |
| **Travis CI**<br>(Linux Build and Integration Tests) | [![Build Status](https://travis-ci.org/SonarOpenCommunity/sonar-cxx.svg?branch=master)](https://travis-ci.org/SonarOpenCommunity/sonar-cxx) |   |
| **AppVeyor CI**<br>(Windows Build and Deployment) | [![Build status](https://ci.appveyor.com/api/projects/status/f6p12h9n59w01770/branch/master?svg=true)](https://ci.appveyor.com/project/SonarOpenCommunity/sonar-cxx/branch/master) | [Download](https://ci.appveyor.com/project/SonarOpenCommunity/sonar-cxx/branch/master/artifacts) |

## SonarQube C++ plugin (Community)

[SonarQube](https://www.sonarqube.org) is an open platform to manage code quality. This plugin
adds C++ support to SonarQube with the focus on integration of existing C++ tools.

This plugin is free software; you can redistribute it and/or modify it under the terms of the [GNU Lesser General Public License](https://www.gnu.org/licenses/lgpl-3.0.en.html) as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

* parser supporting C89, C99, C11, C++03, C++11, C++14 and C++17 standards
  * Microsoft extensions: C++/CLI, Attributed ATL
  * GNU extensions
  * CUDA extensions
* Microsoft Windows and Linux for runtime environment

Sensors for **static and dynamic code analysis**:
* Cppcheck warnings support (http://cppcheck.sourceforge.net/)
* GCC/G++ warnings support (https://gcc.gnu.org/)
* Visual Studio warnings support (https://www.visualstudio.com/)
* Visual Studio Core Guideline Checker warnings support
* Clang Static Analyzer support (https://clang-analyzer.llvm.org/)
* Clang Tidy warnings support (http://clang.llvm.org/extra/clang-tidy/)
* PC-Lint warnings support (http://www.gimpel.com/)
* RATS (https://github.com/andrew-d/rough-auditing-tool-for-security)
* Valgrind (http://valgrind.org/)
* Vera++ (https://bitbucket.org/verateam/vera/wiki/Home)
* Dr. Memory warnings support (http://drmemory.org/)

**Test framework** sensors for:
* XUnit file format
* Google Test file format
* Boost.Test file format
* CppUnit file format
* VSTest file format
* NUnit file format
* extentions over XSLT possible

**Coverage** sensors for:
* Visual Studio coverage reports
* Gcov / gcovr coverage reports
* Bullseye coverage reports (http://www.bullseye.com/)
* Cobertura coverage reports (http://cobertura.github.io/cobertura/)
* Testwell CTC++ coverage reports (https://www.verifysoft.com/en_ctcpp.html)

Simple to **customize**
* provide the ability to write custom rules
* custom rules by XPath checks possible
* custom rules by regular expression checks possible
* easy 3rd party tool integration with XML rule definitions and reports possible


## Quickstart
1. Setup a SonarQube instance
2. Install the plugin (see [Installation](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Installation))
3. Run an analysis (see [Running the analysis](https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Running-the-analysis))


## Resources
- [Latest release](https://github.com/SonarOpenCommunity/sonar-cxx/releases)
- [Documentation](https://github.com/SonarOpenCommunity/sonar-cxx/wiki)
- [Issue Tracker](https://github.com/SonarOpenCommunity/sonar-cxx/issues)
- [Continuous Integration Unix](https://travis-ci.org/SonarOpenCommunity/sonar-cxx)
- [Continuous Integration Windows](https://ci.appveyor.com/project/SonarOpenCommunity/sonar-cxx)
- [Sample project](https://github.com/SonarOpenCommunity/sonar-cxx/tree/master/sonar-cxx-plugin/src/samples/SampleProject)


## Alternatives:
That's not the only choice when you are looking for C++ support in SonarQube there is also
* the commercial [C/C++ plugin from SonarSource](http://www.sonarsource.com/products/plugins/languages/cpp/).
* the commercial [C/C++ plugin from CppDepend](http://www.cppdepend.com/sonarplugin)
* the [Coverity plugin](https://github.com/coverity/coverity-sonar-plugin)
* the commercial [PVS-Studio plugin](https://www.viva64.com/en/pvs-studio-download/)

Choose whatever fits your needs.

## Subscribe
Subscribe our [release feed](https://github.com/SonarOpenCommunity/sonar-cxx/releases.atom)
