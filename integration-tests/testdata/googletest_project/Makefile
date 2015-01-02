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
	@sed 's| name="ts_one"| name="ts_one" filename="tests/unittests/test_without_fixture.cc"|' <gtest_without_fixture.xml >gtest_fname_in_ts.xml
	@sed 's| name="tc_one_good"| name="tc_one_good" filename="tests/unittests/test_without_fixture.cc"|' <gtest_without_fixture.xml >gtest_fname_in_tc.xml
	@sed -e 's| name="tc_one_good"| name="tc_one_good" filename="tests/unittests/test_without_fixture.cc"|'\
	     -e 's| name="ts_one"| name="ts_one" filename="trash"|' <gtest_without_fixture.xml >gtest_fname_in_both.xml

clean:
	rm -rf build googletest_*
	@$(MAKE) clean -C src
	@$(MAKE) clean -C tests
