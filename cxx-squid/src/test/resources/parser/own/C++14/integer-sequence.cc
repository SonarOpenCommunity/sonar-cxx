#include <tuple>
#include <iostream>
#include <array>
#include <utility>

// Convert array into a tuple
template<typename Array, std::size_t... I>
auto a2t_impl(const Array& a, std::index_sequence<I...>)
-> decltype(std::make_tuple(a[I]...))
{
  return std::make_tuple(a[I]...);
}

template<typename T, std::size_t N, typename Indices = std::make_index_sequence<N>>
auto a2t(const std::array<T, N>& a)
-> decltype(a2t_impl(a, Indices()))
{
  return a2t_impl(a, Indices());
}

// pretty-print a tuple (from http://stackoverflow.com/a/6245777/273767)

template<class Ch, class Tr, class Tuple, std::size_t... Is>
void print_tuple_impl(std::basic_ostream<Ch, Tr>& os,
  const Tuple & t,
  std::index_sequence<Is...>)
{
  using swallow = int[]; // guaranties left to right order
  (void)swallow {
    0, (void(os << (Is == 0 ? "" : ", ") << std::get<Is>(t)), 0)...
  };
}


/* ToDo 
35: ::basic_ostream<Ch, Tr>& os, const std::tuple<Args...>& t)
-->->std::basic_ostream<Ch, Tr> &
37: {
  38:   os << "(";
  39:   print_tuple_impl(os, t, std::index_sequence_for<Args...>{});
  40:   return os << ")";
  41: }
42 :
  43 : int

  at org.sonar.sslr.internal.vm.Machine.parse(Machine.java:74)
  at com.sonar.sslr.impl.Parser.parse(Parser.java:87)
  at com.sonar.sslr.impl.Parser.parse(Parser.java:72)
  at org.sonar.cxx.parser.CxxParserTest.testParsingOnDiverseSourceFiles(CxxParserTest.java:54)
*/
//template<class Ch, class Tr, class... Args>
//auto operator<<(std::basic_ostream<Ch, Tr>& os, const std::tuple<Args...>& t)
//-> std::basic_ostream<Ch, Tr>&
//{
//  os << "(";
//  print_tuple_impl(os, t, std::index_sequence_for<Args...>{});
//  return os << ")";
//}
//
//int main()
//{
//  std::array<int, 4> array = { 1,2,3,4 };
//
//  // convert an array into a tuple
//  auto tuple = a2t(array);
//  static_assert(std::is_same<decltype(tuple),
//    std::tuple<int, int, int, int >> ::value, "");
//
//  // print it to cout
//  std::cout << tuple << '\n';
//}
