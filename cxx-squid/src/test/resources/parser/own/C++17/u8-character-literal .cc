char* txt1 = u8"I'm a UTF-8 string.";
char* txt1 = u8"This is a Unicode Character: \u2018.";

auto S1 = u8"hello"s; // Combining string literals with standard s-suffix
auto S6 = u8R"("Hello \ world")"s; // raw const char*, encoded as UTF-8  
