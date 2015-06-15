#include <iostream>
#include <type_traits>

class A {};

int main()
{
  std::cout << std::alignment_of<A>::value << '\n';
  std::cout << std::alignment_of<int>::value << '\n';
  std::cout << std::alignment_of<double>::value << '\n';
}
