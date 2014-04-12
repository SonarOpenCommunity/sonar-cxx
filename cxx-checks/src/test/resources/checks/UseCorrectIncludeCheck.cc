#include <stdio.h> // Compliant
#include <../string> // Non-Compliant
#include "/apath/string" // Non-Compliant
#include "../apath/string" // Non-Compliant
#include "./apath/string" // Non-Compliant
#include "\apath/string" // Non-Compliant
#include "..\apath/string" // Non-Compliant
#include "apath/string" // Compliant
#include "string"// Compliant


class A {
    long * someNumber = nullptr;  // Compliant
public:
    void f( ) {
        if (nullptr != someNumber) { // Compliant
            printf( "a number %d", *someNumber );
        }

    }
};