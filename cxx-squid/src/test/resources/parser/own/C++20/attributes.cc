// C++ attribute: likely, unlikely (since C++20)
int f(int i)
{
    switch (i)
    {
    case 1: [[fallthrough]];
    [[likely]] case 2: return 1;
    }
    return 2;
}

int f(int i)
{
    if (i < 0) [[unlikely]] {
        return 0;
    }

    return 1;
}

// C++ attribute: no_unique_address (since C++20)
struct Y {
    int i;
    [[no_unique_address]] Empty e;
};

// nodiscard( string-literal ) (since C++20):
[[nodiscard("PURE FUN")]] int strategic_value(int x, int y)
{
    return x ^ y;
}
