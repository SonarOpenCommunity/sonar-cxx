Feature: Importing Cppcheck reports
  As a SonarQube user,
  I want to import the Cppcheck reports into SonarQube
  In order to have all static code checking results in one place,
  work with them, filter them etc. and derive metrics from them.

  @SqApi67
  Scenario: The reports are missing
    Given the project "cppcheck_project"
    When I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=empty.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      .*WARN.*The report '.*' seems to be empty, ignoring.
      """
    And the number of violations fed is 0

  @wip
  Scenario: The report mentions an unknown rule
    Given the project "cppcheck_project"
    When I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=rule_unknown.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      .*ERROR.*Could not add the issue.*The rule '.*' does not exist.
      """
    And the number of violations fed is 0

  @SqApi67
  Scenario: The reports use paths relative to directories listed in sonar.sources
    Given the project "cppcheck_project"
    When I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=relative-to-src.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      .*WARN.*Cannot find the file .* skipping violations
      """
    And the number of violations fed is 0

  @SqApi67
  Scenario: The reports and issues in the reports have absolute paths
    Given the project "cppcheck_with_absolute_paths_project"
    And platform is not "Windows"
    When I run "sonar-scanner -X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is 6

  @SqApi67
  Scenario Outline: The reports are invalid
    Given the project "cppcheck_project"
    When I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=<reportpath>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      .*ERROR.*Report .* cannot be parsed
      """
    And the number of violations fed is <violations>
    Examples:
      | reportpath          | violations |
      | unparsable.xml      | 0          |
      | wrongly_encoded.xml | 0          |

  @SqApi67
  Scenario Outline: Importing Cppcheck reports
    Given the project "cppcheck_project"
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
      | cppcheck-v1.xml | 6          |
      | cppcheck-v2.xml | 6          |


   # This doesnt work. We dont support reports outside of the projects directory,
   # although there is no good reason for that(??)
   #
   # Scenario: The reports are outside the project directory
   #   Given the project "cppcheck_project"
   #       And a report outside the projects directory, e.g. "/tmp/cppcheck-v1.xml"
   #   When I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=/tmp/cppcheck-v1.xml"
   #   Then the analysis finishes successfully
   #       And the analysis in server has completed
   #       And the server log (if locatable) contains no error/warning messages
   #       And the analysis log contains no error/warning messages
   #       And the number of violations fed is 7

   # TOTEST:
   # - behaviour on windows (windows paths in the reports,
   # windows paths in the config pattern...)
   # - custom rules scenario (precondition: we're able to register such a rule automaticly)
