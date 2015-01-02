Feature: GoogleTestWithBullseyeAndVsProject

  This is just for running a smoketest using a somewhat more complex testdata.

  Scenario: GoogleTestWithBullseyeAndVsProject
      GIVEN the project "googletest_bullseye_vs_project"
        and rule "rats:getenv" is enabled in project
      WHEN I run "sonar-runner -X"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*to create a dependency with 'PathHandling/PathHandle.h'
              .*WARN.*cannot find the sources for '#include <unistd\.h>'
              .*WARN.*Cannot find the file '.*gtestmock.1.7.2.*', ignoring coverage measures
              .*WARN.*cannot find the sources for.* 
              .*WARN.*syntax error, skip.*
              
              """
          AND the following metrics have following values:
               | metric                   | value |
               # size metrics
               | ncloc                    | 24    |
               | lines                    | 42   |
               | statements               | 7     |
               | classes                  | 1     |
               | files                    | 2     |
               | directories              | 2     |
               | functions                | 3     |
               # comments / documentation
               | comment_lines_density    | 0     |
               # duplications
               | duplicated_lines_density | 0     |
               | duplicated_lines         | 0     |
               | duplicated_blocks        | 0     |
               | duplicated_files         | 0     |
               # complexity
               | complexity               | 4     |
               | function_complexity      | 1.3   |
               | file_complexity          | 2.0   |
               | class_complexity         | 4     |
               # violations
               | violations               | 34    |
               # coverage statistics
               | coverage                 | 88.9  |
               | line_coverage            | 100   |
               | branch_coverage          | 50    |
               # design/tangles
               | package_tangle_index     | 0     |
               | package_tangles          | 0     |
               # test execution statistics
               | test_success_density     | 50    |
               | test_failures            | 1     |
               | test_errors              | 0     |
               | tests                    | 2     |
