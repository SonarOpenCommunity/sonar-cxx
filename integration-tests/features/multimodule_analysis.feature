Feature: cpp-multimodule-project

  Test multimodule project with reports at root of the project

  Scenario: cpp-multimodule-project
      GIVEN the project "cpp-multimodule-project"
      WHEN I run "sonar-runner -X"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
              .*WARN.*cannot find the sources for '#include <iostream>'
              .*WARN  - Cannot find the file '.*', skipping violations
              .*WARN.*to create a dependency with .*
              """
          AND the following metrics have following values:
               | metric                   | value |
               # size metrics
               | ncloc                    | 56    |
               | lines                    | 148   |
               | statements               | 36    |
               | classes                  | 1     |
               | files                    | 8     |
               | directories              | 4     |
               | functions                | 5     |
               # comments / documentation
               | comment_lines_density    | 30    |
               | comment_lines            | 24    |
               # duplications
               | duplicated_lines_density | 58.1  |
               | duplicated_lines         | 86    |
               | duplicated_blocks        | 2     |
               | duplicated_files         | 2     |
               # complexity
               | complexity               | 7     |
               | function_complexity      | 1.4   |
               | file_complexity          | 0.9   |
               | class_complexity         | 6     |
               # violations
               | violations               | 28    |
               # test execution statistics
               | test_success_density     | 33.3  |
               | test_failures            | 2     |
               | skipped_tests            | 1     |
               | test_errors              | 0     |
               | tests                    | 3     |

