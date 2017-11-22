@SqApi67
Feature: GoogleTestWithBullseyeAndVsProject
  This test verifies that analysis is able to import bullseye coverage reports and import custom rules reports.
  Custom rules are created using Rest API, after test ends rules are deleted.
  Bullseye reports need to be created before running the test.

  Scenario: GoogleTestWithBullseyeAndVsProject
    Given the project "googletest_bullseye_vs_project"
    And rule "rats:getenv" is enabled
    And rule "cpplint_legal_copyright_0" is created based on "other:CustomRuleTemplate" in repository "other"
    And rule "cpplint_build_header_guard_0" is created based on "other:CustomRuleTemplate" in repository "other"
    And rule "cpplint_whitespace_indent_2" is created based on "other:CustomRuleTemplate" in repository "other"
    And rule "cpplint_whitespace_parens_5" is created based on "other:CustomRuleTemplate" in repository "other"
    And rule "cpplint_whitespace_line_length_1" is created based on "other:CustomRuleTemplate" in repository "other"
    And rule "cpplint_tekla_custom_include_files_0" is created based on "other:CustomRuleTemplate" in repository "other"
    When I run "sonar-scanner -X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      .*WARN.*to create a dependency with 'PathHandling/PathHandle.h'
      .*WARN.*cannot find the sources for '#include <unistd\.h>'
      .*WARN.*Cannot find the file '.*gtestmock.1.7.2.*', ignoring coverage measures
      .*WARN.*Cannot find a report for '.*'
      .*WARN.*cannot find the sources for '#include.*
      """
    And the following metrics have following values:
      | metric                   | value |
      | ncloc                    | 24    |
      | lines                    | 42    |
      | statements               | 7     |
      | classes                  | 1     |
      | files                    | 2     |
      | directories              | 1     |
      | functions                | 3     |
      | comment_lines_density    | 0     |
      | duplicated_lines_density | 0     |
      | duplicated_lines         | 0     |
      | duplicated_blocks        | 0     |
      | duplicated_files         | 0     |
      | complexity               | 4     |
      | file_complexity          | 2.0   |
      | violations               | 19    |
      | line_coverage            | 100   |
      | branch_coverage          | 50    |
      | test_failures            | 1     |
      | test_errors              | 0     |
      | tests                    | 2     |
    And delete created rule other:cpplint_build_header_guard_0
    And delete created rule other:cpplint_legal_copyright_0
    And delete created rule other:cpplint_whitespace_indent_2
    And delete created rule other:cpplint_whitespace_parens_5
    And delete created rule other:cpplint_whitespace_line_length_1
    And delete created rule other:cpplint_tekla_custom_include_files_0
