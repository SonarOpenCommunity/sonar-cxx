#include <iostream>

// used as conversion
constexpr long double operator"" _deg(long double deg)
{
  return deg*3.141592 / 180;
}

// used with custom type
struct mytype
{
  mytype(unsigned long long m) :m(m) {}
  unsigned long long m;
};
mytype operator"" _mytype(unsigned long long n)
{
  return mytype(n);
}

// used for side-effects
void operator"" _print(const char* str)
{
  std::cout << str;
}

int example1() {
  double x = 90.0_deg;
  std::cout << std::fixed << x << '\n';
  mytype y = 123_mytype;
  std::cout << y.m << '\n';
  0x123ABC_print;
}

long double operator "" _w(long double);
std::string operator "" _w(const char16_t*, std::size_t);
unsigned operator "" _w(const char*);

int example2() {
  1.2_w;    // calls operator "" _w(1.2L)
  u"one"_w; // calls operator "" _w(u"one", 3)
  12_w;     // calls operator "" _w("12")
  "two"_w;  // error: no applicable literal operator
}
