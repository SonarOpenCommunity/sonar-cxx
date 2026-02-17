Feature: Importing Infer reports
  As a CXX plug-in user, I want to import Infer reports into SonarQube

  Scenario: The Infer report is empty
    Given the project "infer_project"
    And rule "infer:NULL_DEREFERENCE" is activated
    And rule "infer:DEAD_STORE" is deactivated
    When I run sonar-scanner with "-X -Dsonar.cxx.infer.reportPaths=empty.json"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      WARN  The 'Infer JSON' report is empty.*skipping
      """
    And the number of violations fed is 0

  Scenario: The Infer report is invalid
    Given the project "infer_project"
    And rule "infer:NULL_DEREFERENCE" is activated
    And rule "infer:DEAD_STORE" is deactivated
    When I run sonar-scanner with "-X -Dsonar.cxx.infer.reportPaths=invalid.json"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      WARN  The 'Infer JSON' report is invalid.*skipping
      """
    And the number of violations fed is 0

  Scenario: The Infer report refer to an invalid file
    Given the project "infer_project"
    And rule "infer:NULL_DEREFERENCE" is activated
    And rule "infer:DEAD_STORE" is deactivated
    When I run sonar-scanner with "-X -Dsonar.cxx.infer.reportPaths=invalid_file.json"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      WARN  Cannot find the file 'src/invalid\.cc' in project.*skipping
      """
    And the number of violations fed is 0

  Scenario: The Infer reports use relative paths to read all issues of the report
    Given the project "infer_project"
    And rule "infer:NULL_DEREFERENCE" is activated
    And rule "infer:DEAD_STORE" is deactivated
    When I run sonar-scanner with "-X -Dsonar.cxx.infer.reportPaths=infer.json"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    But the analysis log contains a line matching
      """
      INFO  Processing successful, saved new issues=2
      """    
    And the number of violations fed is 1
