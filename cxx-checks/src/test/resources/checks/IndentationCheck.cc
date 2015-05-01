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
	printf("\n");          // Compliant with the default tab width (8)
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
// *******************start from column=0
  enum FooX {             // Non-Compliant
    A,                    // Non-Compliant
  B,                      // Compliant
    C                     // Compliant - already reported
};
// *******************start from column=0
int FooY() {
void foo1();               // Non-Compliant
  void foo2();             // Compliant
};
// *******************start from column=0
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
// *******************start from column=0
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
  void foo()         {}    // Compliant
  Foo();                   // Compliant
  enum Code {              // Compliant
    CODE_OK,
    CODE_ERROR,
  }
  bool       variable;     // Compliant
  void       Test1();      // Compliant
  virtual void ReportError(); // Compliant
  virtual void Test()=0;   // Compliant
  virtual ~Foo()      {;}  // Compliant
  enum Code { CODE_1, CODE_2,	// No check
              CODE_3, CODE_4,   // Compliant
	      CODE_4,	CODE_5,     // Compliant with the default tab width (8)
    CODE_6 };              // Not compliant
  void Labels() {
start:
    printf("\n");          // Compliant
end:
printf("\n");              // Non-compliant
  }
  void VariousIfs() {
    if (0)                 // Compliant
      printf("\n");        // Compliant
    if (0)                 // Compliant
    printf("\n");          // Non-Compliant
    if (0) printf("\n");   // Compliant
    if (0) { printf("\n"); }// Compliant
    if (0);                // Compliant
    if (1)
    {
    }
    else if (2)            // Compliant
    {
      printf("\n");        // Compliant
    }
    else if (3)
      printf("\n");        // Compliant
    else
    {                      // Compliant
      printf("\n");        // Compliant
    }
    if (0) {               // Compliant
      printf("\n");        // Compliant
    }
  }
};
// *******************start from column=0
namespace Toto {
  class Foo {
    int a;                 // Compliant
  };
  void foo()
  {
    printf("\n");          // Compliant
  }
class Foo {                // Non-Compliant
  int a;                   // Non-Compliant
};
}
// *******************start from column=0
extern "C" {
void foo() {
  printf("\n");            // Compliant
}
}
// *******************start from column=0
enum Foo {                 // Compliant
  A,
  B
};
// *******************start from column=0
  namespace XXX { }        // Non-Compliant
namespace YYY {            // Compliant
namespace XXX {            // Non-Compliant
}
}
// *******************start from column=0
  int fun() {              // Non-Compliant
  }
// *******************start from column=0
void foo1() {              // Compliant
  printf("\n");            // Compliant
}                          // Compliant
// *******************start from column=0
 void foo2() {             // Non-Compliant
 printf("hehe");           // Non-Compliant
     printf("\n");         // Compliant - already reported
}
// *******************start from column=0
void foo3() {
  switch(x) {
   case 1:                 // Non-Compliant
      break;               // Compliant
    case 2:                // Compliant
     break;                // Non-Compliant
  default:                 // Non-Compliant
      break;
  }
   switch (0)              // Non-Compliant
   {
   case 0:                 // Non-Compliant
      printf("\n");        // Compliant
     break;                // Compliant - already reported
    case 1:                // Compliant
     break;                // Non-Compliant
   }
}
// *******************start from column=0
#define EMPTY()
void Trivia() {
  EMPTY();                 // Compliant
  EMPTY()
  ;                        // Compliant
    EMPTY();               // Non-Compliant
  if (1) {
    EMPTY()
      ;                    // Non-Compliant
  }
  if (1) { EMPTY();        // Compliant
           printf("1");    // Compliant
             printf("0"); }// Non-Compliant
  if (1) {
    /* this is correct */  // Compliant
      /* this is not */    // Non-Compliant
    ;
  }
#if 0                      // Compliant
  #endif                   // Non-Compliant
  printf("0");
}
// *******************start from column=0

namespace piTest {
  using rapidxml::xml_node;
  PngDataSet::PngDataSet(std::string config)
  {
    if (config.compare("") != 0)
      return false;
  }
}

namespace B {
  void f(int);
  void f(double);
}
namespace C {
  void f(int);
  void f(double);
  void f(char);
}
void h() {
  using B::f; // introduces B::f(int), B::f(double)
  using C::f; // introduces C::f(int), C::f(double), and C::f(char)
  f('h');      // calls C::f(char)
  f(1);        // error: B::f(int) or C::f(int)?
  void f(int); // error: f(int) conflicts with C::f(int) and B::f(int)
}

