#include <iostream>
#include <array>

template<typename... Ts>
constexpr auto make_array(Ts&&... ts)
->std::array<std::common_type_t<Ts...>, sizeof...(ts)>
{
  return{ std::forward<Ts>(ts)... };
}

int main()
{
  auto b = make_array(1, 2, 3);
  std::cout << b.size() << '\n';
  for (auto i : b)
    std::cout << i << ' ';
}
