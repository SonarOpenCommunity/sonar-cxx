template<typename T, std::size_t X, std::size_t Y>
struct Matrix {

    static inline std::array<T, X * Y> mat{};               // (2)
    
    static T& operator[](std::size_t x, std::size_t y) {    // (1)
        return mat[y * X + x];
    }
};

int main() {

    std::cout << '\n';

    Matrix<int, 3, 3> mat;
    for (auto i : {0, 1, 2}) {
        for (auto j : {0, 1, 2}) mat[i, j] = (i * 3) + j;
    }
    for (auto i : {0, 1, 2}) {
        for (auto j : {0, 1, 2}) std::cout << mat[i, j] << " ";
    }

    std::cout << '\n';

}
