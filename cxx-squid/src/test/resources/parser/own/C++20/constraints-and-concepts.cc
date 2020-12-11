#include <string>
#include <cstddef>
#include <concepts>
#include <type_traits>

using namespace std::literals;

//
// concept
//
template <class T, class U>
concept Derived = std::is_base_of<U, T>::value;

//
// constraints
//
template<Incrementable T>
void f(T) requires Decrementable<T>;

//
// derived
//
template <class T, class U>
concept Derived = std::is_base_of<U, T>::value;
 
template<Derived<Base> T>
void f(T);  // T is constrained by Derived<T, Base>

//
// conjunctions
//
template <class T>
concept Integral = std::is_integral<T>::value;
template <class T>
concept SignedIntegral = Integral<T> && std::is_signed<T>::value;
template <class T>
concept UnsignedIntegral = Integral<T> && !SignedIntegral<T>;

//
// disjunctions
//
template <class T = void>
    requires EqualityComparable<T> || Same<T, void>
struct equal_to;

//
// atomic constraints
//
template<typename T>
struct S {
    constexpr operator bool() const { return true; }
};
template<typename T>
    requires (S<T>{})

template<class T> constexpr bool is_meowable = true;
template<class T> constexpr bool is_cat = true;
 
template<class T>
concept Meowable = is_meowable<T>;
 
template<class T>
concept BadMeowableCat = is_meowable<T> && is_cat<T>;
 
template<class T>
concept GoodMeowableCat = Meowable<T> && is_cat<T>;

//
// constraint normalization 
//
template<typename T> concept A = T::value || true;
template<typename U> 
concept B = A<U*>; // OK: normalized to the disjunction of 
                   // - T::value (with mapping T -> U*) and
                   // - true (with an empty mapping).
                   // No invalid type in mapping even though
                   // T::value is ill-formed for all pointer types
 
template<typename V> 
concept C = B<V&>; // Normalizes to the disjunction of
                   // - T::value (with mapping T-> V&*) and
                   // - true (with an empty mapping).
                   // Invalid type V&* formed in mapping => ill-formed NDR

//
// requires clauses
//
template<typename T>
void f(T&&) requires Eq<T>; // can appear as the last element of a function declarator
 
template<typename T> requires Addable<T> // or right after a template parameter list
T add(T a, T b) { return a + b; }

//
// requires expressions
//
template<typename T>
concept Addable = requires (T x) { x + x; }; // requires-expression
 
template<typename T> requires Addable<T> // requires-clause, not requires-expression
T add(T a, T b) { return a + b; }
 
template<typename T>
    requires requires (T x) { x + x; } // ad-hoc constraint, note keyword used twice
T add(T a, T b) { return a + b; }

//
// simple requirements
//
template<typename T>
concept Addable =
requires (T a, T b) {
    a + b; // "the expression a+b is a valid expression that will compile"
};
 
template <class T, class U = T>
concept Swappable = requires(T&& t, U&& u) {
    swap(std::forward<T>(t), std::forward<U>(u));
    swap(std::forward<U>(u), std::forward<T>(t));
};

//
// type requirements
//
template<typename T> using Ref = T&;
template<typename T> concept C =
requires {
    typename T::inner; // required nested member name
    typename S<T>;     // required class template specialization
    typename Ref<T>;   // required alias template substitution
};
 
template <class T, class U> using CommonType = std::common_type_t<T, U>;
template <class T, class U> concept Common =
requires (T&& t, U&& u) {
    typename CommonType<T, U>; // CommonType<T, U> is valid and names a type
    { CommonType<T, U>{std::forward<T>(t)} }; 
    { CommonType<T, U>{std::forward<U>(u)} }; 
};

//
// compound requirements
//
template<typename T> concept C2 =
requires(T x) {
    {*x} -> std::convertible_to<typename T::inner>; // the expression *x must be valid
                                                    // AND the type T::inner must be valid
                                                    // AND the result of *x must be convertible to T::inner
    {x + 1} -> std::same_as<int>; // the expression x + 1 must be valid 
                               // AND std::same_as<decltype((x + 1)), int> must be satisfied
                               // i.e., (x + 1) must be a prvalue of type int
    {x * 1} -> std::convertible_to<T>; // the expression x * 1 must be valid
                                       // AND its result must be convertible to T
};

//
// nested requirements
//
template <class T>
concept Semiregular = DefaultConstructible<T> &&
    CopyConstructible<T> && Destructible<T> && CopyAssignable<T> &&
requires(T a, size_t n) {  
    requires Same<T*, decltype(&a)>;  // nested: "Same<...> evaluates to true"
    { a.~T() } noexcept;  // compound: "a.~T()" is a valid expression that doesn't throw
    requires Same<T*, decltype(new T)>; // nested: "Same<...> evaluates to true"
    requires Same<T*, decltype(new T[n])>; // nested
    { delete new T };  // compound
    { delete new T[n] }; // compound
};
 


 
// Declaration of the concept "Hashable", which is satisfied by
// any type 'T' such that for values 'a' of type 'T',
// the expression std::hash<T>{}(a) compiles and its result is convertible to std::size_t
template<typename T>
concept Hashable = requires(T a) {
    { std::hash<T>{}(a) } -> std::convertible_to<std::size_t>;
};
 
struct meow {};
 
template<Hashable T>
void f(T); // constrained C++20 function template
 
// Alternative ways to apply the same constraint:
// template<typename T>
//    requires Hashable<T>
// void f(T); 
// 
// template<typename T>
// void f(T) requires Hashable<T>; 
 
int main() {
  f("abc"s); // OK, std::string satisfies Hashable
  //f(meow{}); // Error: meow does not satisfy Hashable
}
