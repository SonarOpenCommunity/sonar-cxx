CPPFLAGS = -I..

OBJECTS = main.o component1.o
UT_OBJECTS = unit_test.o component1.o
IT_OBJECTS = integration_test.o component2.o

all: app unit-test integration-test

app: $(OBJECTS)
	$(LD) $(LDFLAGS) $(OBJECTS) -o $@

unit-test: $(UT_OBJECTS)
	$(LD) $(LDFLAGS) $(UT_OBJECTS) -o $@

integration-test: $(IT_OBJECTS)
	$(LD) $(LDFLAGS) $(IT_OBJECTS) -o $@

%.o : %.cc
	$(CC) $(CPPFLAGS) $(CXXFLAGS) -c $< -o $@

clean:
	rm -f *.o *.gc* app unit-test integration-test
