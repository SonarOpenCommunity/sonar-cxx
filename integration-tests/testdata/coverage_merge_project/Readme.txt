Coverage Merge Project
======================

Example to check if the results of different coverage reports are merged correctly.

When multiple sources provide divergent coverage data, they are aggregated. Since SonarQube 6.2 you can save several reports for the same file and reorts will be merged using the following "additive" strategy:

- Reports should contain coverage data only for executable lines.
- The coverage data can be spread over several reports.
- Multiple sensors can be used to read coverage data.
- Source code and test code do not have to be in the same programming language.
- The data is merged with the existing coverage information in each case.
  - Line hits are cumulated.
  - The maximum value for the condition coverage is used. Examples: 2/4 + 2/4 = 2/4, 2/4 + 3/4 = 3/4
