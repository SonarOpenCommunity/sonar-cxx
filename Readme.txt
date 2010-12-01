Franck Bonin notes for Developper and User audiance :

There is a sub directory src/main/resources/tools that contains Perl and Python scripts (and other old xml stuff, just in case...)

1/ gcovr.py is a script from (https://software.sandia.gov/trac/fast/wiki/gcovr) that convert gcov .gcda reports to cobertura xml report.
I modified this script to correct what sense to me 2 bugs : 
	- removed –preserve-paths option from gcov command line in order to ensure all .gcda report to be included.
	- An unintended DBG output occuring in the middle of XML output report. (gcovr.py works for Hudson to)

2/ vera++Report2checkstyleRepport.perl is (of course) a script that convert vera++ test style report to xml checkstyle report (This script works for Hudson to)

3/ inventory2XmlRepository.perl is a developper tool to create XML rule repository for cppcheak, valgrind and vera++ sensor. It needs as input an extracted inventory from tools source themself. I provided thos inventory, see for example cppcheck_rule_inventory_XXXXXXXX.txt. (rule extraction from source is not detailed here since it's not very concistant. It is basycally accomplished using find and egrep commands on source code or tools repository)

4/ xmlRepository2XmlDefaultProfile.perl is a developper tool to create XML defaut profile for cppcheak, valgrind and vera++ sensor. It needs as input a xml repository previously build with inventory2XmlRepository.perl script



