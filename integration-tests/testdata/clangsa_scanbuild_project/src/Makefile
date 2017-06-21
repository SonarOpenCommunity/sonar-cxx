CPPFLAGS = -I.

all: divzero unused

divzero:
	$(CXX) $(CPPFLAGS) divzero.cc -o $@

unused:
	$(CXX) $(CPPFLAGS) unused.cc -o $@

clean:
	rm -f *.o divzero
	rm -f *.o unused
