struct S {
    //todo explicit (S)(const S&);    // error in C++20, OK in C++17
    //todo explicit (operator int)(); // error in C++20, OK in C++17
};
