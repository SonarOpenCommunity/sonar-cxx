Feature: Importing Cppcheck reports

  As a SonarQube user
  I want to import the Cppcheck reports into SonarQube
  In order to have all static code checking results in one place,
     work with them, filter them etc. and derive metrics from them.


  Scenario Outline: Importing Cppcheck report(s)
    GIVEN the project "cppcheck_project"
    WHEN I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=<reportpath>"
    THEN the analysis finishes successfully
         AND the analysis in server has completed
         AND the server log (if locatable) contains no error/warning messages
         AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*Unable to get a valid mac address, will use a dummy address
              """
         AND the number of violations fed is <violations>

    Examples:
      | reportpath      | violations |
      | cppcheck-v1.xml | 6          | # XML version 1
      | cppcheck-v2.xml | 6          | # XML version 2


   Scenario: The reports are missing
     GIVEN the project "cppcheck_project"
     WHEN I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=empty.xml"
     THEN the analysis finishes successfully
         AND the analysis in server has completed
         AND the server log (if locatable) contains no error/warning messages
         BUT the analysis log contains a line matching
              """
              .*WARN.*The report '.*' seems to be empty, ignoring.
              """
         AND the number of violations fed is 0

   @wip
   Scenario: The report mentions an unknown rule
     GIVEN the project "cppcheck_project"
     WHEN I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=rule_unknown.xml"
     THEN the analysis finishes successfully
         AND the analysis in server has completed
         AND the server log (if locatable) contains no error/warning messages
         BUT the analysis log contains a line matching
              """
              .*ERROR.*Could not add the issue.*The rule '.*' does not exist.
              """
         AND the number of violations fed is 0


   Scenario Outline: The reports are invalid
     GIVEN the project "cppcheck_project"
     WHEN I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=<reportpath>"
     THEN the analysis finishes successfully
         AND the analysis in server has completed
         AND the server log (if locatable) contains no error/warning messages
         BUT the analysis log contains a line matching
              """
              .*ERROR.*Report .* cannot be parsed
              """
         AND the number of violations fed is <violations>

     Examples:
      | reportpath          | violations |
      | unparsable.xml      | 0          |
      | wrongly_encoded.xml | 0          |


   Scenario: The reports use paths relative to directories listed in sonar.sources
     GIVEN the project "cppcheck_project"
     WHEN I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=relative-to-src.xml"
     THEN the analysis finishes successfully
         AND the analysis in server has completed
         AND the server log (if locatable) contains no error/warning messages
         BUT the analysis log contains a line matching
              """
              .*WARN.*Cannot find the file .* skipping violations
              """
         AND the number of violations fed is 0

   Scenario: The reports and issues in the reports have absolute paths
     GIVEN the project "cppcheck_with_absolute_paths_project"
        and platform is not "Windows"
     WHEN I run "sonar-scanner -X"
     THEN the analysis finishes successfully
         AND the analysis in server has completed
         AND the server log (if locatable) contains no error/warning messages
         AND the number of violations fed is 6
         
   # This doesnt work. We dont support reports outside of the projects directory,
   # although there is no good reason for that(??)
   #
   # Scenario: The reports are outside the project directory
   #   GIVEN the project "cppcheck_project"
   #       AND a report outside the projects directory, e.g. "/tmp/cppcheck-v1.xml"
   #   WHEN I run "sonar-scanner -X -Dsonar.cxx.cppcheck.reportPath=/tmp/cppcheck-v1.xml"
   #   THEN the analysis finishes successfully
   #       AND the analysis in server has completed
   #       AND the server log (if locatable) contains no error/warning messages
   #       AND the analysis log contains no error/warning messages
   #       AND the number of violations fed is 7

   # TOTEST:
   # - behaviour on windows (windows paths in the reports,
   # windows paths in the config pattern...)
   # - custom rules scenario (precondition: we're able to register such a rule automaticly)
