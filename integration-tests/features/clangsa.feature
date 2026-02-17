Feature: Importing Clang Static Analyzer reports
  As a CXX plug-in user, I want to import Cppcheck reports into SonarQube

  Scenario: Clang Static Analyzer reports are missing
    Given the project "clangsa_project"
    When I run sonar-scanner with "-X -Dsonar.cxx.clangsa.reportPaths=empty.plist"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is 0

  Scenario Outline: Importing Clang Static Analyzer report(s)
    Given the project "clangsa_project"
    And rule "clangsa:core.DivideZero" is activated
    And rule "clangsa:deadcode.DeadStores" is activated
    When I run sonar-scanner with "-X -Dsonar.cxx.clangsa.reportPaths=<reportpaths>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is <violations>
    Examples:
      | reportpaths      | violations |
      | divzero.plist   | 1          |
      | unused.plist    | 2          |


  Scenario Outline: Importing Clang Static Analyzer report(s) generated with scan-build
    Given the project "clangsa_scanbuild_project"
    And rule "clangsa:core.DivideZero" is activated
    And rule "clangsa:deadcode.DeadStores" is activated
    When I run sonar-scanner with "-X -Dsonar.cxx.clangsa.reportPaths=<reportpaths>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is <violations>
    Examples:
      | reportpaths                   | violations |
      | analyzer_reports/*/*.plist   | 2          |


  Scenario Outline: Clang Static Analyzer reports are invalid
    Given the project "clangsa_project"
    When I run sonar-scanner with "-X -Dsonar.cxx.clangsa.reportPaths=<reportpaths>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is <violations>
    Examples:
      | reportpaths          | violations |
      | unparsable.plist    | 0          |
      | wrongformat.plist   | 0          |
