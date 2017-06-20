Feature: Importing Clang Static Analyzer reports

  As a SonarQube user
  I want to import the Clang Static Analyzer reports into SonarQube
  In order to have all static code checking results in one place,
     work with them, filter them etc. and derive metrics from them.


  @SqApi56 @SqApi62
  Scenario Outline: Importing Clang Static Analyzer report(s)
    GIVEN the project "clangsa_project"
    GIVEN rule "ClangSA:core.DivideZero" is enabled
    GIVEN rule "ClangSA:deadcode.DeadStores" is enabled
    WHEN I run "sonar-scanner -X -Dsonar.cxx.clangsa.reportPath=<reportpath>"
    THEN the analysis finishes successfully
         AND the analysis in server has completed
         AND the server log (if locatable) contains no error/warning messages
         AND the number of violations fed is <violations>

    Examples:
      | reportpath      | violations |
      | divzero.plist   | 1          | # multi file plist
      | unused.plist    | 2          |

   @SqApi56 @SqApi62
   Scenario: The reports are missing
     GIVEN the project "clangsa_project"
     WHEN I run "sonar-scanner -X -Dsonar.cxx.clangsa.reportPath=empty.plist"
     THEN the analysis finishes successfully
         AND the analysis in server has completed
         AND the server log (if locatable) contains no error/warning messages
         AND the number of violations fed is 0

   @SqApi56 @SqApi62
   Scenario Outline: The reports are invalid
     GIVEN the project "clangsa_project"
     WHEN I run "sonar-scanner -X -Dsonar.cxx.clangsa.reportPath=<reportpath>"
     THEN the analysis finishes successfully
         AND the analysis in server has completed
         AND the server log (if locatable) contains no error/warning messages
         AND the number of violations fed is <violations>

     Examples:
      | reportpath          | violations |
      | unparsable.plist    | 0          |
      | wrongformat.plist   | 0          |
