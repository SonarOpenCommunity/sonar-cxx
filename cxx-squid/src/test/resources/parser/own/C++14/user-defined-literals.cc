#include <string>
long double operator "" _w(long double);
std::string operator "" _w(const char16_t*, size_t);
unsigned operator "" _w(const char*);
void func1() {
    1.2_w; // calls operator "" _w(1.2L)
    u"one"_w; // calls operator "" _w(u"one", 3)
    12_w; // calls operator "" _w("12")
}

// Literal operators
template <char...> double operator "" _x();

void operator "" _km(long double); // OK, will be called for 1.0_km
std::string operator "" _i18n(const char*, std::size_t); // OK
//todo: template <char...> double operator "" _π(); // OK
float operator ""_e(const char*); // OK
double operator"" _Z(long double); // error: all names that begin with underscore
                                   // followed by uppercase letter are reserved
double operator""_Z(long double); // OK: even though _Z is reserved ""_Z is allowed
long double operator""_E(long double);
long double operator""_a(long double);
int operator""_p(unsigned long long);
auto y = 1.0_a + 2.0;   // OK
auto z = 1.0_E + 2.0;  // OK
auto q = (1.0_E) + 2.0; // OK
auto u = 1_p + 2;      // OK



#include <chrono>
using namespace std::literals;
auto b = 4s.count();  // OK
auto c = (4s).count(); // OK



#include <iostream> 
// used as conversion
constexpr long double operator"" _deg(long double deg)
{
    return deg * 3.14159265358979323846264L / 180;
}

// used with custom type
struct mytype
{
    unsigned long long m;
};
constexpr mytype operator"" _mytype(unsigned long long n)
{
    return mytype{ n };
}

// used for side-effects
void operator"" _print(const char* str)
{
    std::cout << str;
}

void func2() {
    double x = 90.0_deg;
    std::cout << std::fixed << x << '\n';
    mytype y = 123_mytype;
    std::cout << y.m << '\n';
    0x123ABC_print;
}

using std::string_view_literals::operator""sv;

constexpr int operator ""h(unsigned long long h)
{
    return 0;
}
