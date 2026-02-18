Feature: Importing coverage data
  As a CXX Plugin user, I want to import coverage test reports into SonarQube

  Scenario: Importing coverage reports
    Given the project "coverage_project"
    When I run sonar-scanner with following options:
      """
      -Dsonar.cxx.cobertura.reportPaths=coverage1.xml,coverage2.xml,coverage3.xml
      """
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*cannot find the sources for '#include <iostream>'
      .*WARN.*Preprocessor:.*
      """
    And the following metrics have following values:
      | metric                  | value |
      | coverage                | 31.0  |
      | line_coverage           | 25.0  |
      | branch_coverage         | 50    |


  Scenario: Merging Corbertura coverage reports
    Given the project "coverage_merge_project"
    When I run sonar-scanner with "-X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages
    And the following metrics have following values:
      | metric                  | value |
      | coverage                | 94.4  |
      | line_coverage           | 100.0 |
      | branch_coverage         | 83.3  |


  Scenario: Zero coverage measures without coverage reports
    If we don't pass coverage reports all coverage measures, except the branch
    ones, should be 'zero'. The branch coverage measures remain 'None'

    Given the project "coverage_project"
    When I run sonar-scanner with following options:
      """
      -Dsonar.cxx.cobertura.reportPaths=dummy.xml
      """
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*cannot find the sources for '#include <iostream>'
      .*WARN.*Property 'sonar.cxx.cobertura.reportPaths': cannot find any files.*
      .*WARN.*Preprocessor:.*
      """
    And the following metrics have following values:
      | metric                  | value |
      | coverage                | 0     |
      | line_coverage           | 0     |
      | branch_coverage         | None  |
