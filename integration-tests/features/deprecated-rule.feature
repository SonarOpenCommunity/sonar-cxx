Feature: Importing reports with deprecated rules
  As a CXX plug-in user, I want to import a report with a deprecated rule.

  Scenario: Successor of deprecated rule is active, the deprecated rule should be mapped to the successor.
    Given the project "deprecated_rule"
    And rule "clangtidy:clang-diagnostic-c++23-extensions" is activated
    When I run sonar-scanner with "-X -Dsonar.cxx.clangtidy.reportPaths=deprecated.txt"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      Downloading rules for 'clangtidy' from server
      """
    And the analysis log contains a line matching
      """
      rules for 'clangtidy' were loaded from server
      """
    And the analysis log contains a line matching
      """
      Map deprecated rule 'clang-diagnostic-c\+\+2b-extensions' to 'clang-diagnostic-c\+\+23-extensions' for 'clangtidy'
      """
    And the number of violations fed is 1
