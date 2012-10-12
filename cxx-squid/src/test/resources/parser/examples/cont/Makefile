OUTPROGS =	vector1 deque1 list1 \
		set2 set1 mset1 setcmp \
		map1 mmap1 mapfind mapcmp \
		array1 carray1 refsem1 \
		stack1 stack2 queue1 queue2 pqueue1 \
		bitset2

CPPPROGS =	sortset sortvec bitset1

HEADERS =	newkey.hpp carray.hpp countptr.hpp Stack.hpp Queue.hpp print.hpp

include ../Makefile.h


refsem1: countptr.hpp refsem1.cpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $@.cpp -o $@

carray1: carray.hpp carray1.cpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $@.cpp -o $@

stack2: Stack.hpp stack2.cpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $@.cpp -o $@

queue2: Queue.hpp queue2.cpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $@.cpp -o $@

