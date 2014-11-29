# Root directory of google test framework
GTEST_DIR = ../gtest-1.6.0

CPPFLAGS = -I../../src -I$(GTEST_DIR)/include -I$(GTEST_DIR)
CXXFLAGS = -g
LIBS = -lpthread ../libgtest.a $(BUILD_DIR)/libcomponents.a

TESTS = $(BUILD_DIR)/test_component1

all: $(TESTS)

# libgtest.a:
# 	$(CC) $(CPPFLAGS) $(CXXFLAGS) -c $(GTEST_DIR)/src/gtest-all.cc -o gtest-all.o
# 	$(CC) $(CPPFLAGS) $(CXXFLAGS) -c $(GTEST_DIR)/src/gtest_main.cc -o gtest_main.o
# 	$(AR) -r libgtest.a gtest-all.o gtest_main.o

$(BUILD_DIR)/test_component1: $(BUILD_DIR)/libcomponents.a test_component1.o
	$(LD) $^ $(LIBS) $(LDFLAGS) -o $@

%.o : %.cc
	$(CC) -c $(CPPFLAGS) $(CXXFLAGS) $< -o $@

clean:
	rm -f *.a *.o *.gc*
