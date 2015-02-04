# Root directory of google test framework
GTEST_DIR = gtest-1.7.0

CPPFLAGS = -I$(GTEST_DIR)

all: libgtest.a
	@$(MAKE) -C unittests

libgtest.a:
	$(CC) $(CPPFLAGS) -c $(GTEST_DIR)/gtest/gtest-all.cc -o gtest-all.o
	$(CC) $(CPPFLAGS) -c $(GTEST_DIR)/gtest/gtest_main.cc -o gtest_main.o
	$(AR) -r libgtest.a gtest-all.o gtest_main.o

clean:
	rm -f *.a *.o *.gc*
	@$(MAKE) clean -C unittests
