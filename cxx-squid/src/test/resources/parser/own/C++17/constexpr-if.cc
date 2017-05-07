template <typename T>
auto get_value(T t) {
    if constexpr (std::is_pointer_v<T>)
        return *t; // deduces return type to int for T = int*
    else
        return t;  // deduces return type to int for T = int
}

extern int x; // no definition of x required
int f() {
    if constexpr (true)
        return 0;
    else if (x)
        return x;
    else
        return -x;
}

template<typename T, typename ... Rest>
void g(T&& p, Rest&& ...rs) {
    // ... handle p
    if constexpr (sizeof...(rs) > 0)
        g(rs...); // never instantiated with an empty argument list.
}

template<class T> void g() {
    auto lm = [](auto p) {
        if constexpr (sizeof(T) == 1 && sizeof p == 1) {
            // this condition remains value-dependent after instantiation of g<T>
        }
    };
}

// todo
//template<class T> struct dependent_false : std::false_type {};
//template <typename T>
//void f() {
//    if constexpr (std::is_arithmetic_v<T>)
//        // ...
//    else
//        static_assert(dependent_false<T>::value, "Must be arithmetic"); // ok
//}
