char xdigit1(int n) {
  static constexpr char digits[] = "0123456789abcdef";
  return digits[n];
}

constexpr char xdigit2(int n) {
  static constexpr char digits[] = "0123456789abcdef"; // C++23
  return digits[n];
}
