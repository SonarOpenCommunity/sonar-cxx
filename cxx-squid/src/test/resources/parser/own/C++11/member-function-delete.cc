// The following type is non-copyable:
struct NonCopyable {
    NonCopyable() = default;
    NonCopyable(const NonCopyable&) = delete;
    NonCopyable & operator=(const NonCopyable&) = delete;
};

// The = delete specifier can be used to prohibit calling any function, which can be used to disallow calling a member function with particular parameters. For example:
struct NoInt {
    void f(double i);
    void f(int) = delete;
};

// This can be generalized to disallow calling the function with any type other than double as follows:
struct OnlyDouble {
    void f(double d);
    template<class T> void f(T) = delete;
};
