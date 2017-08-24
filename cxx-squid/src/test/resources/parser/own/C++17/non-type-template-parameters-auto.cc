template<auto n> struct B { /* ... */ };
B<5> b1; // OK: template parameter type is int
B<'a'> b2; // OK: template parameter type is char

template <auto v>    class Y { };
template <auto* p>   class Y<p> { };
template <auto** pp> class Y<pp> { };

template <auto* p0>   void g(Y<p0>);
template <auto** pp0> void g(Y<pp0>);

template <auto value> constexpr auto TConstant = value;
constexpr auto const MySuperConst = TConstant <100>;

// example of heterogeneous compile time list
template <auto ... vs> struct HeterogenousValueList {};
using MyList = HeterogenousValueList<'a', 100, 'b'>;
