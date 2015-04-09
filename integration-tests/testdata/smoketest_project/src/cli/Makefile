CPPFLAGS = -I.. -I../../3rdparty

OBJECTS = main.o
$(BUILD_DIR)/app: $(OBJECTS)
	$(LD) $(LDFLAGS) $(OBJECTS) $(BUILD_DIR)/libcomponents.a -o $@

%.o : %.cc
	$(CC) $(CPPFLAGS) $(CXXFLAGS) -c $< -o $@

clean:
	rm -f *.o *.gc*
