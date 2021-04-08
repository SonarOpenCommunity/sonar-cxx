@SqApi79
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


  Scenario: Verify macro propagation
    Read cacsaced include files. This works only if macros are
    correct propagated from one include level to the next.

    Given the project "macro_propagation_project"
    When I run sonar-scanner with "-X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      """
    And the following metrics have following values:      
      | metric                   | value |
      | ncloc                    | 9     |
      | lines                    | 40    |
      | statements               | 4     |
      | classes                  | 0     |
      | files                    | 3     |
      | functions                | 2     |
