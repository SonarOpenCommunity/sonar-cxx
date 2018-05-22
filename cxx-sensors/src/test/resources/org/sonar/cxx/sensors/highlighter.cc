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

void test1()
{
   const std::regex RegexEscape(R"([.^$|()\[\]{}*+?\\])"); // raw string literal
}

void test2(const char* sourceFilename)
{
   Warning() << "Failed to open file " << sourceFilename << " for reading";
}

void test3()
{
   const char     *t1 = "...";   // UTF-8 encoded
   const char     *t2 = u8"..."; // UTF-8 encoded   
   const wchar_t  *t3 = L"...";  // Wide string
   const char16_t *t4 = u"...";  // UTF-16 encoded
   const char32_t *t5 = U"...";  //  UTF-32 encoded
   
   const char     *t6 = "hello" " world";
   const wchar_t  *t7 = u"" "hello world";
   const wchar_t  *t8 = /*comment1*/ u"" /*comment2*/ "hello world" /*comment3*/; // issue #996

   const char     *t9 = /*comment4*/ "hello"
                        /*comment5*/ " world" /*comment6*/;

   const char     *t10 = "hello"
                         "Mary"
                         "Lou";
}

/* EOF */
