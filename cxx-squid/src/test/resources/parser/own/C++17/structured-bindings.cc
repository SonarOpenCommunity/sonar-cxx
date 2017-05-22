//todo int[2] f();
auto[x, y] = f();

tuple<T1, T2, T3> g();
auto[a, b, c] = g();

struct MyStruct {
    int x;
    double y;
};

MyStruct h();
auto[u, v] = h();

void inForRange()
{
    std::map<string, double> mymap;
    for (const auto&[key, val] : mymap) {
        std::cout << key << ": " << val << std::endl;
    }
}

