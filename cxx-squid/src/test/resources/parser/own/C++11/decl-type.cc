#include <iostream>
#include <vector>

struct A {
    double x;
};
const A* a = new A{ 0 };

decltype(a->x) x3;       // type of x3 is double (declared type)
decltype((a->x)) x4 = x3;  // type of x4 is const double& (lvalue expression)

auto add_int( int a, int b ) -> int { return a + b; }

template <class T, class U>
auto add(T t, U u) -> decltype(t + u); // return type depends on template parameters

int example()
{
    const std::vector<int> v(1);
    auto a = v[0];        // a has type int
    decltype(v[1]) b = 1; // b has type const int&, the return type of
    //   std::vector<int>::operator[](size_type) const
    auto c = 0;           // c has type int
    auto d = c;           // d has type int
    decltype(c) e;        // e has type int, the type of the entity named by c
    decltype((c)) f = c;  // f has type int&, because (c) is an lvalue
    decltype(0) g;        // g has type int, because 0 is an rvalue
}

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
