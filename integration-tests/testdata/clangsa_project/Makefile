export CC = g++
export LD = g++

all:
	@$(MAKE) -C src

clean:
	@$(MAKE) clean -C src

analyze:
	clang -cc1 -analyze -analyzer-checker=core.DivideZero src/divzero.cc -I src -analyzer-output plist-multi-file -o divzero.plist
	clang -cc1 -analyze -analyzer-checker=deadcode.DeadStores src/unused.cc -I src -analyzer-output plist-multi-file -o unused.plist

sonar:
	sonar-scanner -X
