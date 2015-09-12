Feature: Providing test execution numbers

  As a SonarQube user
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
  # | * It supports two modes: 'simple' and the 'detailed' (switched
  # |   via the parameter -Dsonar.cxx.xunit.provideDetails). In simple
  # |   mode it just aggregates the measures contained in the reports
  # |   and saves the result in the project, skipping all the testcase
  # |   details. In detailed mode, the plugin tries to find the
  # |   test source files where the testcases are implemented in
  # |   and saves the measures to those input files.
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

  # ==================
  # === googletest ===
  # ==================

  Scenario: Importing unchanged googletest reports in default mode

      By default, the plugin doesn't try to assign the testcases
      found in the report to test files in Sonar, it just sums up
      all the measures and assigns it to the project. This makes the
      procedure more stable but doesn't provide the details for the
      testcases in SonarQube, i.e. the drilldown wont be possible.

      GIVEN the project "googletest_project"

      WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.reportPath=gtest_without_fixture.xml"

      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
              .*WARN.*cannot find the sources for '#include <unistd\.h>'
              """
          AND the following metrics have following values:
              | metric               | value |
              | tests                | 1     |
              | test_failures        | 0     |
              | test_errors          | 0     |
              | skipped_tests        | 0     |
              | test_success_density | 100   |
              | test_execution_time  | 0     |


  Scenario Outline: Importing unchanged googletest reports in detailed mode

      Testcases in googletest reports do not know the source file they come
      from. The plugin is able to fill this gap for a subset of testcases
      (currently those which use a fixture) using the 'lookup the classnames
      in the AST'-approach. This doesn't work for all testcases, though

      GIVEN the project "googletest_project"
      WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.reportPath=<reportpath> -Dsonar.cxx.xunit.provideDetails=true"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
              .*WARN.*cannot find the sources for '#include <unistd\.h>'
              """
          AND the test related metrics have following values: <values>

      Examples:
                                           # tests, failure, errors, skipped,
                                           # density, time
      | reportpath                         | values                             |

      # contains 'assignable' testcases only
      | gtest.xml                          | 3, 2, 0, 1, 33.3, 50               |

      # testcases in two testsuites which all have to be assigned to the same file
      | gtest_two_fixtures_in_one_file.xml | 3, 1, 0, 1, 66.7, 0                |


  Scenario Outline: Importing unchanged googletest reports in detailled mode, assigning fails

      see above... this is the case where it doesnt work

      GIVEN the project "googletest_project"
      WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.reportPath=<reportpath> -Dsonar.cxx.xunit.provideDetails=true"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
              .*WARN.*cannot find the sources for '#include <unistd\.h>'
              .*WARN.*no input file found, the testcase '.*' has to be skipped
              .*WARN.*Some testcases had to be skipped, check the relevant parts of your setup.*
              """
          AND the test related metrics have following values: <values>

      Examples:
                                           # tests, failure, errors, skipped,
                                           # density, time
      | reportpath                         | values                             |
      # no assignable testcases here
      | gtest_without_fixture.xml          | None, None, None, None, None, None |


  Scenario Outline: Importing unchanged googletest reports in detailled mode, sonar.tests unset

      Importing in detailled mode isnt possible unless sonar.tests is set

      GIVEN the project "googletest_project"
      WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.reportPath=<reportpath> -Dsonar.cxx.xunit.provideDetails=true -Dsonar.tests="
      THEN the analysis finishes successfully
          AND the analysis log contains a line matching:
              """
              .*ERROR - The property 'sonar.tests' is unset. Please set it to proceed
              """
          AND the test related metrics have following values: <values>

      Examples:
                                           # tests, failure, errors, skipped,
                                           # density, time
      | reportpath                         | values                             |
      | gtest.xml                          | None, None, None, None, None, None |


  Scenario Outline: Importing augmented test reports generated by googletest

      Test reports can be augmented with the filename-attribute which
      may help the plugin to assign the testcases in the report to the
      correct source code files.

      GIVEN the project "googletest_project"
      WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.reportPath=<reportpath>"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
              .*WARN.*cannot find the sources for '#include <unistd\.h>'
              """
          AND the following metrics have following values:
               | metric               | value |
               | tests                | 1     |
               | test_failures        | 0     |
               | test_errors          | 0     |
               | skipped_tests        | 0     |
               | test_success_density | 100   |
               | test_execution_time  | 0     |

      Examples:
        | reportpath              |
        | gtest_fname_in_ts.xml   | # filename-attribute in testsuite-tag
        | gtest_fname_in_tc.xml   | # filename in testcase-tag
        | gtest_fname_in_both.xml | # filename in both tags, the wrong one in testsuite


  Scenario Outline: googletest reports cannot be found or are empty
      GIVEN the project "googletest_project"
      WHEN I run "sonar-runner -Dsonar.cxx.xunit.reportPath=<reportpath>"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
              .*WARN.*cannot find the sources for '#include <unistd\.h>'
              .*WARN.*The report.*seems to be empty, ignoring\.
              .*WARN.*Cannot find a report for '.*'
              """
          AND the following metrics have following values:
              | metric               | value |
              | tests                | None  |
              | test_failures        | None  |
              | test_errors          | None  |
              | skipped_tests        | None  |
              | test_success_density | None  |
              | test_execution_time  | None  |

      Examples:
          | reportpath       |
          | notexistingpath  |
          | empty_report.xml |


   Scenario: googletest report is invalid
       GIVEN the project "googletest_project"
       WHEN I run "sonar-runner -Dsonar.cxx.xunit.reportPath=invalid_report.xml"
       THEN the analysis breaks
           AND the analysis log contains a line matching:
               """
               ERROR.*Cannot feed the data into SonarQube, details: .*
               """

  # =================
  # === boosttest ===
  # =================

  Scenario: Importing simple boosttest report in default mode

      By default, the plugin doesn't try to assign the testcases
      found in the report to test files in Sonar, it just sums up
      all the measures and assigns it to the project. This makes the
      procedure more stable but doesn't provide the details for the
      testcases in SonarQube, i.e. the drilldown wont be possible.

      GIVEN the project "boosttest_project"

      WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.reportPath=btest_test_simple-test_suite.xml"

      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages
          AND the following metrics have following values:
              | metric               | value |
              | tests                | 1     |
              | test_failures        | 0     |
              | test_errors          | 0     |
              | skipped_tests        | 0     |
              | test_success_density | 100   |
              | test_execution_time  | 0     |


  Scenario: Importing nested boosttest report in default mode

      Boost unit test framework supports nested testsuites.
      A testsuite is handled as a C++ namespace.
      Verify the support of nested testsuites.

      GIVEN the project "boosttest_project"

      WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.reportPath=btest_test_nested-test_suite.xml"

      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages
          AND the following metrics have following values:
              | metric               | value |
              | tests                | 4     |
              | test_failures        | 0     |
              | test_errors          | 4     |
              | skipped_tests        | 0     |
              | test_success_density | 0     |
              | test_execution_time  | 3     |

              
 Scenario: Simulate virtual unit test file (provideDetails, filename tag)

      Starting with SQ 4.2 virtual files are no more supported.
      With 'boosttest-1.x-to-junit-1.0-dummy.xsl' it is possible to simulate this feature again.
      The stylesheet set the filename tag to './cxx-xunit/dummy.cpp' additional the dummy
      cpp unittest file in the test folder is needed.

      GIVEN the project "boosttest_project"

      WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.xsltURL=boosttest-1.x-to-junit-dummy-1.0.xsl -Dsonar.tests=cxx-xunit -Dsonar.cxx.xunit.provideDetails=true -Dsonar.cxx.xunit.reportPath=btest_test_nested-test_suite.xml"

      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages
          AND the following metrics have following values:
              | metric               | value |
              | tests                | 4     |
              | test_failures        | 0     |
              | test_errors          | 4     |
              | skipped_tests        | 0     |
              | test_success_density | 0     |
              | test_execution_time  | 3     |
              
              
  Scenario Outline: Importing unchanged boosttest reports in detailed mode (filename tag)

      Testcases in boosttest reports with setting 'log_level=all' also
      the root filename of the testcase. Plugin is able to read
      filename tag and link corresponding test module.

      GIVEN the project "boosttest_project"

      WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.reportPath=<reportpath> -Dsonar.cxx.xunit.provideDetails=true"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages
          AND the test related metrics have following values: <values>

      Examples:
                                           # tests, failure, errors, skipped,
                                           # density, time
      | reportpath                         | values                             |

      # simple example
      | btest_test_simple-all.xml          | 1, 0, 0, 0, 100, 0                 |

      # with failure & errors
      | btest_test_component1-all.xml      | 3, 1, 1, 0, 33.3, 50002            |

      # with testsuite/sucess
      | btest_test_success-all.xml         | 1, 0, 0, 0, 100, 3                 |

      # with nested testsuites
      | btest_test_nested-all.xml          | 4, 0, 4, 0, 0, 5                   |

 
  Scenario Outline: Importing unchanged boosttest reports in detailed mode (AST)

      Testcases in boosttest reports with setting 'log_level=test_suite'
      do not know the source file they come from. The plugin is able
      to fill this gap for a subset of testcases using the
      'lookup the namespaces in the AST'-approach.

      GIVEN the project "boosttest_project"

      WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.reportPath=<reportpath> -Dsonar.cxx.xunit.provideDetails=true"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages
          AND the test related metrics have following values: <values>

      Examples:
                                           # tests, failure, errors, skipped,
                                           # density, time
      | reportpath                         | values                             |

      # simple example
      | btest_test_simple-test_suite.xml   | 1, 0, 0, 0, 100, 0                 |

      # with testsuite
      | btest_test_success-test_suite.xml  | 1, 0, 0, 0, 100, 1                 |

      # with nested testsuites
      | btest_test_nested-test_suite.xml   | 4, 0, 4, 0, 0, 5                   |

 
  Scenario: Test with real boost test framework

        Real boost test framework is a very complex usecase for preprocessor and parser.
        Test if plugin is able to handle this.

        GIVEN the project "boosttest_project"

        WHEN I run "sonar-runner -X -Dsonar.cxx.xunit.reportPath=btest_test_nested-test_suite.xml -Dsonar.cxx.xunit.provideDetails=true -Dsonar.cxx.includeDirectories=/usr/include"
        THEN the analysis finishes successfully
            AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '.*'
              """
            AND the following metrics have following values:
              | metric               | value |
              | tests                | 4     |
              | test_failures        | 0     |
              | test_errors          | 4     |
              | skipped_tests        | 0     |
              | test_success_density | 0     |
              | test_execution_time  | 3     |
