@ECHO OFF
CLS

REM customize paths for your local system
SET SCRIPT_DIR=%~dp0
SET PANDOC_DIR=C:\Program Files\Pandoc\
SET PYTHON_DIR=C:\Program Files (x86)\Microsoft Visual Studio\Shared\Python37_64\
SET LLVM_DIR=C:\Development\git\llvm-project\

REM verify paths
IF NOT EXIST "%PANDOC_DIR%" (
   ECHO [ERROR] Invalid PANDOC_DIR setting
   GOTO ERROR
)

IF NOT EXIST "%PYTHON_DIR%" (
   ECHO [ERROR] Invalid PYTHON_DIR setting
   GOTO ERROR
)

IF NOT EXIST "%LLVM_DIR%" (
   ECHO [ERROR] Invalid LLVM_DIR setting
   GOTO ERROR
)

IF NOT EXIST "%LLVM_DIR%build\Release\bin" (
   ECHO [ERROR] You have to build LLVM first
   GOTO ERROR
)

REM tool versions
"%PANDOC_DIR%pandoc.exe" -v
"%PYTHON_DIR%python.exe" -V
"%LLVM_DIR%build\Release\bin\llvm-tblgen.exe" --version
git --version
ECHO LLVM recent tag:
PUSHD "%LLVM_DIR%"
git describe
POPD
ECHO.

REM GENERATION OF RULES FROM CLANG-TIDY DOCUMENTATION (RST FILES)
ECHO [INFO] generate the new version of the rules file...
"%PYTHON_DIR%python.exe" "%SCRIPT_DIR%clangtidy_createrules.py" rules "%LLVM_DIR%clang-tools-extra\docs\clang-tidy\checks" > "%SCRIPT_DIR%clangtidy_new.xml"
ECHO [INFO] compare the new version with the old one, extend the old XML...
"%PYTHON_DIR%python.exe" "%SCRIPT_DIR%utils_createrules.py" comparerules "%SCRIPT_DIR%..\main\resources\clangtidy.xml" "%SCRIPT_DIR%clangtidy_new.xml" > "%SCRIPT_DIR%clangtidy-comparison.md"

REM GENERATION OF RULES FROM CLANG DIAGNOSTICS
ECHO [INFO] generate the list of diagnostics...
PUSHD "%LLVM_DIR%clang\include\clang\Basic"
"%LLVM_DIR%build\Release\bin\llvm-tblgen.exe" -dump-json "%LLVM_DIR%clang\include\clang\Basic\Diagnostic.td" > "%SCRIPT_DIR%diagnostic.json"
POPD
ECHO [INFO] generate the new version of the diagnostics file...
"%PYTHON_DIR%python.exe" "%SCRIPT_DIR%clangtidy_createrules.py" diagnostics "%SCRIPT_DIR%diagnostic.json" > "%SCRIPT_DIR%diagnostic_new.xml"
ECHO [INFO] compare the new version with the old one, extend the old XML...
"%PYTHON_DIR%python.exe" "%SCRIPT_DIR%utils_createrules.py" comparerules "%SCRIPT_DIR%..\main\resources\clangtidy.xml" "%SCRIPT_DIR%diagnostic_new.xml" > "%SCRIPT_DIR%diagnostic-comparison.md"

REM exit
GOTO END
:ERROR
ECHO.
ECHO [ERROR] execution failed
EXIT /B 1

:END
ECHO.
ECHO [INFO] finished succesfully
EXIT /B 0
