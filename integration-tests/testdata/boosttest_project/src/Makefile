OBJECTS = component1.o
$(BUILD_DIR)/libcomponents.a: $(OBJECTS)
	$(AR) -r $@ $(OBJECTS)

%.o : %.cc
	$(CXX) -c $(CXXFLAGS) $< -o $@

clean:
	rm -f *.o *.gc*
