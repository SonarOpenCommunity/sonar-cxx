Feature: Regex

  Testing rules with regex expressions.

  Scenario: Test rule 'cxx:FileHeader' with regex and UTF-8 files (without BOM) with different file endings.
    Given the project "regex_project"
    And rule "cxx:FileHeader" with params "isRegularExpression=true;headerFormat=//\s*<copyright>\s*//\s*Copyright \(c\) (AAA BBB|CCC DDD) GmbH. All rights reserved.\s*//\s*</copyright>\s*" is activated
    When I run sonar-scanner with "-X -Dsonar.exclusions=**/*-BOM-*.cc"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages
    And the following metrics have following values:
      | metric     | value |
      | ncloc      | 3     |
      | lines      | 18    |
      | files      | 3     |
      | violations | 0     |

  Scenario: Test rule 'cxx:FileHeader' with regex and UTF-8 files (with BOM) with different file endings.
    Given the project "regex_project"
    And rule "cxx:FileHeader" with params "isRegularExpression=true;headerFormat=//\s*<copyright>\s*//\s*Copyright \(c\) (AAA BBB|CCC DDD) GmbH. All rights reserved.\s*//\s*</copyright>\s*" is activated
    When I run sonar-scanner with "-X -Dsonar.inclusions=**/utf8-BOM-*.cc"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages
    And the following metrics have following values:
      | metric     | value |
      | ncloc      | 3     |
      | lines      | 18    |
      | files      | 3     |
      | violations | 0     |

  Scenario: Test rule 'cxx:FileHeader' with regex and UTF-16 files (with BOM) with different file endings.
    Given the project "regex_project"
    And rule "cxx:FileHeader" with params "isRegularExpression=true;headerFormat=//\s*<copyright>\s*//\s*Copyright \(c\) (AAA BBB|CCC DDD) GmbH. All rights reserved.\s*//\s*</copyright>\s*" is activated
    When I run sonar-scanner with "-X -Dsonar.inclusions=**/utf16-BOM-*.cc"
    Then the analysis finishes successfully
    And the analysis in server has completed
    And the analysis log contains no error/warning messages
    And the following metrics have following values:
      | metric     | value |
      | ncloc      | 3     |
      | lines      | 18    |
      | files      | 3     |
      | violations | 0     |
