#include <vector>
#include <iostream>
#include <algorithm>
#include <functional>

void example1()
{
  auto func1 = [] (int x, int y) -> int { return x + y; };
  auto func2 = [] ( int x ) -> int { return f( g( x ) ); }; // -> int is optional
  auto func3 = [] ( int x ) { return ( x + 1 ) * 2; };
}

// generic lambda, operator() is a template with one parameter
auto vglambda = [](auto printer) {
    return [=](auto&&... ts) { // generic lambda, ts is a parameter pack
        printer(std::forward<decltype(ts)>(ts)...);
        return [=]{ printer(ts...); }; // nullary lambda (takes no parameters)
    };
};
auto p = vglambda( [](auto v1, auto v2, auto v3) {
    std::cout << v1 << v2 << v3;
} );

void example2()
{
    auto q = p(1, 'a', 3.14); // outputs 1a3.14
    q();                      // outputs 1a3.14

    // generic lambda, operator() is a template with two parameters
    auto glambda = [](auto a, auto&& b) { return a < b; };
    bool b = glambda(3, 3.14); // OK
}

void example3() {
    float x, &r = x;
    [=] { // x and r are not captured (appearance in a decltype operand is not an odr-use)
        decltype(x) y1; // y1 has type float
        decltype((x)) y2 = y1; // y2 has type float const& because this lambda
        // is not mutable and x is an lvalue
        decltype(r) r1 = y1;   // r1 has type float& (transformation not considered)
        decltype((r)) r2 = y2; // r2 has type float const&
    };
}

struct X {
    int x, y;
    int operator()(int);
    void f() {
        // the context of the following lambda is the member function X::f
        [=]()->int {
            return operator()(this->x + y); // X::operator()(this->x + (*this).y)
            // this has type x*
        };
    }
};

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
