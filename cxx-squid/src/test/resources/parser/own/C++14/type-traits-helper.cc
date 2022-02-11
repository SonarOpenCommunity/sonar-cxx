// C++14 type traits helper were added (issue #2305)

// using types trait with/without helper with/without default value
template <typename T, typename std::enable_if  <std::is_base_of<BaseT, T>::value, int>::type  = 0> class A {};
template <typename T, typename std::enable_if  <std::is_base_of<BaseT, T>::value, int>::type>      class B {};
template <typename T,          std::enable_if_t<std::is_base_of<BaseT, T>::value, int>        = 0> class C {};
template <typename T,          std::enable_if_t<std::is_base_of<BaseT, T>::value, int>>            class D {};
