int doSomething();
int doSomethingElse();
bool condition = true;
class TooManyStatementsPerLine {
    int a; int b; // OK - not a statement
    void myfunc() {
        doSomething(); doSomethingElse(); // NOK
        ; // OK
        if (a) {  } // OK
        if (a) {  } if (b) {  } // NOK
        while (condition); // OK
    label: while (condition) { // OK
        break; // OK
    }
           int a = 0; a++; // NOK
    }
};
