struct S2 { void f(int i); };
void S2::f(int i) {
    [&, i]{ };         // OK
    [&, this, i]{ };   // OK, equivalent to [&, i]
    [=, *this]{ };     // OK
}
