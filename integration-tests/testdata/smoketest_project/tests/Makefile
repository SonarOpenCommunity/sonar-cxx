# Root directory of google test framework
GTEST_DIR = gtest-1.6.0

CPPFLAGS = -I$(GTEST_DIR)/include -I$(GTEST_DIR)
CXXFLAGS = -g

all: libgtest.a
	@$(MAKE) -C unittests

libgtest.a:
	$(CC) $(CPPFLAGS) $(CXXFLAGS) -c $(GTEST_DIR)/src/gtest-all.cc -o gtest-all.o
	$(CC) $(CPPFLAGS) $(CXXFLAGS) -c $(GTEST_DIR)/src/gtest_main.cc -o gtest_main.o
	$(AR) -r libgtest.a gtest-all.o gtest_main.o

clean:
	rm -f *.a *.o *.gc*
	@$(MAKE) clean -C unittests
