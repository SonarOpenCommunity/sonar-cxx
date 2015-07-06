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

int main()
{
  g(NULL);           // Fine
  g(0);              // Fine

  Fwd(g, nullptr);   // Fine
                     //  Fwd(g, NULL);  // ERROR: No function g(int)
}
