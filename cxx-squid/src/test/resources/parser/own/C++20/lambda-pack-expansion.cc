#include <utility>

template <class... Args>
auto delay_invoke_foo(Args... args) {
    return[...args = std::move(args)]() -> decltype(auto) {
        return foo(args...);
    };
}

template <typename... Args>
void foo(Args... args) {
    [...xs = args] {
        bar(xs...); // xs is an init-capture pack
    };
}

// generic lambda, operator() is a template with one parameter pack
auto f = []<typename ...Ts>(Ts&& ...ts) {
    return foo(std::forward<Ts>(ts)...);
};
