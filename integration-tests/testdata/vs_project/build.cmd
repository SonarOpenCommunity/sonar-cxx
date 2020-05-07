@ECHO OFF

rem set Visual Studio environment
:vs2019
IF NOT EXIST "C:\Program Files (x86)\Microsoft Visual Studio\2019\Enterprise\Common7\IDE\devenv.exe" GOTO end
PUSHD %~dp0
CALL "C:\Program Files (x86)\Microsoft Visual Studio\2019\Enterprise\VC\Auxiliary\Build\vcvarsall.bat" x64 -vcvars_ver=14.2
POPD

rem build solution
PUSHD %~dp0
mkdir reports
msbuild.exe ".\vs-project.sln" /t:Rebuild /p:Configuration=Debug;WarningLevel=4 /fileLogger /fileLoggerParameters:WarningsOnly;LogFile=.\reports\warnings.log;Verbosity=detailed;Encoding=UTF-8
POPD

:end
