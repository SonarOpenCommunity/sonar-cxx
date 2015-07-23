long long i = 0;
long long int j = 0;

void func1(long long p) {}
void func2(long long int p) {}

long long func3() { return 0; }
long long int func4() { return 0; }

auto func5(long long p) -> long long { return 0; }
auto func6(long long int p) -> long long int { return 0; }
