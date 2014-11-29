export BUILD_DIR = $(shell "pwd")/build

# Flags passed to the C++ compiler.
export CXXFLAGS = -pthread

# Flags passed to the linker.
export LDFLAGS = -pthread

export CC = g++
export LD = g++
export AR = ar

all:
	@mkdir -p build
	@$(MAKE) -C src
	@$(MAKE) -C tests
	@-$(BUILD_DIR)/test_component1 --gtest_output=xml:googletest_report_1.xml
	@-$(BUILD_DIR)/test_misc --gtest_output=xml:googletest_report_2.xml
	@-$(BUILD_DIR)/test_without_fixture --gtest_output=xml:tmp.xml
	@sed 's/name="tc_one"/name="tc_one" filename="tests\/unittests\/tc_one.cc"/' <tmp.xml >googletest_with_filenames.xml
	@rm tmp.xml

clean:
	rm -rf build googletest_*
	@$(MAKE) clean -C src
	@$(MAKE) clean -C tests
