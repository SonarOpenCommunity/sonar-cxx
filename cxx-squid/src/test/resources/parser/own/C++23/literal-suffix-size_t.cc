auto a = 0z;
auto b = 0zu;
auto c = 0uz;

auto d = 0Z;
auto e = 0ZU;
auto f = 0UZ;

void foo() {
   for (auto i = 0zu; i < v.size(); ++i) {
      std::cout << i << ": " << v[i] << '\n';
   }
}
