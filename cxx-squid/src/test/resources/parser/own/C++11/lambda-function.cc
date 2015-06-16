#include <vector>
#include <iostream>
#include <algorithm>
#include <functional>

int main()
{
  std::vector<int> c{ 1,2,3,4,5,6,7 };
  int x = 5;
  c.erase(std::remove_if(c.begin(), c.end(), [x](int n) { return n < x; }), c.end());

  std::cout << "c: ";
  for (auto i : c) {
    std::cout << i << ' ';
  }
  std::cout << '\n';

  // the type of a closure cannot be named, but can be inferred with auto
  auto func1 = [](int i) { return i + 4; };
  std::cout << "func1: " << func1(6) << '\n';

  // like all callable objects, closures can be captured in std::function
  // (this may incur unnecessary overhead)
  std::function<int(int)> func2 = [](int i) { return i + 4; };
  std::cout << "func2: " << func2(6) << '\n';
}
