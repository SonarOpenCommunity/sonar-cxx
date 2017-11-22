@SqApi67
Feature: cpp-multimodule-project
  Test multimodule project with reports at root of the project

  Scenario: cpp-multimodule-project
    Given the project "cpp-multimodule-project"
    When I run "sonar-scanner -X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
      .*WARN.*cannot find the sources for '#include <iostream>'
      .*WARN-*Cannot find the file '.*', skipping violations
      .*WARN.*to create a dependency with .*
      .*WARN.*the include root '.*' doesn't exist
      .*WARN.* cannot find the sources for .*
      .*WARN.*SCM provider autodetection failed.*
      .*WARN.*Cannot find a report for '.*'
      .*WARN.*File access Failed '.*'
      .*ERROR.*Invalid report baseDir '.*'
      .*ERROR.*Using module base failed to find Path '.*'
      """
    And the following metrics have following values:
      | metric                   | value |
      | ncloc                    | 56    |
      | lines                    | 150   |
      | statements               | 36    |
      | classes                  | 1     |
      | files                    | 8     |
      | directories              | 4     |
      | functions                | 5     |
      | comment_lines_density    | 30    |
      | comment_lines            | 24    |
      | duplicated_lines_density | 56.0  |
      | duplicated_lines         | 84    |
      | duplicated_blocks        | 2     |
      | duplicated_files         | 2     |
      | complexity               | 7     |
      | file_complexity          | 0.9   |
      | violations               | 28    |
      | test_failures            | 2     |
      | skipped_tests            | 1     |
      | test_errors              | 0     |
      | tests                    | 3     |

