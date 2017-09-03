// n4618 [dcl.type.class.deduct]

template<class T> struct container {
    container(T t) {}
    template<class Iter> container(Iter beg, Iter end);
};
template<class Iter>
container(Iter b, Iter e) -> container<typename std::iterator_traits<Iter>::value_type>;
std::vector<double> v = { /* ... */ };

container c(7); // OK. Deduces int for T
auto d = container(v.begin(), v.end()); // OK. Deduces double for T
