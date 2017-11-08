@SqApi67
Feature: Providing test execution numbers
  As a SonarQube user,
  I want to import the test execution reports into SonarQube
  In order to have the text execution metrics in SonarQube and use such SonarQube features as:
  - continuous monitoring of those metrics
  - Quality Gates on top of those metrics
  - Overview over the test execution status
  - ...

  # |
  # | Specification:
  # | * The plugin is able to import test which conform to the schema
  # |   described in xunit.rnc Note: some test frameworks (QUnit,
  # |   AFAIK), skip the top-tag 'testsuites'. Those report are
  # |   compatible with the element testsuite of the schema and can be
  # |   imported too.
  # |
  # | * Additionally, the plugin is able to import every XML format 'X'
  # |   when given an XSLT-sheet, which performs the X->JUnitReport
  # |   conversion (This feature should really be deprecated...)
  # |
  # | * To locate the test source file for assigning, there are
  # |   two strategies:
  # |   1. If either the testcase-tag or the enclosing testsuite tag
  # |      contains the attribute 'filename', its content is assumed
  # |      to contain a relative path to the according test source
  # |      file. Filename in testcase overwrites filename in testsuite
  # |      if both are present.
  # |   2. Via parsing the test files and trying to find the values of
  # |      'classname' in the resulting AST's. This requires the test
  # |      sources to be actually parsable.
  # |

  Scenario: Importing unchanged googletest reports in default mode
    By default, the plugin doesn't try to assign the testcases
    found in the report to test files in Sonar, it just sums up
    all the measures and assigns it to the project. This makes the
    procedure more stable but doesn't provide the details for the
    testcases in SonarQube, i.e. the drilldown won't be possible.

    Given the project "googletest_project"
    When I run "sonar-scanner -X -Dsonar.cxx.xunit.reportPath=gtest_without_fixture.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
      .*WARN.*cannot find the sources for '#include <unistd\.h>'
      """
    And the following metrics have following values:
      | metric               | value |
      | tests                | 1     |
      | test_failures        | 0     |
      | test_errors          | 0     |
      | skipped_tests        | 0     |
      | test_execution_time  | 0     |

  Scenario: googletest report is invalid
    Given the project "googletest_project"
    When I run "sonar-scanner -Dsonar.cxx.errorRecoveryEnabled=false -Dsonar.cxx.xunit.reportPath=invalid_report.xml"
    Then the analysis breaks
    And the analysis log contains a line matching:
      """
      .*ERROR.*Cannot feed the data into SonarQube, details: .*
      """

  Scenario: Importing simple boosttest report in default mode
    By default, the plugin doesn't try to assign the testcases
    found in the report to test files in Sonar, it just sums up
    all the measures and assigns it to the project. This makes the
    procedure more stable but doesn't provide the details for the
    testcases in SonarQube, i.e. the drilldown won't be possible.

    Given the project "boosttest_project"
    When I run "sonar-scanner -X -Dsonar.cxx.xunit.reportPath=btest_test_simple-test_suite.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      """
    And the following metrics have following values:
      | metric               | value |
      | tests                | 1     |
      | test_failures        | 0     |
      | test_errors          | 0     |
      | skipped_tests        | 0     |
      | test_execution_time  | 0     |

  Scenario: Importing nested boosttest report in default mode
    Boost unit test framework supports nested testsuites.
    A testsuite is handled as a C++ namespace.
    Verify the support of nested testsuites.

    Given the project "boosttest_project"
    When I run "sonar-scanner -X -Dsonar.cxx.xunit.reportPath=btest_test_nested-test_suite.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      """
    And the following metrics have following values:
      | metric               | value |
      | tests                | 4     |
      | test_failures        | 0     |
      | test_errors          | 4     |
      | skipped_tests        | 0     |
      | test_execution_time  | 3     |

  Scenario: Simulate virtual unit test file (provideDetails, filename tag)
    Starting with SQ 4.2 virtual files are no more supported.
    With 'boosttest-1.x-to-junit-1.0-dummy.xsl' it is possible to simulate this feature again.
    The stylesheet set the filename tag to './cxx-xunit/dummy.cpp' additional the dummy
    cpp unittest file in the test folder is needed.

    Given the project "boosttest_project"
    When I run "sonar-scanner -X -Dsonar.cxx.xunit.xsltURL=boosttest-1.x-to-junit-dummy-1.0.xsl -Dsonar.tests=cxx-xunit -Dsonar.cxx.xunit.provideDetails=true -Dsonar.cxx.xunit.reportPath=btest_test_nested-test_suite.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      """
    And the following metrics have following values:
      | metric               | value |
      | tests                | 4     |
      | test_failures        | 0     |
      | test_errors          | 4     |
      | skipped_tests        | 0     |
      | test_execution_time  | 3     |

  Scenario: Test with real boost test framework
    Real boost test framework is a very complex usecase for preprocessor and parser.
    Test if plugin is able to handle this.
    Given the project "boosttest_project"
    And platform is not "Windows"
    When I run "sonar-scanner -X -Dsonar.cxx.xunit.reportPath=btest_test_nested-test_suite.xml -Dsonar.cxx.xunit.provideDetails=true -Dsonar.cxx.includeDirectories=/usr/include"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      .*WARN.*cannot find the sources for '.*'
      """
    And the following metrics have following values:
      | metric               | value |
      | tests                | 4     |
      | test_failures        | 0     |
      | test_errors          | 4     |
      | skipped_tests        | 0     |
      | test_execution_time  | 3     |

  Scenario Outline: Importing augmented test reports generated by googletest
    Test reports can be augmented with the filename-attribute which
    may help the plugin to assign the testcases in the report to the
    correct source code files.

    Given the project "googletest_project"
    When I run "sonar-scanner -X -Dsonar.cxx.xunit.reportPath=<reportpath>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
      .*WARN.*cannot find the sources for '#include <unistd\.h>'
      """
    And the following metrics have following values:
      | metric               | value |
      | tests                | 1     |
      | test_failures        | 0     |
      | test_errors          | 0     |
      | skipped_tests        | 0     |
      | test_execution_time  | 0     |
    Examples:
      | reportpath              |
      | gtest_fname_in_ts.xml   |
      | gtest_fname_in_tc.xml   |
      | gtest_fname_in_both.xml |

  Scenario Outline: googletest reports cannot be found or are empty
    Given the project "googletest_project"
    When I run "sonar-scanner -Dsonar.cxx.xunit.reportPath=<reportpath>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
      .*WARN.*cannot find the sources for '#include <unistd\.h>'
      .*WARN.*The report.*seems to be empty, ignoring\.
      .*WARN.*Cannot find a report for '.*'
      """
    And the following metrics have following values:
      | metric               | value |
      | tests                | None  |
      | test_failures        | None  |
      | test_errors          | None  |
      | skipped_tests        | None  |
      | test_execution_time  | None  |
    Examples:
      | reportpath       |
      | notexistingpath  |
      | empty_report.xml |

