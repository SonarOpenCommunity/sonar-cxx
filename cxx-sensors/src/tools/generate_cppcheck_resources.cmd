@ECHO OFF
cls

SET SCRIPT_DIR=%~dp0
SET CPPCHECK_DIR=C:\Program Files\Cppcheck\
SET PYTHON_DIR=

SET CPPCHECK_LIBRARY_ARGS=--library=avr.cfg --library=bento4.cfg --library=boost.cfg --library=bsd.cfg --library=cairo.cfg --library=cppunit.cfg --library=dpdk.cfg --library=embedded_sql.cfg --library=gnu.cfg --library=googletest.cfg --library=gtk.cfg --library=kde.cfg --library=libcerror.cfg --library=libcurl.cfg --library=libsigc++.cfg --library=lua.cfg --library=mfc.cfg--library=microsoft_atl.cfg --library=microsoft_sal.cfg --library=microsoft_unittest.cfg --library=motif.cfg --library=nspr.cfg --library=opencv2.cfg --library=opengl.cfg --library=openmp.cfg --library=openssl.cfg --library=posix.cfg --library=python.cfg --library=qt.cfg --library=ruby.cfg --library=sdl.cfg --library=sfml.cfg --library=sqlite3.cfg --library=std.cfg --library=tinyxml2.cfg --library=vcl.cfg --library=windows.cfg --library=wxwidgets.cfg --library=zlib.cfg

rem download cwec_latest.xml.zip and extract it to unzip cwec_vx.y.xml
rem wget https://cwe.mitre.org/data/xml/cwec_latest.xml.zip --output-document=cwec_latest.xml.zip && unzip -j -o cwec_latest.xml.zip

"%PYTHON_DIR%python.exe" -V

ECHO create Cppcheck errorlist cppcheck-errorlist.xml...
"%CPPCHECK_DIR%cppcheck.exe" %CPPCHECK_LIBRARY_ARGS% --errorlist --xml-version=2 > cppcheck-errorlist.xml

ECHO create SonarQube rules file cppcheck.xml...
"%CPPCHECK_DIR%cppcheck.exe" %CPPCHECK_LIBRARY_ARGS% --errorlist --xml-version=2 | "%PYTHON_DIR%python.exe" cppcheck_createrules.py rules cwec_v4.5.xml > cppcheck.xml

ECHO create cppcheck-comparison.md...
"%PYTHON_DIR%python.exe" utils_createrules.py comparerules "%SCRIPT_DIR%\..\main\resources\cppcheck.xml" .\cppcheck.xml > cppcheck-comparison.md
