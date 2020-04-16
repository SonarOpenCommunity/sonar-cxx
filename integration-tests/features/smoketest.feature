@SqApi79
Feature: Smoketest

  This is just for running a smoketest using a somewhat more complex testdata.
  Will be reworked later.

  Scenario: Smoketest
    Given the project "smoketest_project"
    And rule "cppcheck:unusedVariable" is enabled
    And rule "cppcheck:unreadVariable" is enabled
    And rule "cppcheck:deallocDealloc" is enabled
    And rule "cppcheck:doubleFree" is enabled
    And rule "cppcheck:uninitvar" is enabled
    And rule "cppcheck:unusedFunction" is enabled
    And rule "cppcheck:missingInclude" is enabled
    When I run "sonar-scanner -X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
      .*WARN.*cannot find the sources for '#include <iostream>'
      .*WARN.*Cannot find the file '.*component_XXX.cc', skipping
      .*WARN.*Cannot find a report for '.*'
      """
    And the following metrics have following values:
      | metric                         | value |
      | ncloc                          | 56    |
      | lines                          | 151   |
      | statements                     | 36    |
      | classes                        | 1     |
      | files                          | 8     |
      | directories                    | None  |
      | functions                      | 5     |
      | comment_lines_density          | 30    |
      | comment_lines                  | 24    |
      | duplicated_lines_density       | 55.6  |
      | duplicated_lines               | 84    |
      | duplicated_blocks              | 2     |
      | duplicated_files               | 2     |
      | complexity                     | 7     |
      | cognitive_complexity           | 2     |
      | file_complexity                | 0.9   |
      | violations                     | 12    |
      | lines_to_cover                 | 31    |
      | coverage                       | 53.8  |
      | line_coverage                  | 54.8  |
      | branch_coverage                | 50    |
      | uncovered_conditions           | 4     |
      | uncovered_lines                | 14    |
      | tests                          | 5     |
      | test_failures                  | 2     |
      | test_errors                    | 0     |
      | skipped_tests                  | 1     |
      | test_execution_time            | 159   |
      | test_success_density           | None  |
      | false_positive_issues          | 0     |
      | open_issues                    | 12    |
      | confirmed_issues               | 0     |
      | reopened_issues                | 0     |
      | code_smells                    | 6     |
      | sqale_index                    | 30    |
      | sqale_debt_ratio               | 1.8   |
      | bugs                           | 6     |
      | reliability_remediation_effort | 30    |
      | vulnerabilities                | 0     |
      | security_remediation_effort    | 0     |
      | security_hotspots              | 0     |



