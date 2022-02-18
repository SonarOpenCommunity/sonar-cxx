// C++14 type traits helper were added (issue #2305)

// using types trait with/without helper with/without default value
template <typename T, typename std::enable_if  <std::is_base_of<BaseT, T>::value, int>::type  = 0> class A {};
template <typename T, typename std::enable_if  <std::is_base_of<BaseT, T>::value, int>::type>      class B {};
template <typename T,          std::enable_if_t<std::is_base_of<BaseT, T>::value, int>        = 0> class C {};
template <typename T,          std::enable_if_t<std::is_base_of<BaseT, T>::value, int>>            class D {};


template <typename Collection, std::enable_if_t<std::is_convertible_v<Collection, interface_type>>* = nullptr> class E {};
template <typename Collection, std::enable_if<std::is_convertible_v<Collection, interface_type>>::type* = nullptr> class F {};
template <class T, typename std::enable_if  <!std::is_trivially_destructible<T>{} && (std::is_class<T>{} || std::is_union<T>{}), bool>::type = true> class G {};
template <class T,          std::enable_if_t<!std::is_trivially_destructible<T>{} && (std::is_class<T>{} || std::is_union<T>{}), bool>       = true> class H {};


struct I {
    template<typename T, std::enable_if_t< std::is_integral_t<T>, int> = 0 >
    I( T num ) {}

    template<typename T, std::enable_if_t< std::is_integral_v<T>, int> = 0 >
    int foo( T num ) {}
};


template< typename T, std::enable_if_t< std::is_integral_v<T>, int> >
A operator+( T num ) {}

template< typename T, std::enable_if_t< std::is_integral_v<T>, int> = 0>
A operator+( T num ) {}
