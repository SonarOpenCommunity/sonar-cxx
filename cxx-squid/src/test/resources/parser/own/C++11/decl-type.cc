#include <iostream>

struct A {
  double x;
};
const A* a = new A{ 0 };

decltype(a->x) x3;       // type of x3 is double (declared type)
decltype((a->x)) x4 = x3;  // type of x4 is const double& (lvalue expression)

//ToDo - make this work
//template <class T, class U>
//auto add(T t, U u) -> decltype(t + u); // return type depends on template parameters

int main()
{
  int i = 33;
  decltype(i) j = i * 2;

  std::cout << "i = " << i << ", "
    << "j = " << j << '\n';

  auto f = [](int a, int b) -> int {
    return a*b;
  };

  decltype(f) f2 = f; // the type of a lambda function is unique and unnamed
  i = f(2, 2);
  j = f2(3, 3);

  std::cout << "i = " << i << ", "
    << "j = " << j << '\n';
}
