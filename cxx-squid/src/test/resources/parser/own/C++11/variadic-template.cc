// A template parameter pack
template<class ... Types>
struct Tuple { };

// A function parameter pack
template<class ... Types>
void f(Types ... args);

template<typename T, typename... Targs>
void tprintf(const char* format, T value , Targs... Fargs)
{
   tprintf(format + 1, Fargs...);
}

template<class ... Args>
void g(Args ... args) {
   f(const_cast<const Args*>(&args)...);
   f(h(args ...) + args ...);
}

template<typename T, T... Ns>
struct integer_sequence {
    template< class U >
    struct rebind {
        typedef integer_sequence<U, static_cast<U>(Ns)...> type;
    };
};
