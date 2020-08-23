rem @ECHO OFF

SET SCRIPT_DIR=%~dp0
SET CPPCHECK_DIR=C:\Program Files\Cppcheck
SET PYTHON_DIR=C:\Perforce\Helix-QAC-2020.1\components\python-2.7.10

SET CPPCHECK_LIBRARY_ARGS=--library=avr.cfg --library=boost.cfg --library=bsd.cfg --library=cfg.txt --library=cppunit.cfg --library=embedded_sql.cfg --library=gnu.cfg --library=googletest.cfg --library=gtk.cfg --library=libcerror.cfg --library=microsoft_sal.cfg --library=motif.cfg --library=nspr.cfg --library=opengl.cfg --library=posix.cfg --library=python.cfg --library=qt.cfg --library=ruby.cfg --library=sdl.cfg --library=sfml.cfg --library=sqlite3.cfg --library=std.cfg --library=windows.cfg --library=wxwidgets.cfg --library=zlib.cfg

rem wget https://cwe.mitre.org/data/xml/cwec_latest.xml.zip --output-document=cwec_latest.xml.zip && unzip -j -o cwec_latest.xml.zip

"%CPPCHECK_DIR%\cppcheck.exe" %CPPCHECK_LIBRARY_ARGS% --errorlist --xml-version=2 > cppcheck-errorlist.xml
"%CPPCHECK_DIR%\cppcheck.exe" %CPPCHECK_LIBRARY_ARGS% --errorlist --xml-version=2 | "%PYTHON_DIR%\python.exe" cppcheck_createrules.py rules cwec_v4.2.xml > cppcheck.xml
"%PYTHON_DIR%\python.exe" utils_createrules.py comparerules "%SCRIPT_DIR%\..\main\resources\cppcheck.xml" cppcheck.xml > cppcheck-comparison.md
