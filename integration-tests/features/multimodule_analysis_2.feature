Feature: cpp-multimodule-project

  Test multimodule project with reports at root of the project

  @SqApi56 @SqApi62
  Scenario: cpp-multimodule-project-2
  
      GIVEN the project "cpp-multimodule-project-2"
          and platform is not "Windows"
      
      WHEN I run "sonar-scanner -X"
      THEN the analysis finishes successfully
          AND the analysis in server has completed
          AND the analysis log contains no error/warning messages except those matching:
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
          AND the following metrics have following values:
              | metric                   | value |
              # size metrics
              | ncloc                    | 12    |
              | lines                    | 14    |
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
