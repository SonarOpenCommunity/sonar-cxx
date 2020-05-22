@SqApi79
Feature: Importing coverage data
  As a CXX Plugin user, I want to import coverage test reports into SonarQube

  Scenario: Importing coverage reports
    Given the project "coverage_project"
    When I run sonar-scanner with following options:
      """
      -Dsonar.cxx.coverage.reportPaths=ut-coverage.xml,it-coverage.xml,overall-coverage.xml
      """
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      .*WARN.*cannot find the sources for '#include <iostream>'
      """
    And the following metrics have following values:
      | metric                  | value |
      | coverage                | 31.0  |
      | line_coverage           | 25.0  |
      | branch_coverage         | 50    |


  Scenario: Zero coverage measures without coverage reports
    If we don't pass coverage reports all coverage measures, except the branch
    ones, should be 'zero'. The branch coverage measures remain 'None'

    Given the project "coverage_project"
    When I run sonar-scanner with following options:
      """
      -Dsonar.cxx.coverage.reportPaths=dummy.xml
      """
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      .*WARN.*cannot find the sources for '#include <iostream>'
      .*WARN.*Cannot find a report for '.*'
      """
    And the following metrics have following values:
      | metric                  | value |
      | coverage                | 0     |
      | line_coverage           | 0     |
      | branch_coverage         | None  |
