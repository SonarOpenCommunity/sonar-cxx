Feature: Indexing files

  Test indexing of files depending on language and file extension settings

  Scenario: CXX file suffixes
    Given the project "indexing_project"
    When I run sonar-scanner with "-X -Dsonar.cxx.file.suffixes=.cc"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the following metrics have following values:
      | metric                   | value |
      | files                    | 3     |


  Scenario: Turn CXX language off
    Given the project "indexing_project"
    When I run sonar-scanner with "-X -Dsonar.cxx.file.suffixes=-"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the following metrics have following values:
      | metric                   | value |
      | files                    | None  |
