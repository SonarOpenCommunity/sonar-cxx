#include <stdarg.h>
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