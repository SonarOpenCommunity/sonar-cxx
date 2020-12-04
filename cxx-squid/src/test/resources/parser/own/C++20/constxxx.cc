//
// consteval - specifies that a function is an immediate function, that is, every call to the function must produce a compile-time constant 
//

consteval int sqr(int n) {
  return n*n;
}
constexpr int r = sqr(100);  // OK
 
//int x = 100;
//int r2 = sqr(x);  // Error: Call does not produce a constant
 
consteval int sqrsqr(int n) {
  return sqr(sqr(n)); // Not a constant expression at this point, but OK
}
 
//constexpr int dblsqr(int n) {
//  return 2*sqr(n); // Error: Enclosing function is not consteval and sqr(n) is not a constant
//}

consteval int f() { return 42; }
consteval auto g() { return &f; }
consteval int h(int (*p)() = g()) { return p(); }
constexpr int r = h();   // OK
//constexpr auto e = g();  // ill-formed: a pointer to an immediate function is not a permitted result of a constant expression

//
// constinit - asserts that a variable has static initialization, i.e. zero initialization and constant initialization, otherwise the program is ill-formed. 
//

const char *g() { return "dynamic initialization"; }
constexpr const char *f(bool p) { return p ? "constant initializer" : g(); }
 
constinit const char *c = f(true); // OK
// constinit const char *d = f(false); // error

//
// constinit can also be used in a non-initializing declaration to tell the compiler that a thread_local variable is already initialized. 
//

extern thread_local constinit int x;
int f() { return x; } // no check of a guard variable needed
