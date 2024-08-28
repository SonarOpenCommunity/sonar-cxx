constexpr bool is_constant_evaluated() noexcept
{
    if consteval { return true; } else { return false; }
}
 
constexpr bool is_runtime_evaluated() noexcept
{
    if !consteval { return true; } else { return false; }
}
 
consteval std::uint64_t ipow_ct(std::uint64_t base, std::uint8_t exp)
{
    if (!base) return base;
    std::uint64_t res{1};
    while (exp)
    {
        if (exp & 1) res *= base;
        exp /= 2;
        base *= base;
    }
    return res;
}
 
constexpr std::uint64_t ipow(std::uint64_t base, std::uint8_t exp)
{
    if consteval // use a compile-time friendly algorithm
    {
        return ipow_ct(base, exp);
    }
    else // use runtime evaluation
    {
        return std::pow(base, exp);
    }
}
 
int main(int, const char* argv[])
{
    static_assert(ipow(0, 10) == 0 && ipow(2, 10) == 1024);
    std::cout << ipow(std::strlen(argv[0]), 3) << '\n';
}
