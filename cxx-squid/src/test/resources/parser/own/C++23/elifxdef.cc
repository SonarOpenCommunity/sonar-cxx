void sample() {
    
// Note that if a compiler does not support C++23's #elifdef/#elifndef
// directives then the "unexpected" block (see below) will be selected.
#ifdef CPU
    std::cout << "4: no1\n";
#elifdef GPU
    std::cout << "4: no2\n";
#elifndef RAM
    std::cout << "4: yes\n"; // expected block
#else
    std::cout << "4: no!\n"; // unexpectedly selects this block by skipping
                             // unknown directives and "jumping" directly
                             // from "#ifdef CPU" to this "#else" block
#endif
 
// To fix the problem above we may conditionally define the
// macro ELIFDEF_SUPPORTED only if the C++23 directives
// #elifdef/#elifndef are supported.
#if 0
#elifndef UNDEFINED_MACRO
#define ELIFDEF_SUPPORTED
#else
#endif
 
#ifdef ELIFDEF_SUPPORTED
    #ifdef CPU
        std::cout << "4: no1\n";
    #elifdef GPU
        std::cout << "4: no2\n";
    #elifndef RAM
        std::cout << "4: yes\n"; // expected block
    #else
        std::cout << "4: no3\n";
    #endif
#else // when #elifdef unsupported use old verbose `#elif defined`
    #ifdef CPU
        std::cout << "4: no1\n";
    #elif defined GPU
        std::cout << "4: no2\n";
    #elif !defined RAM
        std::cout << "4: yes\n"; // expected block
    #else
        std::cout << "4: no3\n";
    #endif
#endif

}
