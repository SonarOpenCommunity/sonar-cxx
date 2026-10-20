This is an integration test suite for the SonarQube C++ Community Plugin. It
provides a means to check that the plugin works (or works not) with a
particular SonarQube version/setup.


Preconditions
=============

Make sure the following preconditions are met, before running the test suite:

* Python is installed
* behave (http://pythonhosted.org/behave/) is installed
* request module is available ('pip install requests' may help)
* Optional: colorama module is installed ('pip install colorama')


Usage
=====
Either install the plugin, startup SonarQube manually and simply run:

$ behave

from the project root folder or let the test suite do the job by
telling it the path to your SQ installation:

$ SONARHOME=/path/to/SonarQuebe behave

In the latter case, the suite will automatically install and test the
jar in sonar-python-plugin/target. So make sure the plugin is build
and the jar is available.

The URL to your SonarQube instance is per default http://localhost:9000.
You can set the environment variable SONAR_HOST_URL to use a different one.

Environment variable SONAR_TOKEN is used by the scanner to authenticate to
your SonarQube instance. A token can be specific to a project (allows you to
run analyses on the specific project) or global (allows you to run analyses on
any project). The corresponding SonarQube user must have the Execute Analysis
permission on the project or the Global Execute Analysis permission, respectively.
If no SONAR_TOKEN exists the Python script creates an own one over the Web API.


Features to test
================
- core/common:
  - finding reports
    - using slashes as separators
    - using backslashes as separators
    - using div. patterns

- Import of the coverage data
  - BullsEye, coberture and VisualStudio formats
  - unit test, integration test & overall test coverage
  - line and branch coverage
  - valid, invalid and missing reports

- Import of compiler warnings
  - GCC and VS formats
  - valid, invalid and missing reports
  - wrong encoding
  - custom regex?

- Detection of duplicated C++ code

- Import of CppCheck reports
  - V1 and V2 formats
  - valid, invalid and missing reports
  - unknown rule

- Import of Clang Static Analyzer reports
  - import plist format
  - import scan-build results (plist)
  - valid, invalid and missing reports

- Rules from the common-repository
- Rules implemented in the plugin
- Multi-language support
- Multi-module support


Why behave/Python
=================
Gherkin as a specification language is quite an obvious choice because
of its clarity. JBehave seems to be big and bloated. Besides, its just
faster to write the steps implementation in something like
Python. Hence behave.
