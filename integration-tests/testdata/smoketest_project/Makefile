export BUILD_DIR = $(shell "pwd")/build

# Flags passed to the C++ compiler.
export CXXFLAGS = -g -pthread --coverage

# Flags passed to the linker.
export LDFLAGS = -pthread --coverage

export CC = g++
export LD = g++
export AR = ar

all:
	@mkdir -p build
	@$(MAKE) -C src
	@$(MAKE) -C tests

clean:
	rm -rf build
	@$(MAKE) clean -C src
	@$(MAKE) clean -C tests


############################ Sonar-related rules ###############################

sonar: sonar_cppcheck sonar_runtests sonar_coverage sonar_sonar

CPPCHECK_INCLUDES = -Isrc
SOURCES_TO_ANALYSE = src

# Run cppcheck, a general purpose static code checker
sonar_cppcheck:
	cppcheck -v --enable=all --xml $(CPPCHECK_INCLUDES) $(SOURCES_TO_ANALYSE) 2> $(BUILD_DIR)/cppcheck-report.xml

# Run some tests. This run provides:
# 1) test execution report
# 2) valgrind/memcheck report
# 3) raw coverage data (.gcda-files)
sonar_runtests:
	-valgrind --xml=yes --xml-file=$(BUILD_DIR)/valgrind-report.xml $(BUILD_DIR)/test_component1 --gtest_output=xml:$(BUILD_DIR)/xunit-report.xml

# Collect the coverage data and convert it to cobertura-format
sonar_coverage:
	gcovr -x -r . > $(BUILD_DIR)/gcovr-report.xml

# Parse collected data and feed it into sonar
sonar_sonar:
	sonar-scanner -X
