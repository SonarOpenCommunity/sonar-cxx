#include <stdio.h>
#include <tchar.h>
class Foo {
  int a;                   // Compliant
   int b;                  // Non-Compliant
  int c;                   // Compliant - already reported
public:
  void foo1() {            // Compliant
    printf("\n");          // Compliant
    }                      // Compliant
 void foo2() {             // Non-Compliant
   printf("hehe");         // Non-Compliant
     printf("\n");         // Compliant - already reported
  }
  void foo3() {            // Compliant
printf("\n");;             // Non-Compliant
printf("\n");              // Compliant - already reported
printf("\n");              // Compliant - already reported
if (0) {                   // Compliant - already reported
  printf("\n");            // Non-Compliant
  if (0) {                 // Compliant - already reported
        printf("\n");      // Compliant
    printf("\n");          // Non-Compliant
  }
      ; printf("\n");      // Compliant
}
}
  class FooXX {
    int a;                 // Compliant
  int b;                   // Non-Compliant
  };
};
  enum FooX {
    A,                    // Compliant
  B,                      // Non-Compliant
    C
};
int FooY() {
void foo1();               // Non-Compliant
  void foo2();             // Compliant
};
class Foo {
  void foo() {
    class MyInterface{
      void foo() {         // Compliant - not checked
      }
      void foo() {         // Compliant - not checked
     }
   };
}
  int foo[]  {
    0,
    1
  };
};

 class Foo {               // Non-Compliant
  public:
  int par;
  void foo() {
    switch (0) 
    {
      case 0: 
        printf("ho"); printf("\n"); // Compliant
        break;
    }
    printf("ha"            // Compliant
          ); printf("\n"); // Compliant
    switch (par) {         // Compliant
    }
    switch (par) {         // Compliant
      case 0: 
      case 1: break;
      case 2: 
      case 3: 
        ;
        break;
      default: ;
    }
  };
};
