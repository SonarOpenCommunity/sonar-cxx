Feature: multi-module project

  As a SonarQube user
  I want use the multi-module project feature
  - Way #1 Set all the configuration in the properties file in the root folder
  - Way #2 Set the configuration in multiple properties files
  There are different possibilities to define the report path
  - absolute paths
    - inside root projectBaseDir
    - outside root projectBaseDir
  - relative paths
    - relative to root projectBaseDir
    - relative to current projectBaseDir
  Path of issues in reports can be
  - absolute paths
  - relative paths to current projectBaseDir
  
  Scenario: multi-module-project with all reports in one folder inside of root
  
      configuration: one configuration file in root projectBaseDir
      report location: all reports in one folder relative to root projectBaseDir
      issue paths in reports: relative to current projectBaseDir
  
      GIVEN the project "multimodule_project_reports_root"
      WHEN I run "sonar-runner -X -e"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*Unable to get a valid mac address, will use a dummy address
              .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
              .*WARN.*cannot find the sources for '#include <iostream>'
              .*WARN  - Cannot find the file '.*', skipping violations
              .*WARN.*to create a dependency with .*
              .*WARN  - the include root '.*' doesn't exist
              .*WARN  - .* cannot find the sources for .*
              .*WARN  - SCM provider autodetection failed.*
              .*WARN.*Cannot find a report for '.*'
              .*WARN.*- File access Failed '.*'
              .*ERROR.*Invalid report baseDir '.*'
              .*ERROR.*Using module base failed to find Path '.*'
              """
          AND the following metrics have following values:
               | metric                   | value |
               # size metrics
               | ncloc                    | 56    |
               | lines                    | 150   |
               | statements               | 36    |
               | classes                  | 1     |
               | files                    | 8     |
               | directories              | 4     |
               | functions                | 5     |
               # comments / documentation
               | comment_lines_density    | 30    |
               | comment_lines            | 24    |
               # duplications
               | duplicated_lines_density | 57.3  |
               | duplicated_lines         | 86    |
               | duplicated_blocks        | 2     |
               | duplicated_files         | 2     |
               # complexity
               | complexity               | 7     |
               | function_complexity      | 1.4   |
               | file_complexity          | 0.9   |
               | class_complexity         | 6     |
               # violations
               | violations               | 28    |
               # test execution statistics
               | test_success_density     | 33.3  |
               | test_failures            | 2     |
               | skipped_tests            | 1     |
               | test_errors              | 0     |
               | tests                    | 3     |
               

  Scenario: multi-module project with all reports in one folder outside of root
  
      configuration: configuration in multiple properties files
      report location: all reports in one folder absolute and outside of root projectBaseDir
      issue paths in reports: absolute paths
      
      GIVEN the project "multimodule_project_reports_outside"
      WHEN I run "sonar-runner -X -e"
      THEN the analysis finishes successfully
          AND the analysis log contains no error/warning messages except those matching:
              """
              .*WARN.*Unable to get a valid mac address, will use a dummy address
              .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
              .*WARN.*cannot find the sources for '#include <iostream>'
              .*WARN  - Cannot find the file '.*', skipping violations
              .*WARN.*to create a dependency with .*
              .*WARN  - the include root '.*' doesn't exist
              .*WARN  - .* cannot find the sources for .*
              .*WARN  - SCM provider autodetection failed.*
              .*WARN.*Cannot find a report for '.*'
              .*WARN.*- File access Failed '.*'
              .*WARN.* A multi-module project can't have source folders, so '.*'
              .*ERROR.*Invalid report baseDir '.*'
              .*ERROR.*Using module base failed to find Path '.*'
              """
          AND the following metrics have following values:
               | metric                   | value |
               # size metrics
               | ncloc                    | 12    |
               | lines                    | 18    |
               | statements               | 4     |
               | classes                  | 0     |
               | files                    | 6     |
               | directories              | 4     |
               | functions                | 4     |
               # complexity
               | complexity               | 4     |
               | function_complexity      | 1.0   |
               | file_complexity          | 0.7   |
               # violations
               | violations               | 4     |
