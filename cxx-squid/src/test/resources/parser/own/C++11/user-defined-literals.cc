#include <iostream>

//ToDo - make this work
// used as conversion
//constexpr long double operator"" _deg(long double deg)
//{
//  return deg*3.141592 / 180;
//}
//
//// used with custom type
//struct mytype
//{
//  mytype(unsigned long long m) :m(m) {}
//  unsigned long long m;
//};
//mytype operator"" _mytype(unsigned long long n)
//{
//  return mytype(n);
//}
//
//// used for side-effects
//void operator"" _print(const char* str)
//{
//  std::cout << str;
//}
//
//int main() {
//  double x = 90.0_deg;
//  std::cout << std::fixed << x << '\n';
//  mytype y = 123_mytype;
//  std::cout << y.m << '\n';
//  0x123ABC_print;
//}
