Franck Bonin notes for Developper and User audiance :

Sonar Cxx Plugin can extract parameters provided by cxx-maven-plugin configuration node from project's pom.xml. See following sample :

  <!-- pom.xml extract -->
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>cxx-maven-plugin</artifactId>
        <configuration>
          <sourceDirs>
            <sourceDir>sources/application</sourceDir>
            <sourceDir>sources/utils</sourceDir>
            <sourceDir>sources/tests</sourceDir>
          </sourceDirs>
          <!-- All of the sub configuration nodes following are needed by sonar-cxx plugin -->
          <!-- This sample provided values are default values.
          <!-- So you can simple remove all of it if you provide reports in the right places -->
          <xunit>
            <directory>${basedir}/xunit-reports</directory>
            <includes>
              <include>**/xunit-result-*.xml</include>
            </includes>
          </xunit>
          <gcovr>
            <directory>${basedir}/gcovr-reports</directory>
            <includes>
              <include>**/gcovr-reports-*.xml</include>
            </includes>
          </gcovr>
          <cppcheck>
            <directory>${basedir}/cppcheck-reports</directory>
            <includes>
              <include>**/cppcheck-result-*.xml</include>
            </includes>
            <reportsIncludeSourcePath>
              <include>..</include>
            </reportsIncludeSourcePath>
          </cppcheck>
          <cppncss>
            <directory>${basedir}/cppncss-reports</directory>
            <includes>
              <include>**/cppncss-result-*.xml</include>
            </includes>
            <reportsIncludeSourcePath>
              <include>..</include>
            </reportsIncludeSourcePath>
          </cppncss>
          <veraxx>
            <directory>${basedir}/vera++-reports</directory>
            <includes>
              <include>**/vera++-result-*.xml</include>
            </includes>
            <reportsIncludeSourcePath>
              <include>..</include>
            </reportsIncludeSourcePath>
          </veraxx>
          <valgrind>
            <directory>${basedir}/valgrind-reports</directory>
            <includes>
              <include>**/valgrind-result-*.xml</include>
            </includes>
            <reportsIncludeSourcePath>
              <include>..</include>
            </reportsIncludeSourcePath>
          </valgrind>
        </configuration>
      </plugin>
    </plugins>	
    <!-- We have our own <configuration><sourceDirs> node inside cxx plugin configuration -->
    <!-- <sourceDirectory>${basedir}/sources</sourceDirectory> -->
  </build>
  <properties>
    <sonar.language>c++</sonar.language>
    <sonar.dynamicAnalysis>reuseReports</sonar.dynamicAnalysis>
  </properties>


${basedir} is maven well known variable.

Sub-node <properties><sonar.language> tell sonar that we check c++. It is requiered in ordzer to SonarCxxPlugin works properly.

Sub-node <properties><sonar.dynamicAnalysis> talls about anaylyse type. For now it must be «reuseReports» for all Sonar Cxx Plugin sensor (except RATS).

Sub-nodes <configuration><sourceDirs><sourceDir> tell where c++ sources directories are. It is a replacement for <build><sourceDirectory>. Known file extensions are "cxx", "cpp", "h" and "hxx".

Sub-node <build><plugins><plugin><configuration><xunit> allow Xunit sensor to locate its pre-generated reports (see External Report generation quick Guide section)
Sub-node <build><plugins><plugin><configuration><gcovr> allow coverage sensor to locate its pre-generated reports (see External Report generation quick Guide section)
Sub-node <build><plugins><plugin><configuration><cppcheck> allow cppcheck sensor to locate its pre-generated reports (see External Report generation quick Guide section)
Sub-node <build><plugins><plugin><configuration><cppncss> allow cppncss sensor to locate its pre-generated reports (see External Report generation quick Guide section)
Sub-node <build><plugins><plugin><configuration><veraxx> allow vera++ sensor to locate its pre-generated reports (see External Report generation quick Guide section)
Sub-node <build><plugins><plugin><configuration><valgrind> allow valgrind sensor to locate its pre-generated reports (see External Report generation quick Guide section)

Sub-nodes <build><plugins><plugin><configuration><XXX><reportsIncludeSourcePath> allow plugins to find resource that reports talk about. This is the same opption as well known gcc '-I' option. It is very useful to locate ressource from relative path that reports talk about.

##############################   TOOLS ( Developeur )   #########################################
There is a sub directory src/main/resources/tools that contains Perl and Python scripts (and other old xml stuff, just in case...)

1/ gcovr.py is a script from (https://software.sandia.gov/trac/fast/wiki/gcovr) that convert gcov .gcda reports to cobertura xml report.
I modified this script to correct what sense to me 2 bugs : 
	- removed –preserve-paths option from gcov command line in order to ensure all .gcda report to be included.
	- An unintended DBG output occuring in the middle of XML output report. (gcovr.py works for Hudson to)

2/ vera++Report2checkstyleRepport.perl is (of course) a script that convert vera++ test style report to xml checkstyle report (This script works for Hudson to)

3/ inventory2XmlRepository.perl is a developper tool to create XML rule repository for cppcheak, valgrind and vera++ sensor. It needs as input an extracted inventory from tools source themself. I provided thos inventory, see for example cppcheck_rule_inventory_XXXXXXXX.txt. (rule extraction from source is not detailed here since it's not very concistant. It is basycally accomplished using find and egrep commands on source code or tools repository)

4/ xmlRepository2XmlDefaultProfile.perl is a developper tool to create XML defaut profile for cppcheak, valgrind and vera++ sensor. It needs as input a xml repository previously build with inventory2XmlRepository.perl script


##################  Integration with cxx-maven-plugin  ##########################
See provided 'blank' sample CXX_PROJECT_SAMPLE.tar.gz

###############  External Report generation quick Guide  ########################

cppcheck :
cppcheck -v --enable=style --force --xml [SOURCE_PATH(s)] 2> [POM_PATH]/cppcheck-reports/cppcheck-result-XXXXX.xml

cppncss : 
cppncss -v -r [SOURCE_PATH] -x -f=[POM_PATH]/cppncss-reports/cppncss-result-XXXXXX.xml
	(you may use -D and -M options to tell cppncss about preprocessor MACRO and DIRECTIVE)

gcov / gcovr :
1/ use gcc with -fprofile-arcs -ftest-coverage seted and eventualy debug flags options
2/ execute you application or unit test
3/ gcovr.py -x -d [SOURCE_PATH(s)]> [POM_PATH]/gcovr-reports/gcovr-result-XXXXXX.xml
	([SOURCE_PATH] is not needed since gcovr will recursively explore for .gcda files of current dir)

vera++ :
	find [SOURCE_PATH] -name *.cpp && find [SOURCE_PATH] -name *.h | xargs vera++ -nodup -showrules 2>[POM_PATH]/vera++-reports/vera++-result-XXXXX.tmp
	cat [POM_PATH]/vera++-reports/vera++-result-XXXXX.tmp | perl vera2checkstyle.perl > [POM_PATH]/vera++-reports/vera++-result-XXXXX.xml

valgrind :
1/ Use gcc debug flags options to compile your program or unit test (gcov compilation can be reused here)
3/ Execute you application or unit test using :
	valgrind --leak-check=yes --demangle=no --xml=yes --xml-file=[POM_PATH]/valgrind-reports/valgrind-result-XXXXX.xml [FILE_PATH_AND_ARGS_TO_MY_PROGRAM]


