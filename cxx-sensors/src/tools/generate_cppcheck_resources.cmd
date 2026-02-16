@ECHO OFF
cls

SET SCRIPT_DIR=%~dp0
SET CPPCHECK_DIR=C:\Program Files\Cppcheck\
SET PYTHON_DIR=C:\Program Files (x86)\Microsoft Visual Studio\Shared\Python39_64\

setlocal ENABLEDELAYEDEXPANSION

SET CPPCHECK_LIBRARY_ARGS=

for %%i in ("%CPPCHECK_DIR%cfg\*.cfg") do (
     SET CPPCHECK_LIBRARY_ARGS=!CPPCHECK_LIBRARY_ARGS! "--library=%%~nxi"
)

rem download cwec_latest.xml.zip and extract it to unzip cwec_vx.y.xml
rem wget https://cwe.mitre.org/data/xml/cwec_latest.xml.zip --output-document=cwec_latest.xml.zip && unzip -j -o cwec_latest.xml.zip

"%PYTHON_DIR%python.exe" -V

ECHO create Cppcheck errorlist cppcheck-errorlist.xml...
"%CPPCHECK_DIR%cppcheck.exe" %CPPCHECK_LIBRARY_ARGS% --errorlist --xml-version=2 > cppcheck-errorlist.xml

ECHO create SonarQube rules file cppcheck.xml...
"%CPPCHECK_DIR%cppcheck.exe" %CPPCHECK_LIBRARY_ARGS% --errorlist --xml-version=2 | "%PYTHON_DIR%python.exe" cppcheck_createrules.py rules cwec_v4.19.1.xml > cppcheck.xml

ECHO create cppcheck-comparison.md...
"%PYTHON_DIR%python.exe" utils_createrules.py comparerules "%SCRIPT_DIR%\..\main\resources\cppcheck.xml" .\cppcheck.xml > cppcheck-comparison.md
