Feature: Importing compiler reports
  As a CXX plug-in user, I want to import LOG files from compilers into SonarQube

  Scenario: Import a VisualStudio LOG file with warnings
    Given the project "vs_project"
    And rule "compiler-vc:C4189" is activated
    And rule "compiler-vc:C4457" is activated
    And platform is "Windows"
    When I run sonar-scanner with "-X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the server log (if locatable) contains no error/warning messages
    And the number of violations fed is 2
