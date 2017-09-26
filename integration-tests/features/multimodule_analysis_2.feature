@SqApi56
Feature: cpp-multimodule-project with sonar property file per module
  Test multimodule project with reports at root of the project


  Scenario: cpp-multimodule-project-2
    Given the project "cpp-multimodule-project-2"
    And platform is not "Windows"
    When I run "sonar-scanner -X"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages except those matching:
      """
      .*WARN.*Unable to get a valid mac address, will use a dummy address
      .*WARN.*cannot find the sources for '#include <gtest/gtest\.h>'
      .*WARN.*cannot find the sources for '#include <iostream>'
      .*WARN.*Cannot find the file '.*', skipping violations
      .*WARN.*to create a dependency with .*
      .*WARN.*the include root '.*' doesn't exist
      .*WARN.* cannot find the sources for .*
      .*WARN.*SCM provider autodetection failed.*
      .*WARN.*Cannot find a report for '.*'
      .*WARN.*File access Failed '.*'
      .*WARN.*A multi-module project can't have source folders, so '.*'
      .*ERROR.*Invalid report baseDir '.*'
      .*ERROR.*Using module base failed to find Path '.*'
      """
    And the following metrics have following values:
      | metric                   | value |
      | ncloc                    | 12    |
      | lines                    | 14    |
      | statements               | 4     |
      | classes                  | 0     |
      | files                    | 6     |
      | directories              | 4     |
      | functions                | 4     |
      | complexity               | 4     |
      | function_complexity      | 1.0   |
      | file_complexity          | 0.7   |
      | violations               | 4     |
