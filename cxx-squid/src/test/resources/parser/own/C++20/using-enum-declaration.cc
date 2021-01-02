enum class fruit { orange, apple };
struct S {
  using enum fruit; // OK: introduces orange and apple into S
};
void f()
{
    S s;
    s.orange;  // OK: names fruit::orange
    S::orange; // OK: names fruit::orange
}
