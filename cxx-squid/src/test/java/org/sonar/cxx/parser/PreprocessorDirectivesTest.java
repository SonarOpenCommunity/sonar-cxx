/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx.parser;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PreprocessorDirectivesTest extends ParserBaseTestHelper {

  @Test
  void preprocessorDirectives() {
    assertThat(parse("""
                     #define IDX 10
                     array[IDX];
                     """))
      .isEqualTo("array [ 10 ] ; EOF");
  }

  @Test
  void hashhashRelatedParsingProblem1() {
    assertThat(parse("""
                     #define CASES CASE(00)
                     #define CASE(n) case 0x##n:
                     void foo()  {
                       switch (1) {
                       CASES
                         break;
                       }
                     }
                     """))
      .isEqualTo("void foo ( ) { switch ( 1 ) { case 0x00 : break ; } } EOF");
  }

  @Test
  void hashhashRelatedParsingProblem2() {
    assertThat(parse("""
                     #define paster( n ) printf_s( "token" #n " = %d", token##n )
                     int token9 = 9;int main() {
                       paster( 9 );
                     }
                     """))
      .isEqualTo("int token9 = 9 ; int main ( ) { printf_s ( \"token9 = %d\" , token9 ) ; } EOF");
  }

  @Test
  void objectLikeMacros() {
    assertThat(parse("""
                                      #define BUFFER_SIZE 1024
                                      foo = (char *) malloc (BUFFER_SIZE);
                                      """))
      .isEqualTo("foo = ( char * ) malloc ( 1024 ) ; EOF");

    assertThat(parse("""
                     #define NUMBERS 1, \\
                     2, \\
                     3
                     int x[] = { NUMBERS };
                     """))
      .isEqualTo("int x [ ] = { 1 , 2 , 3 } ; EOF");

    assertThat(parse("""
                     #define TABLESIZE BUFSIZE
                     #define BUFSIZE 1024
                     int i = TABLESIZE;
                     """))
      .isEqualTo("int i = 1024 ; EOF");

    assertThat(parse("""
                     #define A 1 + 1
                     int i = A;
                     """))
      .isEqualTo("int i = 1 + 1 ; EOF");

    assertThat(parse("""
                     #define A a // Comment
                     A;
                     """))
      .isEqualTo("a ; EOF");

    // TODO: wrong assert
    assertThat(parse("""
                     #define A_B A/*Comment*/B
                      A_B;
                     """)
      .equals("A B ; EOF"));

  }

  @Test
  void functionLikeMacros() {
    assertThat(parse("""
                     #define lang_init() c_init()
                     lang_init();
                     """))
      .isEqualTo("c_init ( ) ; EOF");

    // without whitespace after parameter list
    assertThat(parse("""
                     #define foo(a)x
                     foo(b)=1;
                     """))
      .isEqualTo("x = 1 ; EOF");

    // with parantheses
    assertThat(parse("""
                     #define isequal(a, b)(a == b)
                     b = isequal(1,2);
                     """))
      .isEqualTo("b = ( 1 == 2 ) ; EOF");
  }

  @Test
  void complexMacroRescanning() {
    assertThat(parse("""
                     #define lang_init std_init
                     #define std_init() c_init()
                     lang_init();
                     """))
      .isEqualTo("c_init ( ) ; EOF");

    assertThat(parse("""
                     #define lang_init(x) x = std_init
                     #define std_init() c_init()
                     lang_init(c)();
                     """))
      .isEqualTo("c = c_init ( ) ; EOF");

    assertThat(parse("""
                     #define _MSC_VER_WORKAROUND_GUARD 1
                     #define BOOST_MSVC_WORKAROUND_GUARD 0
                     #define _MSC_VER 1600
                     #define BOOST_MSVC _MSC_VER
                     #define TEST(symbol) symbol ## _WORKAROUND_GUARD
                     int i=TEST(BOOST_MSVC);
                     """))
      .isEqualTo("int i = 0 ; EOF");

    assertThat(parse("""
                     #define _MSC_VER_WORKAROUND_GUARD 1
                     #define BOOST_MSVC_WORKAROUND_GUARD 0
                     #define _MSC_VER 1600
                     #define BOOST_MSVC _MSC_VER
                     #define _WORKAROUND_GUARD _XXX
                     #define TEST(symbol1, symbol2) symbol1 ## symbol2
                     int i=TEST(BOOST_MSVC, _WORKAROUND_GUARD);
                     """))
      .isEqualTo("int i = 0 ; EOF");
  }

  @Test
  void macroArguments() {
    assertThat(parse("""
                     #define min(X, Y)  ((X) < (Y) ? (X) : (Y))
                     int i = min(a + 28, *p);
                     """))
      .isEqualTo("int i = ( ( a + 28 ) < ( * p ) ? ( a + 28 ) : ( * p ) ) ; EOF");

    assertThat(parse("""
                     #define foo(x) x + "x"
                     string s = foo(bar);
                     """))
      .isEqualTo("string s = bar + \"x\" ; EOF");
  }

  @Test
  void variadicMacros() {
    assertThat(parse("""
                     #define eprintf(...) fprintf (stderr, __VA_ARGS__)
                     eprintf("%s:%d: ", input_file, lineno);
                     """))
      .isEqualTo("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF");

    assertThat(parse("""
                     #define eprintf(args...) fprintf (stderr, args)
                     eprintf("%s:%d: ", input_file, lineno);
                     """))
      .isEqualTo("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF");

    assertThat(parse("""
                     #define eprintf(format, ...) fprintf (stderr, format, __VA_ARGS__)
                     eprintf("%s:%d: ", input_file, lineno);
                     """))
      .isEqualTo("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF");

    assertThat(parse("""
                     #define eprintf(format, args...) fprintf (stderr, format, args)
                     eprintf("%s:%d: ", input_file, lineno);
                     """))
      .isEqualTo("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF");

    // the Visual C++ implementation will suppress a trailing comma
    // if no arguments are passed to the ellipsis
    assertThat(parse("""
                     #define EMPTY
                     #define MACRO(s, ...) printf(s, __VA_ARGS__)
                     MACRO("error", EMPTY);
                     """))
      .isEqualTo("printf ( \"error\" ) ; EOF");

    assertThat(parse("""
                     #define MACRO(s, ...) printf(s, __VA_ARGS__)
                     MACRO("error");
                     """))
      .isEqualTo("printf ( \"error\" ) ; EOF");

    assertThat(parse("""
                     #define MACRO(s, ...) do { printf(s, __VA_ARGS__); } while (false)
                     int main() { MACRO("error"); }
                     """))
      .isEqualTo("int main ( ) { do { printf ( \"error\" ) ; } while ( false ) ; } EOF");

    // without whitespace after the parameter list
    assertThat(parse("""
                     #define foo(a...);
                     foo(a, b)
                     """))
      .isEqualTo("; EOF");

    // with more parameters and without whitespace after the parameter list
    assertThat(parse("""
                     #define foo(a, b...);
                     foo(a, b, c)
                     """))
      .isEqualTo("; EOF");

    // GNU CPP: You are allowed to leave the variable argument out entirely
    assertThat(parse("""
                     #define eprintf(format, ...) fprintf (stderr, format, __VA_ARGS__)
                     eprintf("success!");
                     """))
      .isEqualTo("fprintf ( stderr , \"success!\" ) ; EOF");
    // GNU CPP: special meaning of token paste operator - if variable argument is left out then the comma before the ‘##’ will be deleted.
    assertThat(parse("""
                     #define eprintf(format, ...) fprintf (stderr, format, ##__VA_ARGS__)
                     eprintf("success!");
                     """))
      .isEqualTo("fprintf ( stderr , \"success!\" ) ; EOF");

    // C++11: see  syntax 4 at http://en.cppreference.com/w/cpp/preprocessor/replace
    assertThat(parse("""
                     #define showlist(...) puts(#__VA_ARGS__)
                     showlist();
                     """))
      .isEqualTo("puts ( \"\" ) ; EOF");

    // C++11: see  syntax 3 at http://en.cppreference.com/w/cpp/preprocessor/replace
    assertThat(parse("""
                     #define showlist(...) puts(#__VA_ARGS__)
                     showlist(1, "x", int);
                     """))
      .isEqualTo("puts ( \"1,\\\"x\\\",int\" ) ; EOF");
  }

  @Test
  void vaOptMacros() {
    // C++20 : Replacement-list may contain the token sequence __VA_OPT__ ( content ),
    // which is replaced by content if __VA_ARGS__ is non-empty, and expands to nothing otherwise.

    assertThat(parse("""
                     #define LOG(msg, ...) printf(msg __VA_OPT__(,) __VA_ARGS__)
                     LOG("hello world");
                     """))
      .isEqualTo("printf ( \"hello world\" ) ; EOF");

    assertThat(parse("""
                     #define LOG(msg, ...) printf(msg __VA_OPT__(,) __VA_ARGS__)
                     LOG("hello world", );
                     """))
      .isEqualTo("printf ( \"hello world\" ) ; EOF");

    assertThat(parse("""
                     #define LOG(msg, ...) printf(msg __VA_OPT__(,) __VA_ARGS__)
                     LOG("hello %d", n);
                     """))
      .isEqualTo("printf ( \"hello %d\" , n ) ; EOF");
    assertThat(parse("""
                     #define SDEF(sname, ...) S sname __VA_OPT__(= { __VA_ARGS__ })
                     SDEF(foo);
                     """))
      .isEqualTo("S foo ; EOF");

    assertThat(parse("""
                     #define SDEF(sname, ...) S sname __VA_OPT__(= { __VA_ARGS__ })
                     SDEF(bar, 1, 2);
                     """))
      .isEqualTo("S bar = { 1 , 2 } ; EOF");

    // https://marc.info/?l=gcc-patches&m=151047200615291&w=2
    assertThat(parse("""
                     #define CALL(F, ...) F(7 __VA_OPT__(,) __VA_ARGS__)
                     CALL(f1);
                     """))
      .isEqualTo("f1 ( 7 ) ; EOF");

    assertThat(parse("""
                     #define CALL(F, ...) F(7 __VA_OPT__(,) __VA_ARGS__)
                     CALL(f1, );
                     """))
      .isEqualTo("f1 ( 7 ) ; EOF");

    assertThat(parse("""
                     #define CALL(F, ...) F(7 __VA_OPT__(,) __VA_ARGS__)
                     CALL(f1, 1);
                     """))
      .isEqualTo("f1 ( 7 , 1 ) ; EOF");

    assertThat(parse("""
                     #define CP(F, X, Y, ...) F(__VA_OPT__(X ## Y,) __VA_ARGS__)
                     CP(f0, one, two);
                     """))
      .isEqualTo("f0 ( ) ; EOF");

    assertThat(parse("""
                     #define CP(F, X, Y, ...) F(__VA_OPT__(X ## Y,) __VA_ARGS__)
                     CP(f0, one, two, );
                     """))
      .isEqualTo("f0 ( ) ; EOF");

    assertThat(parse("""
                     #define CP(F, X, Y, ...) F(__VA_OPT__(X ## Y,) __VA_ARGS__)
                     CP(f0, one, two, 3);
                     """))
      .isEqualTo("f0 ( onetwo , 3 ) ; EOF");

    assertThat(parse("""
                     #define CS(F, ...) F(__VA_OPT__(s(# __VA_ARGS__)))
                     CS(f0);
                     """))
      .isEqualTo("f0 ( ) ; EOF");

    assertThat(parse("""
                     #define CS(F, ...) F(__VA_OPT__(s(# __VA_ARGS__)))
                     CS(f1, 1, 2, 3, 4);
                     """))
      .isEqualTo("f1 ( s ( \"1,2,3,4\" ) ) ; EOF");

    assertThat(parse("""
                     #define D(F, ...) F(__VA_OPT__(__VA_ARGS__) __VA_OPT__(,) __VA_ARGS__)
                     D(f0);
                     """))
      .isEqualTo("f0 ( ) ; EOF");

    assertThat(parse("""
                     #define D(F, ...) F(__VA_OPT__(__VA_ARGS__) __VA_OPT__(,) __VA_ARGS__)
                     D(f2, 1);
                     """))
      .isEqualTo("f2 ( 1 , 1 ) ; EOF");

    assertThat(parse("""
                     #define D(F, ...) F(__VA_OPT__(__VA_ARGS__) __VA_OPT__(,) __VA_ARGS__)
                     D(f2, 1, 2);
                     """))
      .isEqualTo("f2 ( 1 , 2 , 1 , 2 ) ; EOF");

    assertThat(parse("""
                     #define CALL0(...) __VA_OPT__(f2)(0 __VA_OPT__(,)__VA_ARGS__)
                     int* ptr = CALL0();
                     """))
      .isEqualTo("int * ptr = ( 0 ) ; EOF");

    assertThat(parse("""
                     #define CALL0(...) __VA_OPT__(f2)(0 __VA_OPT__(,)__VA_ARGS__)
                     CALL0(23);
                     """))
      .isEqualTo("f2 ( 0 , 23 ) ; EOF");
  }

  @Test
  void stringification() {
    // default use case
    assertThat(parse("""
                     #define make_string(x) #x
                     string s = make_string(a test);
                     """))
      .isEqualTo("string s = \"a test\" ; EOF");

    // leading and trailing spaces were trimmed,
    // space between words was compressed to a single space character
    assertThat(parse("""
                     #define make_string(x) #x
                     string s = make_string(   a    test   );
                     """))
      .isEqualTo("string s = \"a test\" ; EOF");

    // the quotes were automatically converted
    assertThat(parse("""
                     #define make_string(x) #x
                     string s = make_string("a" "test");
                     """))
      .isEqualTo("string s = \"\\\"a\\\" \\\"test\\\"\" ; EOF");

    // the slash were automatically converted
    assertThat(parse("""
                     #define make_string(x) #x
                     string s = make_string(a\\test);
                     """))
      .isEqualTo("string s = \"a\\\\test\" ; EOF");

    // If the token is a macro, the macro is not expanded
    // - the macro name is converted into a string.
    assertThat(parse("""
                     #define make_string(x) #x
                     #define COMMA ,
                     string s = make_string(a COMMA test);
                     """))
      .isEqualTo("string s = \"a COMMA test\" ; EOF");

    assertThat(parse("""
                     #define F abc
                     #define B def
                     #define FB(arg) #arg
                     string s = FB(F B);
                     """))
      .isEqualTo("string s = \"F B\" ; EOF");

    assertThat(parse("""
                     #define F abc
                     #define B def
                     #define FB(arg) #arg
                     #define FB1(arg) FB(arg)
                     string s = FB1(F B);
                     """))
      .isEqualTo("string s = \"abc def\" ; EOF");

    assertThat(parse("""
                     #define F abc
                     #define B def
                     #define FB(arg) #arg
                     #define FB1(arg) FB(arg)
                     string s = FB1(F\\B);
                     """))
      .isEqualTo("string s = \"abc\\\\def\" ; EOF");

    assertThat(parse("""
                     #define F abc
                     #define B def
                     #define FB(arg) #arg
                     #define FB1(arg) FB(arg)
                     string s = FB1(F/B);
                     """))
      .isEqualTo("string s = \"abc/def\" ; EOF");

    assertThat(parse("""
                     #define SC_METHOD(func) declare_method_process( func ## _handle, #func, func )
                     SC_METHOD(test);
                     """))
      .isEqualTo("declare_method_process ( test_handle , \"test\" , test ) ; EOF");
  }

  @Test
  void concatenation() {
    assertThat(parse("""
                     #define A t ## 1
                     int i = A;
                     """))
      .isEqualTo("int i = t1 ; EOF");

    assertThat(parse("""
                     #define A(p) p ## 1
                     t = A(t);
                     """))
      .isEqualTo("t = t1 ; EOF");

    assertThat(parse("""
                     #define A(p1,p2) p1 ## p2
                     t = A(a,b);
                     """))
      .isEqualTo("t = ab ; EOF");

    assertThat(parse("""
                     #define A(p1,p2) p1 ## ## p2
                     t = A(a,b);
                     """))
      .isEqualTo("t = ab ; EOF");

    assertThat(parse("""
                     #define ab 1
                     #define A(p1,p2) p1 ## p2
                     t = A(a,b);
                     """))
      .isEqualTo("t = 1 ; EOF");

    assertThat(parse("""
                     #define macro_start i ## n ##t m         ##ain(void);
                     macro_start
                     """))
      .isEqualTo("int main ( void ) ; EOF");

    assertThat(parse("""
                     #define A B(cf)
                     #define B(n) 0x##n
                     i = A;
                     """))
      .isEqualTo("i = 0xcf ; EOF");
  }

  @Test
  void undef() {
    assertThat(parse("""
                     #define FOO 4
                     #undef FOO
                     x = FOO;
                     """))
      .isEqualTo("x = FOO ; EOF");

    assertThat(parse("""
                     #define BUFSIZE 1020
                     #define TABLESIZE BUFSIZE
                     #undef BUFSIZE
                     #define BUFSIZE 37
                     int i = TABLESIZE;
                     """))
      .isEqualTo("int i = 37 ; EOF");
  }

  @Test
  void redefiningMacros() {
    assertThat(parse("""
                     #define FOO 1
                     #define FOO 2
                     int i = FOO;
                     """))
      .isEqualTo("int i = 2 ; EOF");
  }

  @Test
  void prescan() {
    assertThat(parse("""
                     #define AFTERX(x) X_ ## x
                     #define XAFTERX(x) AFTERX(x)
                     #define TABLESIZE 1024
                     #define BUFSIZE TABLESIZE
                     int i = XAFTERX(BUFSIZE);
                     """))
      .isEqualTo("int i = X_1024 ; EOF");

    assertThat(parse("""
                     #define FOOBAR 1
                     #define CHECK(a, b) (( a ## b + 1 == 2))
                     #if CHECK(FOO , BAR)
                     i = 1;
                     #else
                     0
                     #endif
                     """))
      .isEqualTo("i = 1 ; EOF");
  }

  @Test
  void selfReferentialMacros() {
    assertThat(parse("""
                     #define EPERM EPERM
                     a = EPERM;
                     """))
      .isEqualTo("a = EPERM ; EOF");

    assertThat(parse("""
                     #define foo (4 + foo)
                     i = foo;
                     """))
      .isEqualTo("i = ( 4 + foo ) ; EOF");

    assertThat(parse("""
                     #define x (4 + y)
                     #define y (2 * x)
                     i = x;
                     """))
      .isEqualTo("i = ( 4 + ( 2 * x ) ) ; EOF");
  }

  @Test
  void hasInclude() {
    assertThat(parse("""
                     #if __has_include
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #if defined(__has_include)
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #if __has_include(<optional>)
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 0 ; EOF");

    assertThat(parse("""
                     #if __has_include("optional")
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 0 ; EOF");

    assertThat(parse("""
                     #define EXISTS __has_include("optional")
                     #if EXISTS
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 0 ; EOF");
  }

  @Test
  void featureCheckingMacros() {
    //
    // source: https://clang.llvm.org/docs/LanguageExtensions.html
    //
    assertThat(parse("""
                     #ifdef __has_builtin
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_feature
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_extension
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_cpp_attribute
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_c_attribute
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_attribute
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_attribute
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_declspec_attribute
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __is_identifier
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_attribute
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_include
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_include_next
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse("""
                     #ifdef __has_warning
                     #   define OK 1
                     #else
                     #   define OK 0
                     #endif
                     r = OK;
                     """))
      .isEqualTo("r = 1 ; EOF");
  }

}
