Feature: GoogleTestWithBullseyeAndVsProject

  This is just for running a smoketest using a somewhat more complex testdata.

  Scenario: GoogleTestWithBullseyeAndVsProject
      GIVEN the project "googletest_bullseye_vs_project"
        and rule "rats:getenv" is enabled
        and rule "cpplint_legal_copyright_0" is created based on "other:CustomRuleTemplate" in repository "other"
        and rule "cpplint_build_header_guard_0" is created based on "other:CustomRuleTemplate" in repository "other"
        and rule "cpplint_whitespace_indent_2" is created based on "other:CustomRuleTemplate" in repository "other"
        and rule "cpplint_whitespace_parens_5" is created based on "other:CustomRuleTemplate" in repository "other"
        and rule "cpplint_whitespace_line_length_1" is created based on "other:CustomRuleTemplate" in repository "other"
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
               | violations               | 18    |
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
        THEN delete created rule other:cpplint_build_header_guard_0
            AND delete created rule other:cpplint_legal_copyright_0
            AND delete created rule other:cpplint_whitespace_indent_2
            AND delete created rule other:cpplint_whitespace_parens_5
            AND delete created rule other:cpplint_whitespace_line_length_1
