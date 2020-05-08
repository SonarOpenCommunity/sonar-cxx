@SqApi79
Feature: Importing Cppcheck reports
  As a CXX plug-in user, I want to import Cppcheck reports into SonarQube

  Scenario: The reports are missing
    Given the project "cppcheck_project"
    And rule "cppcheck:unusedVariable" is enabled
    And rule "cppcheck:unreadVariable" is enabled
    And rule "cppcheck:deallocDealloc" is enabled
    And rule "cppcheck:doubleFree" is enabled
    And rule "cppcheck:uninitvar" is enabled
    And rule "cppcheck:unusedFunction" is enabled
    When I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=empty.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      .*WARN.*The report '.*' seems to be empty, ignoring.
      """
    And the number of violations fed is 0


  Scenario: The reports use paths relative to directories listed in sonar.sources
    Given the project "cppcheck_project"
    And rule "cppcheck:unusedVariable" is enabled
    And rule "cppcheck:unreadVariable" is enabled
    And rule "cppcheck:deallocDealloc" is enabled
    And rule "cppcheck:doubleFree" is enabled
    And rule "cppcheck:uninitvar" is enabled
    And rule "cppcheck:unusedFunction" is enabled
    When I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=relative-to-src.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      .*WARN.*Cannot find the file .* skipping
      """
    And the number of violations fed is 0


  Scenario: The reports and issues in the reports have absolute paths
    Given the project "cppcheck_with_absolute_paths_project"
    And rule "cppcheck:unusedVariable" is enabled
    And rule "cppcheck:unreadVariable" is enabled
    And rule "cppcheck:deallocDealloc" is enabled
    And rule "cppcheck:doubleFree" is enabled
    And rule "cppcheck:uninitvar" is enabled
    And rule "cppcheck:unusedFunction" is enabled
    And platform is not "Windows"
    When I run "sonar-scanner -X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is 6


  Scenario Outline: The reports are invalid
    Given the project "cppcheck_project"
    And rule "cppcheck:unusedVariable" is enabled
    And rule "cppcheck:unreadVariable" is enabled
    And rule "cppcheck:deallocDealloc" is enabled
    And rule "cppcheck:doubleFree" is enabled
    And rule "cppcheck:uninitvar" is enabled
    And rule "cppcheck:unusedFunction" is enabled
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


  Scenario Outline: Importing Cppcheck reports
    Given the project "cppcheck_project"
    And rule "cppcheck:unusedVariable" is enabled
    And rule "cppcheck:unreadVariable" is enabled
    And rule "cppcheck:deallocDealloc" is enabled
    And rule "cppcheck:doubleFree" is enabled
    And rule "cppcheck:uninitvar" is enabled
    And rule "cppcheck:unusedFunction" is enabled
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


  Scenario Outline: Importing Cppcheck reports with issues in C and C++ language
    Given the project "cppcheck_project_c_cpp"
    And declared suffixes for cxx files to analyze are ".cpp,.cc,.hpp,.hh"
    And rule "cppcheck:unusedVariable" is enabled
    And rule "cppcheck:unreadVariable" is enabled
    And rule "cppcheck:deallocDealloc" is enabled
    And rule "cppcheck:doubleFree" is enabled
    And rule "cppcheck:uninitvar" is enabled
    And rule "cppcheck:unusedFunction" is enabled
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
