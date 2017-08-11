/** fallthrough C++11 **/

[[noreturn]] void f() {
    throw "error";
    // OK
}


/** fallthrough C++17 **/

void f(int n) {
    void g(), h(), i();
    switch (n) {
    case 1:
    case 2:
        g();
        [[fallthrough]];
    case 3: // warning on fallthrough discouraged
        h();
            break;
    }
}


/** nodiscard C++17 **/

struct [[nodiscard]] error_info { /*...*/ };
error_info enable_missile_safety_mode();
void launch_missiles();
void test_missiles() {
    enable_missile_safety_mode(); // warning encouraged
    launch_missiles();
}


/** maybe_unused C++17 **/

[[maybe_unused]] void f([[maybe_unused]] bool thing1,
                        [[maybe_unused]] bool thing2) {
    [[maybe_unused]] bool b = thing1 && thing2;
    assert(b);
}
