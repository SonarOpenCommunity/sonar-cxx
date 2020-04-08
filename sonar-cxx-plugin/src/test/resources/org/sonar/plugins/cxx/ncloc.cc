/*
 Header
*/

#include "ncloc.h"

// comment
void func1()
{
   h1 = 0;
}

/*  comment */
void func2()
{
   const char* txt =
      "Hello "
      " World!";
}

void func3(
   int a,
   int b
  )
{
   return a + b;
}

#define NUMBER 10

void func4()
{
   // comment
   for(int iii=0; iii<NUMBER; ++iii) {
      h1 += iii; // comment
   }
}

#define ADD(a, b) \
   ((a) + (b))

void func5()
{
   int a=1, b=2;
   int sum = ADD(a, b);
}

// declaration
void func6();

void func7(bool flag)
{
    int iii = 0;

    if (flag) {
        iii = 1;
    }
    else {
        iii = 0;
    }
}

int func8(int p)
{
    int r;

    switch (p) {
    case 0:
        r = 100;
        break;
    case 1:
        r = 200;
        break;
    default:
        r = 300;
        break;
    }

    return r;
}

void func9()
{
    int *p;

    try {
        p = new int[10];
    }
    catch (...) {
        p = nullptr;
    }

}

void func10()
{
    // comment
    for (int iii = 0;
        iii < NUMBER;
        ++iii)
    {
        h1 += iii; // comment
    }
}

// inline
class MyClass {
public:
   MyClass();
   MyClass(const MyClass&) = delete;
   MyClass& operator=(const MyClass&) = default;

   int method1();
   
   int method2()
   {
      // comment
      for(int iii=0; iii<NUMBER; ++iii) {
         h1 += iii; // comment
      }
   }
};

constexpr int factorial(int n)
{
  return n <= 1 ? 1 : (n * factorial(n - 1));
}

/* EOF */
