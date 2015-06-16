#include <string>
#include <ios>
#include <type_traits>

// type alias, identical to
// typedef std::ios_base::fmtflags flags;
using flags = std::ios_base::fmtflags;
// the name 'flags' now denotes a type:
flags fl = std::ios_base::dec;

// type alias, identical to
// typedef void (*func)(int, int);
using func = void(*) (int, int);
// the name 'func' now denotes a pointer to function:
void example(int, int) {}
func fn = example;

// template type alias
template<class T> using ptr = T*;
// the name 'ptr<T>' is now an alias for pointer to T
ptr<int> x;

// type alias used to hide a template parameter 
template <class CharT> using mystring =
std::basic_string<CharT, std::char_traits<CharT>>;
mystring<char> str;

// type alias can introduce a member typedef name
template<typename T>
struct Container {
  using value_type = T;
};
// which can be used in generic programming
template<typename Container>
void fn2(const Container& c)
{
  typename Container::value_type n;
}

// type alias used to simplify the syntax of std::enable_if
template <typename T> using Invoke =
typename T::type;
template <typename Condition> using EnableIf =
Invoke<std::enable_if<Condition::value>>;
template <typename T, typename = EnableIf<std::is_polymorphic<T>>>
int fpoly_only(T t) { return 1; }

struct S { virtual ~S() {} };
int main()
{
  Container<int> c;
  fn2(c); // Container::value_type will be int in this function

          //    fpoly_only(c); // error, enable_if prohibits this
  S s;
  fpoly_only(s); // okay, enable_if allows this
}
