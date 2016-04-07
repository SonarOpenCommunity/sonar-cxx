#include <vector>
#include <iostream>
#include <algorithm>
#include <functional>

struct foo {
  int x;
  double y;
};

int four(int a, int b, int f, int d)
{
  return a+f+d*b;
}

int threePlusVar(int a, int, int f, ...)
{
  va_list vl;
  va_start(vl, f);
  if (a == 17)
  {
    //a = va_arg(vl, int); 
  }
  return a + f;
}
extern double some_function(double x);
void three(const double &a, const struct foo &b, int (*fp)(const struct foo &, double &, int) )
{
  double d = some_function(a);
  for (int i = 0 ; i < 4 ; ++i)
  {
    fp(b, d, i);
  }
}

int printf(const char *fmt, ...)
{
}

int leegte(void)
{
  return 0;
}

int ltest1(void)
{
    std::vector<int> c { 1,2,3,4,5,6,7 };
    int x = 5;
    c.erase(std::remove_if(c.begin(), c.end(), [x](int n) { return n < x; } ), c.end());
     std::cout << "c: ";
    for (auto i: c) {
        std::cout << i << ' ';
    }
    std::cout << '\n';
 
    std::function<int (int)> func = [](int i) { return i+4; };
    std::cout << "func: " << func(6) << '\n'; 
    return 0;
}

int ltest2(void)
{
  auto glambda = [](auto a, auto&& b) { return a < b; };
  auto glambda = [](auto a, auto&& b, auto c, auto d) { return (a+b) < (c+d); };
}