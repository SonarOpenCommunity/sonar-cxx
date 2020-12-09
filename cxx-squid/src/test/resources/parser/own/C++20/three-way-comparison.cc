#include <compare>
#include <iostream>
 
int test() {
    double foo = -0.0;
    double bar = 0.0;
 
    auto res = foo <=> bar;
 
    if (res < 0)
        std::cout << "-0 is less than 0";
    else if (res == 0)
        std::cout << "-0 and 0 are equal";
    else if (res > 0)
        std::cout << "-0 is greater than 0";
}

struct MyInt {
    int value;
    explicit MyInt(int val): value{val} { }
    auto operator<=>(const MyInt& rhs) const {           // (1)      
        return value <=> rhs.value;
    }
};

struct MyDouble {
    double value;
    explicit constexpr MyDouble(double val): value{val} { }
    auto operator<=>(const MyDouble&) const = default;   // (2)
};

