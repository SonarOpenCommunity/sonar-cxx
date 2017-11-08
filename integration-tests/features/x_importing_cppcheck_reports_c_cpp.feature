@SqApi67
Feature: Importing Cppcheck ANSI-C reports

  Scenario Outline: Importing cppcheck issues when c language issues are in report.
    Given the project "cppcheck_project_c_cpp"
    And declared header extensions of language c++ are ".hpp,.hh"
    And declared source extensions of language c++ are ".cpp,.cc"
    When I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=<reportpath>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      """
    And the number of violations fed is <violations>
    Examples:
      | reportpath      | violations |
      | cppcheck-v2.xml | 6          |

