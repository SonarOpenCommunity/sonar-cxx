# Flags passed to the C++ compiler.
export CXXFLAGS = -g -pthread --coverage

# Flags passed to the linker.
export LDFLAGS = -pthread --coverage

export CC = g++
export LD = g++

all:
	@$(MAKE) -C src
	src/unit-test
	gcovr -x -r . > ut-coverage.xml
	rm -f src/*.gcda
	src/integration-test
	gcovr -x -r . > it-coverage.xml
	src/unit-test
	gcovr -x -r . > overall-coverage.xml

clean:
	@$(MAKE) clean -C src
