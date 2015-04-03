Feature: Importing coverage data

  As a SonarQube user
  I want to import my coverage metric values into SonarQube
  In order to be able to use relevant SonarQube features


  Scenario: Importing coverage reports
      GIVEN the project "coverage_project"

      WHEN I run sonar-runner with following options:
          """
          -Dsonar.cxx.coverage.reportPath=ut-coverage.xml
          -Dsonar.cxx.coverage.itReportPath=it-coverage.xml
          -Dsonar.cxx.coverage.overallReportPath=overall-coverage.xml
          -Dsonar.cxx.coverage.forceZeroCoverage=False
          """

      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '#include <iostream>'
              """
          AND the following metrics have following values:
              | metric                  | value |
              | coverage                | 23.8  |
              | line_coverage           | 17.6  |
              | branch_coverage         | 50    |
              | it_coverage             | 40.6  |
              | it_line_coverage        | 36.4  |
              | it_branch_coverage      | 50    |
              | overall_coverage        | 34    |
              | overall_line_coverage   | 28.2  |
              | overall_branch_coverage | 50.0  |


  Scenario: Importing coverage reports zeroing coverage for untouched files
      GIVEN the project "coverage_project"

      WHEN I run sonar-runner with following options:
          """
          -Dsonar.cxx.coverage.reportPath=ut-coverage.xml
          -Dsonar.cxx.coverage.itReportPath=it-coverage.xml
          -Dsonar.cxx.coverage.overallReportPath=overall-coverage.xml
          -Dsonar.cxx.coverage.forceZeroCoverage=True
          """

      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '#include <iostream>'
              """
          AND the following metrics have following values:
              | metric                  | value |
              | coverage                | 8.5   |
              | line_coverage           | 5.5   |
              | branch_coverage         | 50    |
              | it_coverage             | 20.3  |
              | it_line_coverage        | 14.8  |
              | it_branch_coverage      | 50    |
              | overall_coverage        | 28.1  |
<<<<<<< HEAD
              | overall_line_coverage   | 22.0  |
=======
              | overall_line_coverage   | 22    |
>>>>>>> origin/master
              | overall_branch_coverage | 50    |


  Scenario: Zeroing coverage measures without importing reports

      If we dont pass coverage reports *and* request zeroing untouched
      files at the same time, all coverage measures, except the branch
      ones, should be 'zero'. The branch coverage measures remain 'None',
      since its currently ignored by the 'force zero...'
      implementation

      GIVEN the project "coverage_project"

      WHEN I run "sonar-runner -Dsonar.cxx.coverage.forceZeroCoverage=True"

      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*cannot find the sources for '#include <iostream>'
              """
          AND the following metrics have following values:
              | metric                  | value |
              | coverage                | 0     |
              | line_coverage           | 0     |
              | branch_coverage         | None  |
              | it_coverage             | 0     |
              | it_line_coverage        | 0     |
              | it_branch_coverage      | None  |
              | overall_coverage        | 0     |
              | overall_line_coverage   | 0     |
              | overall_branch_coverage | None  |
