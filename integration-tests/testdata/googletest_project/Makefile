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
	@-$(BUILD_DIR)/test_component1 --gtest_output=xml:gtest.xml
	@-$(BUILD_DIR)/test_two_fixtures --gtest_output=xml:gtest_two_fixtures_in_one_file.xml
	@-$(BUILD_DIR)/test_without_fixture --gtest_output=xml:gtest_without_fixture.xml
	@sed 's/name="tc_one"/name="tc_one" filename="tests\/unittests\/test_without_fixture.cc"/' <gtest_without_fixture.xml >gtest_with_filenames.xml

clean:
	rm -rf build googletest_*
	@$(MAKE) clean -C src
	@$(MAKE) clean -C tests
