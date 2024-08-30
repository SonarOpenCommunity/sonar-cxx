std::string s1 = "abc";
auto withParen = [s1 = std::move(s1)] () {
  std::cout << s1 << '\n'; 
};

std::string s2 = "abc";
auto noSean = [s2 = std::move(s2)] { // Note no syntax error in C++ < 23
  std::cout << s2 << '\n'; 
};

std::string s3 = "abc";
auto withParenCpp23 = [s3 = std::move(s3)] () mutable {
  s3 += "d";
  std::cout << s3 << '\n'; 
};

std::string s4 = "abc";
auto noSeanCpp23 = [s4 = std::move(s4)] mutable { // C++23, syntax error before without ()
  s4 += "d";
  std::cout << s4 << '\n'; 
};
