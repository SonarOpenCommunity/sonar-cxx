[[gnu::sample]] [[gnu::sample]] [[gnu::hot]] [[nodiscard]]
inline int f(); // declare f with four attributes
 
[[gnu::sample, gnu::sample, gnu::hot, nodiscard]]
int f(); // same as above, but uses a single attr specifier that contains four attributes
 
[[using gnu : sample, sample, hot]] [[nodiscard]] [[gnu::sample]]
int f(); // same as above
