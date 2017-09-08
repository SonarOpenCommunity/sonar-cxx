#include <type_traits>
 
template <class T>
void swap(T& a, T& b)
{
    static_assert(std::is_copy_constructible<T>::value,
                  "Swap requires copying");
    static_assert(std::is_nothrow_copy_constructible<T>::value
               && std::is_nothrow_copy_assignable<T>::value,
                  "Swap may throw");
    auto c = b;
    b = a;
    a = c;
}
 
template <class T>
struct data_structure
{
    static_assert(std::is_default_constructible<T>::value,
                  "Data Structure requires default-constructible elements");
};
 
struct no_copy
{
    no_copy ( const no_copy& ) = delete;
    no_copy () = default;
};
 
struct no_default
{
    no_default () = delete;
};

int main()
{
    int a, b;
    swap(a, b);
 
    no_copy nc_a, nc_b;
    swap(nc_a, nc_b); // error: static assertion failed: Swap requires copying
 
    data_structure<int> ds_ok;
    data_structure<no_default> ds_error; // error: static assertion failed: Data Structure requires default-constructible elements
}

// C++17 without message
extern void foo(void*);
void f()
{
    int i;
    // does int fit into void* ?
    static_assert(sizeof(void *) >= sizeof i);
    foo((void *) i);
}
