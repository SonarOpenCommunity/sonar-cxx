Franck Bonin notes for Developper and User audiance :

SonarCxxPlugin need new parameters provided from project's pom.xml. See following sample :

  <!-- pom.xml extract -->
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.sonar.plugins</groupId>
        <artifactId>sonar-cxx-plugin.xunit</artifactId>
        <configuration>
          <directory>${basedir}/build_testu</directory>
          <includes><include>**/xunit-*.xml</include></includes>
          <!--<excludes><exclude>**/*.cpp</exclude></excludes>-->
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.codehaus.sonar.plugins</groupId>
        <artifactId>sonar-cxx-plugin.gcovr</artifactId>
        <configuration>
          <directory>${basedir}</directory>
          <includes><include>**/coverage.xml</include></includes>
          <!--<excludes><exclude>**/*.cpp</exclude></excludes>-->
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.codehaus.sonar.plugins</groupId>
        <artifactId>sonar-cxx-plugin.cppcheck</artifactId>
        <configuration>
          <directory>${basedir}</directory>
          <includes><include>**/cppcheck-result-*.xml</include></includes>
          <!--<excludes><exclude>**/*.cpp</exclude></excludes>-->
          <reportsIncludeSourcePath><include>..</include></reportsIncludeSourcePath>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.codehaus.sonar.plugins</groupId>
        <artifactId>sonar-cxx-plugin.cppncss</artifactId>
        <configuration>
          <directory>${basedir}</directory>
          <includes><include>**/cppncss-result-*.xml</include></includes>
          <!--<excludes><exclude>**/*.cpp</exclude></excludes>-->
          <reportsIncludeSourcePath><include>..</include></reportsIncludeSourcePath>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.codehaus.sonar.plugins</groupId>
        <artifactId>sonar-cxx-plugin.vera++</artifactId>
        <configuration>
          <directory>${basedir}</directory>
          <includes><include>**/vera++-result-*.xml</include></includes>
          <!--<excludes><exclude>**/*.cpp</exclude></excludes>-->
          <reportsIncludeSourcePath><include>..</include></reportsIncludeSourcePath>
        </configuration>
      </plugin>
      <plugin> 
        <groupId>org.codehaus.sonar.plugins</groupId> 
        <artifactId>sonar-cxx-plugin.valgrind</artifactId> 
        <configuration> 
          <directory>${basedir}</directory> 
          <includes><include>**/valgrind.xml</include></includes> 
          <!--<excludes><exclude>**/*.cpp</exclude></excludes>--> 
          <reportsIncludeSourcePath><include>..</include></reportsIncludeSourcePath> 
        </configuration> 
      </plugin>
    </plugins>	
    <sourceDirectory>${basedir}/sources</sourceDirectory>
  </build>

  <properties>
    <sonar.language>c++</sonar.language>
    <sonar.dynamicAnalysis>reuseReports</sonar.dynamicAnalysis>
  </properties>



${basedir} is maven well known variable.

Sub-node <properties><sonar.language> tell sonar that we check c++. It is requiered in ordzer to SonarCxxPlugin works properly.

Sub-node <properties><sonar.dynamicAnalysis> talls about anaylyse type. For now it must be «reuseReports» for all SonarCxxPlugin sensor (except RATS).

Sub-node <build><sourceDirectory> tells where c++ soursce are.Known file extensions are "cxx", "cpp", "h" and "hxx".

Sub-nodes <build><plugins><plugin><configuration> allow plugins to locate their pre-generated reports (see External Report generation quick Guide section)

Sub-nodes <build><plugins><plugin><configuration><reportsIncludeSourcePath> allow plugins to find resource that reports talk about. this is the same opption as well known gcc '-I' option. It is very useful to locate ressourc from relative path contains insid many reports.

##############################   TOOLS   #########################################
There is a sub directory src/main/resources/tools that contains Perl and Python scripts (and other old xml stuff, just in case...)

1/ gcovr.py is a script from (https://software.sandia.gov/trac/fast/wiki/gcovr) that convert gcov .gcda reports to cobertura xml report.
I modified this script to correct what sense to me 2 bugs : 
	- removed –preserve-paths option from gcov command line in order to ensure all .gcda report to be included.
	- An unintended DBG output occuring in the middle of XML output report. (gcovr.py works for Hudson to)

2/ vera++Report2checkstyleRepport.perl is (of course) a script that convert vera++ test style report to xml checkstyle report (This script works for Hudson to)

3/ inventory2XmlRepository.perl is a developper tool to create XML rule repository for cppcheak, valgrind and vera++ sensor. It needs as input an extracted inventory from tools source themself. I provided thos inventory, see for example cppcheck_rule_inventory_XXXXXXXX.txt. (rule extraction from source is not detailed here since it's not very concistant. It is basycally accomplished using find and egrep commands on source code or tools repository)

4/ xmlRepository2XmlDefaultProfile.perl is a developper tool to create XML defaut profile for cppcheak, valgrind and vera++ sensor. It needs as input a xml repository previously build with inventory2XmlRepository.perl script

###############  External Report generation quick Guide  ########################

cppcheck :
cppcheck -v --enable=all --force --xml [SOURCE_PATH] 2> [REPORT_PATH]/cppcheck-result-XXXXX.xml

cppncss : 
cppncss -v -r [SOURCE_PATH] -x -f=[REPORT_PATH]/cppncss-result-XXXXXX.xml
	(you may use -D and -M options to tell cppncss about preprocessor MACRO and DIRECTIVE)

gcov / gcovr :
1/ use gcc with -fprofile-arcs -ftest-coverage seted and eventualy debug flags options
2/ execute you application or unit test
3/ gcovr.py -x > [REPORT_PATH]/coverage.xml 
	([SOURCE_PATH] is not needed since gcovr will recursively explore for .gcda files)

vera++ :
	find [SOURCE_PATH] -name *.cpp && find [SOURCE_PATH] -name *.h | xargs vera++ -nodup -showrules 2>[REPORT_PATH]/vera++-result-XXXXX.tmp
	cat [REPORT_PATH]/vera++-result-XXXXX.tmp | perl vera2checkstyle.perl > [REPORT_PATH]/vera++-result-XXXXX.xml

valgrind :
1/ Use gcc debug flags options to compile your program or unit test (gcov compilation can be reused here)
3/ Execute you application or unit test using :
	valgrind --leak-check=yes --xml=yes --xml-file=[REPORT_PATH]/valgrind.xml --demangle=no [FILE_PATH_AND_ARGS_TO_MY_PROGRAM]


