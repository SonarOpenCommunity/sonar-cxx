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

/* EOF */
