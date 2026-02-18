Feature: Providing test execution measures
  As a CXX Plugin user, I want to import the Boost.Test execution reports into SonarQube

  # |
  # | Specification:
  # | * The plugin is able to import test which conform to the schema
  # |   described in xunit.rnc Note: some test frameworks (QUnit,
  # |   AFAIK), skip the top-tag 'testsuites'. Those report are
  # |   compatible with the element testsuite of the schema and can be
  # |   imported too.
  # |
  # | * Additionally, the plugin is able to import every XML format
  # |   when given an XSLT-sheet, which performs the XSL to JUnit
  # |   conversion (see sonar.cxx.xslt.xxx)
  # |
  # | * To locate the test source file:
  # |   If either the testcase tag or the enclosing testsuite tag
  # |   contains the attribute 'filename', its content is assumed
  # |   to contain a path to the according test source file.
  # |   Filename in testcase overwrites filename in testsuite
  # |   if both are present.
  # |

  Scenario: Importing simple Boost.Test report
    By default, the plugin doesn't try to assign the testcases
    found in the report to test files in Sonar, it just sums up
    all the measures and assigns it to the project. This makes the
    procedure more stable but doesn't provide the details for the
    testcases in SonarQube, i.e. the drilldown won't be possible.

    Given the project "boosttest_project"
    When I run sonar-scanner with "-X -Dsonar.cxx.xslt.1.inputs=btest_test_simple-test_suite.xml -Dsonar.cxx.xunit.reportPaths=btest_test_simple-test_suite.after_xslt"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages
    And the following metrics have following values:
      | metric               | value |
      | tests                | 1     |
      | test_failures        | 0     |
      | test_errors          | 0     |
      | skipped_tests        | 0     |
      | test_execution_time  | 0     |


  Scenario: Importing nested Boost.Test report
    Boost unit test framework supports nested testsuites.
    A testsuite is handled as a C++ namespace.
    Verify the support of nested testsuites.

    Given the project "boosttest_project"
    When I run sonar-scanner with "-X -Dsonar.cxx.xslt.1.inputs=btest_test_nested-test_suite.xml -Dsonar.cxx.xunit.reportPaths=btest_test_nested-test_suite.after_xslt"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages
    And the following metrics have following values:
      | metric               | value |
      | tests                | 4     |
      | test_failures        | 0     |
      | test_errors          | 4     |
      | skipped_tests        | 0     |
      | test_execution_time  | 3     |


  Scenario: Test with real Boost.Test framework
    Real boost test framework is a very complex usecase for preprocessor and parser.
    Test if plugin is able to handle this.
    Given the project "boosttest_project"
    And platform is not "Windows"
    When I run sonar-scanner with "-X -Dsonar.cxx.includeDirectories=/usr/include -Dsonar.cxx.xslt.1.inputs=btest_test_nested-test_suite.xml -Dsonar.cxx.xunit.reportPaths=btest_test_nested-test_suite.after_xslt"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*cannot find the sources for '.*'
      .*WARN.*Preprocessor:.*
      """
    And the following metrics have following values:
      | metric               | value |
      | tests                | 4     |
      | test_failures        | 0     |
      | test_errors          | 4     |
      | skipped_tests        | 0     |
      | test_execution_time  | 3     |
