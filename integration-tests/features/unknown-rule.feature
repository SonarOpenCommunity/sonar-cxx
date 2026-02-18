Feature: Importing reports with unknown rules
  As a CXX plug-in user, I want to import a report with an unknown rule.
  Depending on the "unused" rule is active or not the rule should be reported or irgnored.

  Scenario: sensor is not active and no rules are loaded from the server.
    Given the project "unknown_rule"
    And rule "cppcheck:unusedVariable" is deactivated    
    And rule "cppcheck:unknown" is deactivated
    When I run sonar-scanner with "-X -Dsonar.cxx.cppcheck.reportPaths=unknown_rule.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      'CXX Cppcheck report import' skipped because there is no related rule activated in the quality profile
      """
    And the number of violations fed is 0
    
  Scenario: sensor is active but 'unknown' rule is not activated.
    Given the project "unknown_rule"
    And rule "cppcheck:unusedVariable" is activated
    And rule "cppcheck:unknown" is deactivated
    When I run sonar-scanner with "-X -Dsonar.cxx.cppcheck.reportPaths=unknown_rule.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      Rule mapping to 'cppcheck:unknown' is not active
      """
    And the number of violations fed is 1
    
  Scenario: 'unknown' rule is active, the rule mapping is active and rules are loaded from the server.
    Given the project "unknown_rule"
    And rule "cppcheck:unusedVariable" is activated    
    And rule "cppcheck:unknown" is activated
    When I run sonar-scanner with "-X -Dsonar.cxx.cppcheck.reportPaths=unknown_rule.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      Downloading rules for 'cppcheck' from server
      """
    And the analysis log contains a line matching
      """
      rules for 'cppcheck' were loaded from server
      """
    And the analysis log contains a line matching
      """
      Rule 'id_of_a_rule_unknown_to_the_server' is unknown in 'cppcheck' and will be mapped to 'unknown'
      """
    And the number of violations fed is 2
