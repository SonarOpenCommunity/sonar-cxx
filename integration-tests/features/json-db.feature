Feature: JSON Compilation Database support

  As a CXX Plugin user, I want to use the JSON Compilation Database to analyze SonarQube projects

  Scenario: Analyze only the source files contained in the JSON Compilation Database (case insensitive filesystem).
    Given the project "json_db_project_case_insensitive"
    And platform is "Windows"
    When I run sonar-scanner with "-X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*ERROR.*preprocessor:.*
      """
    And the following metrics have following values:
      | metric     | value |
      | ncloc      | 9     |
      | lines      | 24    |
      | statements | 3     |
      | functions  | 3     |

  Scenario: Analyze only the source files contained in the JSON Compilation Database (case sensitive filesystem).
    Given the project "json_db_project_case_sensitive"
    And platform is not "Windows"
    When I run sonar-scanner with "-X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*ERROR.*preprocessor:.*
      """
    And the following metrics have following values:
      | metric     | value |
      | ncloc      | 9     |
      | lines      | 24    |
      | statements | 3     |
      | functions  | 3     |
