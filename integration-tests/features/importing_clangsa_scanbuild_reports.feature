@SqApi67
Feature: Importing Clang Static Analyzer scan-build reports

  As a SonarQube user,
  I want to import the Clang Static Analyzer reports into SonarQube,
  to analyze my project I've used scan-build.
  In order to have all static code checking results in one place,
  work with them, filter them etc. and derive metrics from them.

  Scenario Outline: Importing Clang Static Analyzer report(s) generated with scan-build
    Given the project "clangsa_scanbuild_project"
    And rule "ClangSA:core.DivideZero" is enabled
    And rule "ClangSA:deadcode.DeadStores" is enabled
    When I run "sonar-scanner -X -Dsonar.cxx.clangsa.reportPath=<reportpath>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is <violations>

    Examples:
      | reportpath                   | violations |
      | analyzer_reports/*/*.plist   | 2          |
