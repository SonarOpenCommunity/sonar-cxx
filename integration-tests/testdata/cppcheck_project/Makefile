export CC = g++
export LD = g++

all:
	@$(MAKE) -C src

clean:
	rm -rf build
	@$(MAKE) clean -C src

sonar: cppcheck sonar_sonar

cppcheck: cppcheck_v1 cppcheck_v2 cppcheck_relative_to_src

cppcheck_v1:
	cppcheck -v --enable=all --xml src -Isrc 2> cppcheck-v1.xml

cppcheck_v2:
	cppcheck -v --enable=all --xml --xml-version=2 src -Isrc 2> cppcheck-v2.xml

cppcheck_relative_to_src:
	cd src; cppcheck -v --enable=all --xml --xml-version=1 . -I. 2> ../relative-to-src.xml; cd -

sonar_sonar:
	sonar-scanner -X
