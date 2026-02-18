Feature: Importing Clang-Tidy reports
  As a CXX plug-in user, I want to import Clang-Tidy reports into SonarQube

  Scenario Outline: Importing Clang-Tidy reports
    Given the project "clangtidy_project"
    And rule "clangtidy:hicpp-signed-bitwise" is activated
    When I run sonar-scanner with "-X -Dsonar.cxx.clangtidy.reportPaths=<reportpaths>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the analysis log contains no error/warning messages
    And the number of violations fed is <violations>
    Examples:
      | reportpaths | violations |
      | column.txt  | 4          |
