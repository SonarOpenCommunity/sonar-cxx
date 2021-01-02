int a;
const int b = 0;
struct S {
    // simple cases
    int x1 : 8 = 42;                 // OK; "= 42" is brace-or-equal-initializer
    int x2 : 8 { 42 };               // OK; "{ 42 }" is brace-or-equal-initializer
    // ambiguities
    int y1 : true ? 8 : a = 42;      // OK; brace-or-equal-initializer is absent
    int y3 : (true ? 8 : b) = 42;    // OK; "= 42" is brace-or-equal-initializer
    int z : 1 || new int { 0 };      // OK; brace-or-equal-initializer is absent
};
