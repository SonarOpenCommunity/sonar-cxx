#include <iostream>
#include <type_traits>
#include <string>

template<class T, std::size_t N>
class static_vector
{
  // properly aligned uninitialized storage for N T's
  typename std::aligned_storage<sizeof(T), alignof(T)>::type data[N];
  std::size_t m_size = 0;

public:
  // Create an object in aligned storage
  template<typename ...Args> void emplace_back(Args&&... args)
  {
    if (m_size >= N) // possible error handling
      throw std::bad_alloc{};
    new(data + m_size) T(std::forward<Args>(args)...);
    ++m_size;
  }

  // Access an object in aligned storage
  const T& operator[](std::size_t pos) const
  {
    return *reinterpret_cast<const T*>(data + pos);
  }

  // Delete objects from aligned storage
  ~static_vector()
  {
    for (std::size_t pos = 0; pos < m_size; ++pos) {
      reinterpret_cast<const T*>(data + pos)->~T();
    }
  }
};

int main()
{
  static_vector<std::string, 10> v1;
  v1.emplace_back(5, '*');
  v1.emplace_back(10, '*');
  std::cout << v1[0] << '\n' << v1[1] << '\n';
}
