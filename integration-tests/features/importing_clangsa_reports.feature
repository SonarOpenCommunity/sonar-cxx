@SqApi67
Feature: Importing Clang Static Analyzer reports
  As a SonarQube user,
  I want to import the Clang Static Analyzer reports into SonarQube.
  In order to have all static code checking results in one place,
  work with them, filter them etc. and derive metrics from them.

  Scenario: The Clang reports are missing
    Given the project "clangsa_project"
    When I run "sonar-scanner -X -Dsonar.cxx.clangsa.reportPath=empty.plist"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is 0

  Scenario Outline: Importing Clang Static Analyzer report(s)
    Given the project "clangsa_project"
    And rule "ClangSA:core.DivideZero" is enabled
    And rule "ClangSA:deadcode.DeadStores" is enabled
    When I run "sonar-scanner -X -Dsonar.cxx.clangsa.reportPath=<reportpath>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is <violations>
    Examples:
      | reportpath      | violations |
      | divzero.plist   | 1          |
      | unused.plist    | 2          |

  Scenario Outline: The Clang reports are invalid
    Given the project "clangsa_project"
    When I run "sonar-scanner -X -Dsonar.cxx.clangsa.reportPath=<reportpath>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is <violations>
    Examples:
      | reportpath          | violations |
      | unparsable.plist    | 0          |
      | wrongformat.plist   | 0          |
