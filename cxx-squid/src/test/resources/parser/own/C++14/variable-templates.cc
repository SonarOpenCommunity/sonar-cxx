template<typename T>
constexpr T pi = T(3.1415926535897932385);

// Usual specialization rules apply:
template<>
constexpr const char* pi<const char*> = "pi";
