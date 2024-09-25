struct X {
    void foo(this X const& self, int i); // same as void foo(int i) const &;
//  void foo(int i) const &; // Error: already declared
    void bar(this X self, int i); // pass object by value: makes a copy of “*this”
};

struct Y {
    template<typename Self>
    void foo(this Self&&, int);
};
 
struct D : Y {};
 
void ex(Y& y, D& d)
{
    y.foo(1);       // Self = Y&
    move(x).foo(2); // Self = Y
    d.foo(3);       // Self = D&
}

// a CRTP trait
struct add_postfix_increment {
    template<typename Self>
    auto operator++(this Self&& self, int) {
        auto tmp = self; // Self deduces to "some_type"
        ++self;
        return tmp;
    }
};
 
struct some_type : add_postfix_increment {
    some_type& operator++() { /*...*/ }
};
