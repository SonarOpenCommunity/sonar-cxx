This project is used for integration testing.
The tested feature is importing Clang Static Analyzer reports.
To analyze the whole project scan-build is used (https://github.com/rizsotto/scan-build).

The absolute source file path in the plist report was modified to relative path because the 
tests run in different environments.
