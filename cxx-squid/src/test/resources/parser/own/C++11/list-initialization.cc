#include <iostream>
#include <vector>
#include <map>
#include <string>

struct Object {
    float first;
    int second;
};

Object scalar = {0.43f, 10}; // One Object, with first=0.43f and second=10
Object anArray[] = {{13.4f, 3}, {43.28f, 29}, {5.934f, 17}}; // An array of three Objects

class SequenceClass {
public:
    SequenceClass(std::initializer_list<int> list){};
};
SequenceClass some_var = {1, 4, 5, 6};

struct Foo {
  std::vector<int> mem = { 1,2,3 }; // list-initialization of a non-static member
  std::vector<int> mem2;
  Foo() : mem2{ -1, -2, -3 } {} // list-initialization of a member in constructor
};

std::vector<std::string> v1 = { "xyzzy", "plugh", "abracadabra" };
std::vector<std::string> v2({ "xyzzy", "plugh", "abracadabra" });
std::vector<std::string> v3{ "xyzzy", "plugh", "abracadabra" }; // see "Uniform initialization" below


std::pair<std::string, std::string> f(std::pair<std::string, std::string> p)
{
  return{ p.second, p.first }; // list-initialization in return statement
}

template <typename T>
std::string* string_creator()
{
    return new T();
}

int main()
{
  int n0{};     // value-initialization (to zero)
  int n1{ 1 };    // direct-list-initialization
  std::string s1{ 'a', 'b', 'c', 'd' }; // initializer-list constructor call
  std::string s2{ s1, 2, 2 };           // regular constructor call
  std::string s3{ 0x61, 'a' }; // initializer-list ctor is preferred to (int, char)

  int n2 = { 1 }; // copy-list-initialization
  double d = double{ 1.2 }; // list-initialization of a temporary, then copy-init

  std::map<int, std::string> m = { // nested list-initialization
    { 1, "a" },
    { 2,{ 'a', 'b', 'c' } },
    { 3, s1 }
  };

  std::cout << f({ "hello", "world" }).first // list-initialization in function call
    << '\n';

  const int(&ar)[2] = { 1,2 }; // binds a lvalue reference to a temporary array
  int&& r1 = { 1 }; // binds a rvalue reference to a temporary int
                    //  int& r2 = {2}; // error: cannot bind rvalue to a non-const lvalue ref

                    //  int bad{1.0}; // error: narrowing conversion
  unsigned char uc1{ 10 }; // okay
                           //  unsigned char uc2{-1}; // error: narrowing conversion

  Foo f;

  std::cout << n0 << ' ' << n1 << ' ' << n2 << '\n'
    << s1 << ' ' << s2 << ' ' << s3 << '\n';
  for (auto p : m)
    std::cout << p.first << ' ' << p.second << '\n';
  for (auto n : f.mem)
    std::cout << n << ' ';
  for (auto n : f.mem2)
    std::cout << n << ' ';

  static const struct
  {
    std::string* (*create)();
  }
  strings[] =
  {
    &string_creator<std::string>
  };

  for(auto const& s: strings)
    std::cout << s.create();

}
