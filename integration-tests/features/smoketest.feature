Feature: Smoketest

  This is just for running a smoketest using a somewhat more complex testdata.
  Will be reworked later.

  Scenario: Smoketest
      GIVEN the project "smoketest_project"
      WHEN I run "sonar-runner"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
              .*WARN.*cannot find the sources for '#include <iostream>'
              .*WARN  - Cannot find the file '.*component_XXX.cc', skipping violations
              """
          AND the following metrics have following values:
               | metric                   | value |
               # size metrics
               | ncloc                    | 56    |
               | lines                    | 148   |
               | statements               | 36    |
               | classes                  | 1     |
               | files                    | 8     |
               | directories              | 5     |
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
               | violations               | 12    |
               # coverage statistics
               | coverage                 | 41.2  |
               | line_coverage            | 39.5  |
               | branch_coverage          | 50    |
               | it_coverage              | 84    |
               | it_line_coverage         | 100   |
               | it_branch_coverage       | 50    |
               | overall_coverage         | 84    |
               | overall_line_coverage    | 100   |
               | overall_branch_coverage  | 50    |
               # design/tangles
               | package_tangle_index     | 66.7  |
               | package_tangles          | 1     |
               # test execution statistics
               | test_success_density     | 50    |
               | test_failures            | 2     |
               | test_errors              | 0     |
               | tests                    | 4     |
