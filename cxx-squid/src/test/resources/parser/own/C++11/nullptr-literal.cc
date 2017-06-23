#include <cstddef>
#include <iostream>

char *pc = nullptr;     // OK
int  *pi = nullptr;     // OK
bool   b = nullptr;     // OK. b is false.
int    i = nullptr;     // error

template<class F, class A>
void Fwd(F f, A a)
{
  f(a);
}

void g(int* i)
{
  std::cout << "Function g called\n";
}

void n(int* pi)
{
  std::cout << "Pointer to integer overload\n";
}

void n(double* pd)
{
  std::cout << "Pointer to double overload\n";
}

void n(std::nullptr_t nullp)
{
  std::cout << "null pointer overload\n";
}

int main()
{
  g(NULL);           // Fine
  g(0);              // Fine

  Fwd(g, nullptr);   // Fine
                     //  Fwd(g, NULL);  // ERROR: No function g(int)

  int* pi; double* pd;
  n(pi);             // Fine - integer pointer overload
  n(pd);             // Fine - double pointer overload
  n(nullptr);        // Fine - would be ambiguous without void n(nullptr_t)
                     //  n(NULL);   // ERROR: ambiguous overload
}
