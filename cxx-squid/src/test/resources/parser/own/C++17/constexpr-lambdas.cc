auto add1 = [](int a, int b) constexpr { return a + b; };
int arr[add1(1, 2)];

auto monoid = [](auto v) { return [=] { return v; }; };
auto add2 = [](auto m1) constexpr {
    auto ret = m1();
    return [=](auto m2) mutable {
        auto m1val = m1();
        auto plus = [=](auto m2val) mutable constexpr
        { return m1val += m2val; };
        ret = plus(m2());
        return monoid(ret);
    };
};
