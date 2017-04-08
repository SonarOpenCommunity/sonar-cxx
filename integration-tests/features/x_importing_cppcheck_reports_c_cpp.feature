Feature: Importing Cppcheck reports

  @SqApi62
  Scenario Outline: Importing cppcheck issues when c language issues are in report.
    GIVEN the project "cppcheck_project_c_cpp"
        and declared header extensions of language c++ are ".hpp,.hh"
        and declared source extensions of language c++ are ".cpp,.cc"
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
      | cppcheck-v2.xml | 6          | # XML version 2

