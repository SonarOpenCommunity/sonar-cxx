#include <stdio.h>
#include <string>
class A {
    long * someNumber = NULL;  // Compliant
public:
    void f( ) {
        if (NULL != someNumber) { // Non-Compliant
            printf( "a number %d", *someNumber );
        }
        WORD aVar1 = 0; // Non-Compliant
        DWORD aVar1 = 0; // Non-Compliant
        BOOL myFlag1 = true; // Non-Compliant
        myFlag1 = FALSE; // Non-Compliant
        bool myFlag2 = false; // Compliant
        myFlag2 = TRUE; // Non-Compliant
        BYTE aChar = 0; // Non-Compliant
        FLOAT fNumber1 = 0.0; // Non-Compliant
    }
};