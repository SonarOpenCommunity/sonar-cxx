struct A { int x; int y; int z; };
A a{ .x = 1, .z = 2 }; // ok, b.y initialized to 0

union u { int a; const char* b; };
u f = { .b = "asdf" }; // OK, active member of the union is b

struct B {
    string str;
    int n = 42;
    int m = -1;
};
B b{ .m = 21 }; // Initializes str with {}, which calls the default constructor
                // then initializes n with = 42
                // then initializes m with = 21

struct C { int x, y; };
struct D { struct C a; };
struct C a = { .y = 1, .x = 2 }; // valid C, invalid C++ (out of order)
int arr[3] = { [1] = 5 };        // valid C, invalid C++ (array)
struct D d = { .a.x = 0 };       // valid C, invalid C++ (nested)
struct C c = { .x = 1, 2 };      // valid C, invalid C++ (mixed)
