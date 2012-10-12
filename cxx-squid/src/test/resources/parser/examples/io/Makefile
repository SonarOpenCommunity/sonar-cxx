CPPPROGS = io1 sum1 sum2 charcat1 \
	   ignore1 ignoreparam1 charset \
	   cat1 cat2 charcat2 \
	   inbuf1 \
	   copy1 copy2 \
	   countlines

OUTPROGS = rdbuf1 rdbuf2 redirect \
	   rw1 sstr1 \
	   outbuf1 outbuf1x outbuf2 outbuf3 

HEADERS = ignore.hpp ignoreparam.hpp \
          frac1out.hpp frac2out.hpp frac1in.hpp frac2in.hpp \
	  outbuf1.hpp outbuf1x.hpp outbuf2.hpp outbuf3.hpp \
	  inbuf1.hpp

include ../Makefile.h

outbuf1: outbuf1.hpp outbuf1.cpp
	$(CXX) $(CXXFLAGS) $@.cpp $(LDFLAGS) -o $@
outbuf1x: outbuf1x.hpp outbuf1x.cpp
	$(CXX) $(CXXFLAGS) $@.cpp $(LDFLAGS) -o $@
outbuf2: outbuf2.hpp outbuf2.cpp
	$(CXX) $(CXXFLAGS) $@.cpp $(LDFLAGS) -o $@
outbuf3: outbuf3.hpp outbuf3.cpp
	$(CXX) $(CXXFLAGS) $@.cpp $(LDFLAGS) -o $@

ignoreparam1: ignoreparam1.cpp ignoreparam.hpp
	$(CXX) $(CXXFLAGS) $@.cpp $(LDFLAGS) -o $@

