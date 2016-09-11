Feature: Smoketest

  This is just for running a smoketest using a somewhat more complex testdata.
  Will be reworked later.

  Scenario: Smoketest
      GIVEN the project "smoketest_project"
      WHEN I run "sonar-scanner -X"
      THEN the analysis finishes successfully
          AND the analysis in server has completed
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*Unable to get a valid mac address, will use a dummy address
              .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
              .*WARN.*cannot find the sources for '#include <iostream>'
              .*WARN.*Cannot find the file '.*component_XXX.cc', skipping violations
              .*WARN.*Cannot find a report for '.*'
              .*WARN.*Already created edge from 'src/cli/main.cc'.*
              """
          AND the following metrics have following values:
               | metric                   | value |
               # size metrics
               | ncloc                    | 56    |
               | lines                    | 151   |
               | statements               | 36    |
               | classes                  | 1     |
               | files                    | 8     |
               | directories              | 4     |
               | functions                | 5     |
               # comments / documentation
               | comment_lines_density    | 30    |
               | comment_lines            | 24    |
               # duplications
               | duplicated_lines_density | 55.6  |
               | duplicated_lines         | 84    |
               | duplicated_blocks        | 2     |
               | duplicated_files         | 2     |
               # complexity
               | complexity               | 7     |
               | function_complexity      | 1.4   |
               | file_complexity          | 0.9   |
               | class_complexity         | 6     |
               # violations
               | violations               | 12    |
               # coverage statistics = with 5.6 there are plenty of differences because unit tests are not imported to the statistics. and the current coverage data does not match the files. todo create proper coverage data project
               | coverage                 | 81.8  |
               | line_coverage            | 100.0  |
               | branch_coverage          | 50    |
               | it_coverage              | 81.8  |
               | it_line_coverage         | 100.0  |
               | it_branch_coverage       | 50    |
               | overall_coverage         | 81.8  |
               | overall_line_coverage    | 100.0  |
               | overall_branch_coverage  | 50    |
               # test execution statistics
               #| test_success_density     | 50    | -> enable when this is restored in core
               | test_failures            | 2     |
               | test_errors              | 0     |
               | tests                    | 4     |
