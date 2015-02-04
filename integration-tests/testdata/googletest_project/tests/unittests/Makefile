# Root directory of google test framework
GTEST_DIR = ../gtest-1.7.0

CPPFLAGS = -I../../src -I$(GTEST_DIR)
LIBS = -lpthread ../libgtest.a $(BUILD_DIR)/libcomponents.a

TESTS = $(BUILD_DIR)/test_component1 \
	$(BUILD_DIR)/test_two_fixtures \
	$(BUILD_DIR)/test_without_fixture \
	$(BUILD_DIR)/test_fname_in_tc\
	$(BUILD_DIR)/test_fname_in_ts\
	$(BUILD_DIR)/test_fname_in_both

all: $(TESTS)

$(BUILD_DIR)/test_component1: $(BUILD_DIR)/libcomponents.a test_component1.o
	$(LD) $^ $(LIBS) $(LDFLAGS) -o $@

$(BUILD_DIR)/test_two_fixtures: test_two_fixtures.o
	$(LD) $^ $(LIBS) $(LDFLAGS) -o $@

$(BUILD_DIR)/test_without_fixture: test_without_fixture.o
	$(LD) $^ $(LIBS) $(LDFLAGS) -o $@

$(BUILD_DIR)/test_fname_in_tc: test_fname_in_tc.o
	$(LD) $^ $(LIBS) $(LDFLAGS) -o $@

$(BUILD_DIR)/test_fname_in_ts: test_fname_in_ts.o
	$(LD) $^ $(LIBS) $(LDFLAGS) -o $@

$(BUILD_DIR)/test_fname_in_both: test_fname_in_both.o
	$(LD) $^ $(LIBS) $(LDFLAGS) -o $@

%.o : %.cc
	$(CC) -c $(CPPFLAGS) $< -o $@

clean:
	rm -f *.a *.o *.gc*
