Feature: Importing Cppcheck reports
  As a CXX plug-in user, I want to import Cppcheck reports into SonarQube

  Scenario: The reports are missing
    Given the project "cppcheck_project"
    And rule "cppcheck:unusedVariable" is activated
    And rule "cppcheck:unreadVariable" is activated
    And rule "cppcheck:deallocDealloc" is activated
    And rule "cppcheck:doubleFree" is activated
    And rule "cppcheck:uninitvar" is activated
    And rule "cppcheck:unusedFunction" is activated
    When I run sonar-scanner with "-X -Dsonar.cxx.cppcheck.reportPaths=empty.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      WARN  The 'Cppcheck V2' report is empty.*skipping
      """
    And the number of violations fed is 0


  Scenario: The reports use paths relative to directories listed in sonar.sources
    Given the project "cppcheck_project"
    And rule "cppcheck:unusedVariable" is activated
    And rule "cppcheck:unreadVariable" is activated
    And rule "cppcheck:deallocDealloc" is activated
    And rule "cppcheck:doubleFree" is activated
    And rule "cppcheck:uninitvar" is activated
    And rule "cppcheck:unusedFunction" is activated
    When I run sonar-scanner with "-X -Dsonar.cxx.cppcheck.reportPaths=relative-to-src.xml"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      WARN  Cannot find the file 'component1\.cc'.*skipping
      """
    And the number of violations fed is 0


  Scenario: The reports and issues in the reports have absolute paths
    Given the project "cppcheck_with_absolute_paths_project"
    And rule "cppcheck:unusedVariable" is activated
    And rule "cppcheck:unreadVariable" is activated
    And rule "cppcheck:deallocDealloc" is activated
    And rule "cppcheck:doubleFree" is activated
    And rule "cppcheck:uninitvar" is activated
    And rule "cppcheck:unusedFunction" is activated
    And platform is not "Windows"
    When I run sonar-scanner with "-X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is 6


  Scenario Outline: The reports are invalid
    Given the project "cppcheck_project"
    And rule "cppcheck:unusedVariable" is activated
    And rule "cppcheck:unreadVariable" is activated
    And rule "cppcheck:deallocDealloc" is activated
    And rule "cppcheck:doubleFree" is activated
    And rule "cppcheck:uninitvar" is activated
    And rule "cppcheck:unusedFunction" is activated
    When I run sonar-scanner with "-X -Dsonar.cxx.cppcheck.reportPaths=<reportpaths>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      WARN  The 'Cppcheck V2' report is invalid.*skipping
      """
    And the number of violations fed is <violations>
    Examples:
      | reportpaths          | violations |
      | unparsable.xml      | 0          |
      | wrongly_encoded.xml | 0          |


  Scenario Outline: Importing Cppcheck reports
    Given the project "cppcheck_project"
    And rule "cppcheck:unusedVariable" is activated
    And rule "cppcheck:unreadVariable" is activated
    And rule "cppcheck:deallocDealloc" is activated
    And rule "cppcheck:doubleFree" is activated
    And rule "cppcheck:uninitvar" is activated
    And rule "cppcheck:unusedFunction" is activated
    When I run sonar-scanner with "-X -Dsonar.cxx.cppcheck.reportPaths=<reportpaths>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the analysis log contains no error/warning messages
    And the number of violations fed is <violations>
    Examples:
      | reportpaths      | violations |
      | cppcheck-v2.xml | 6          |


  Scenario Outline: Importing Cppcheck reports with issues in C and C++ language
    Given the project "cppcheck_project_c_cpp"
    And declared suffixes for cxx files to analyze are ".cpp,.cc,.hpp,.hh"
    And rule "cppcheck:unusedVariable" is activated
    And rule "cppcheck:unreadVariable" is activated
    And rule "cppcheck:deallocDealloc" is activated
    And rule "cppcheck:doubleFree" is activated
    And rule "cppcheck:uninitvar" is activated
    And rule "cppcheck:unusedFunction" is activated
    When I run sonar-scanner with "-X -Dsonar.cxx.cppcheck.reportPaths=<reportpaths>"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Preprocessor:.*
      """
    And the number of violations fed is <violations>
    Examples:
      | reportpaths      | violations |
      | cppcheck-v2.xml | 6          |
