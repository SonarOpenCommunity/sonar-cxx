Feature: Samples

  This is for testing the sample projects.

  Scenario: cxx plugin hello world project
    Given the project "hello_world"
    And rule "cppcheck:unusedVariable" is activated
    When I run sonar-scanner with "-X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the following metrics have following values:
      | metric     | value |
      | ncloc      | 5     |
      | lines      | 8     |
      | files      | 1     |
      | functions  | 1     |
      | violations | 1     |
