OUTPROGS = itercat advance1 distance swap1 \
		reviter1 reviter2 reviter3 reviter4 \
		backins frontins inserter \
		ostriter \
		assoiter

CPPPROGS = 	istriter advance2 

HEADERS =	distance.hpp assoiter.hpp print.hpp

EXPORT =	istriter.in advance2.in

include ../Makefile.h

assoiter: assoiter.hpp assoiter.cpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $@.cpp -o $@

