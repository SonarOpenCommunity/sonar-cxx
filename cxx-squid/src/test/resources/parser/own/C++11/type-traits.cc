// issue #733
template <typename LE, typename RE, typename std::enable_if<is_etl_expr<LE>::value, int>::type = 42>
void operator-(LE&& lhs, RE&& rhs){}

// issue #793
template<class T,
         typename std::enable_if<
             !std::is_trivially_destructible<T>{} &&
             (std::is_class<T>{} || std::is_union<T>{}),
            int>::type = 0>
void destroy(T* t)
{
    std::cout << "destroying non-trivially destructible T\n";
    t->~T();
}
