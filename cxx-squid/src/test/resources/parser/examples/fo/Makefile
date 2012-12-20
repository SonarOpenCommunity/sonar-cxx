include ../Makefile.h

OUTPROGS =	sort1 genera1 genera2 foreach3 removeif \
	   	fopow1 \
	  	compose1 compose2 compose3 compose4

CPPPROGS =	memfun1

HEADERS =	fopow.hpp compose11.hpp compose12.hpp compose21.hpp \
		compose22.hpp compose10.hpp nullary.hpp print.hpp


fopow1: fopow.hpp fopow1.cpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $@.cpp -o $@

compose1: print.hpp compose11.hpp compose1.cpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $@.cpp -o $@

compose2: compose21.hpp print.hpp compose2.cpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $@.cpp -o $@

compose3: compose3.cpp compose22.hpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $@.cpp -o $@

