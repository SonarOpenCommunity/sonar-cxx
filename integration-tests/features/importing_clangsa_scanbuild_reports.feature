Feature: Importing Clang Static Analyzer reports

  As a SonarQube user
  I want to import the Clang Static Analyzer reports into SonarQube,
  to analyze my project I've used scan-build.
  In order to have all static code checking results in one place,
     work with them, filter them etc. and derive metrics from them.

  @SqApi56 @SqApi62
  Scenario Outline: Importing Clang Static Analyzer report(s) generated with scan-build
    GIVEN the project "clangsa_scanbuild_project"
    GIVEN rule "ClangSA:core.DivideZero" is enabled
    GIVEN rule "ClangSA:deadcode.DeadStores" is enabled
    WHEN I run "sonar-scanner -X -Dsonar.cxx.clangsa.reportPath=<reportpath>"
    THEN the analysis finishes successfully
         AND the analysis in server has completed
         AND the server log (if locatable) contains no error/warning messages
         AND the number of violations fed is <violations>

    Examples:
      | reportpath                   | violations |
      | analyzer_reports/*/*.plist   | 2          | # multi file plist output is not supported by scan-build yet
