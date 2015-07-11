char* txt1 = u8R"XXX(I'm a "raw UTF-8" string.)XXX";
char16_t* txt2 = uR"*(This is a "raw UTF-16" string.)*";
char32_t* txt3 = UR"(This is a "raw UTF-32" string.)";
string s = R"X*X(A C++11 raw string literal can be specified like this: R"(This is my raw string)" )X*X";
