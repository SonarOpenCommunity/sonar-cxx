/*
 comment
*/

//
// comment
//

//
// PREPROCESS_DIRECTIVE
//
#include "highlighter.h"

#ifdef _TEST
#   define VALUE 1
#else
#   define VALUE 2
#endif

#define FUNC(a) \
   auto value = a; \
   return value ? 0 : 1;

//
// CONSTANT
//
int i1 =  0;
int i2 = -1;
int i3 = +1;

unsigned u1 = 0u;
unsigned long u2 = 1ul;

int h1 = 0x0;
int b1 = 0b0;
int b2 = 0b0100'1100'0110;

float f1 =  0.0;
float f2 = -1.0;
float f3 = +1.0;
float f4 =  3.14E-10;

//
// STRING
//
char c1 = 'x';
char c1 = '\t';

const char* str1 = "hello";
const char* str1 = "hello\tworld\r\n";

//
// KEYWORD
//
void func()
{
   auto str = "test"; // comment
   // comment
   auto iii = 0;

   /* comment */
   for(const auto& c : str)
   {
      if (c == 't') /* comment */
      {
         iii++;
      }
   }
}

/* EOF */
