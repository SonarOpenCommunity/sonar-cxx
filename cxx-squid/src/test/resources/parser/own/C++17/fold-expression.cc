#include <iostream>
#include <vector>
#include <climits>
#include <cstdint>
#include <type_traits>
#include <utility>

// n4618

template<typename... Args>
   bool f(Args... args) { 
      return (true + ... + args);
   } 

template<typename... Args>
   bool all(Args... args) { return (args && ...); }

bool b = all(true, true, true, false);

// cppreference sample

template<typename ...Args>
   void printer(Args&&... args) {
      (std::cout << ... << args) << '\n';
   }

template<typename T, typename... Args>
   void push_back_vec(std::vector<T>& v, Args&&... args)
   {
      (v.push_back(args), ...);
   }

// compile-time endianness swap based on http://stackoverflow.com/a/36937049 
template<class T, std::size_t... N>
constexpr T bswap_impl(T i, std::index_sequence<N...>) {
  return (((i >> N*CHAR_BIT & std::uint8_t(-1)) << (sizeof(T)-1-N)*CHAR_BIT) | ...);
}
template<class T, class U = std::make_unsigned_t<T>>
constexpr U bswap(T i) {
  return bswap_impl<U>(i, std::make_index_sequence<sizeof(T)>{});
}
 
int main()
{
    printer(1, 2, 3, "abc");
 
    std::vector<int> v;
    push_back_vec(v, 6, 2, 45, 12);
    push_back_vec(v, 1, 2, 9);
    for (int i : v) std::cout << i << ' ';
 
    static_assert(bswap<std::uint16_t>(0x1234u)==0x3412u);
    static_assert(bswap<std::uint64_t>(0x0123456789abcdefULL)==0xefcdab8967452301ULL);
}
