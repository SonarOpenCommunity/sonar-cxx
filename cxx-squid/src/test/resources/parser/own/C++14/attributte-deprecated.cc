[[deprecated]] int f();

[[deprecated("g() is thread-unsafe. Use h() instead")]]
void g( int& x );

void h( int& x );

void test() {
    int a = f(); // warning: 'f' is deprecated
    g(a); // warning: 'g' is deprecated: g() is thread-unsafe. Use h() instead
}
