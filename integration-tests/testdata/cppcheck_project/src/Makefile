CPPFLAGS = -I.

OBJECTS = component1.o main.o

app: $(OBJECTS)
	$(LD) $(LDFLAGS) $(OBJECTS) -o $@

clean:
	rm -f *.o app

%.o : %.cc
	$(CC) $(CPPFLAGS) $(CXXFLAGS) -c $< -o $@
