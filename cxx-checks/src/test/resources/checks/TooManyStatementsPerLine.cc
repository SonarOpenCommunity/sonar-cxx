int doSomething();
int doSomethingElse();
bool condition = true;
#define TEST(a,b) if (a) {    \
                  } if (b) {  \
                  }
class TooManyStatementsPerLine {
    int a; int b; // OK - not a statement
    void myfunc() {
        doSomething(); doSomethingElse(); // NOK
        ; // OK
        if (a) {  } // OK
        if (a) {  } if (b) {  } // NOK
        while (condition); // OK
        TEST(a,b) // OK
        if (a) {  } TEST(a,b) // NOK
    label: while (condition) { // OK
            break; // OK
        }
        int a = 0; a++; // NOK
        switch(x) {
        case 0: x++; break; //(N)OK, depending on parameters
        case 1:
            x++; break;     //NOK
        }
    }
};
