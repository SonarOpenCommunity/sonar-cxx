void declarations(const char *name, int size) {
  Hasher a(name);
  Hasher b(name, size);
  Hasher c(Config);
  Hasher d(const char *n);
  Hasher e(name, int n);
  Hasher f(T n);
  Hasher g();
  Hasher h("literal");
}

int definition(a, b) int a; int b; { return a; }
