/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
    assertThat(parse(
      "#define IDX 10\n"
        + "array[IDX];"))
      .isEqualTo("array [ 10 ] ; EOF");
  }

  @Test
  void hashhash_related_parsing_problem1() {
    assertThat(parse(
      "#define CASES CASE(00)\n"
        + "#define CASE(n) case 0x##n:\n"
        + "void foo()  {\n"
        + "switch (1) {\n"
        + "CASES\n"
        + "break;\n"
        + "}\n"
        + "}\n"))
      .isEqualTo("void foo ( ) { switch ( 1 ) { case 0x00 : break ; } } EOF");
  }

  @Test
  void hashhash_related_parsing_problem2() {
    assertThat(parse(
      "#define paster( n ) printf_s( \"token\" #n \" = %d\", token##n )\n"
        + "int token9 = 9;"
        + "int main() { \n"
        + "paster( 9 );\n"
        + "}\n"))
      .isEqualTo("int token9 = 9 ; int main ( ) { printf_s ( \"token9 = %d\" , token9 ) ; } EOF");
  }

  @Test
  void object_like_macros() {
    assertThat(parse(
      "#define BUFFER_SIZE 1024\n"
        + "foo = (char *) malloc (BUFFER_SIZE);"))
      .isEqualTo("foo = ( char * ) malloc ( 1024 ) ; EOF");

    assertThat(parse(
      "#define NUMBERS 1, \\\n"
        + "2, \\\n"
        + "3\n"
        + "int x[] = { NUMBERS };"))
      .isEqualTo("int x [ ] = { 1 , 2 , 3 } ; EOF");

    assertThat(parse(
      "#define TABLESIZE BUFSIZE\n"
        + "#define BUFSIZE 1024\n"
        + "int i = TABLESIZE;"))
      .isEqualTo("int i = 1024 ; EOF");

    assertThat(parse(
      "#define A 1 + 1\n"
        + "int i = A;"))
      .isEqualTo("int i = 1 + 1 ; EOF");

    assertThat(parse(
      "#define A a // Comment\n"
        + "A;"))
      .isEqualTo("a ; EOF");

    assertThat(parse(
      "#define A_B A/*Comment*/B\n"
        + " A_B;")
      .equals("A B ; EOF"));

  }

  @Test
  void function_like_macros() {
    assertThat(parse(
      "#define lang_init() c_init()\n"
        + "lang_init();"))
      .isEqualTo("c_init ( ) ; EOF");

    // without whitespace after parameter list
    assertThat(parse(
      "#define foo(a)x\n"
        + "foo(b)=1;"))
      .isEqualTo("x = 1 ; EOF");

    // with parantheses
    assertThat(parse(
      "#define isequal(a, b)(a == b)\n"
        + "b = isequal(1,2);"))
      .isEqualTo("b = ( 1 == 2 ) ; EOF");
  }

  @Test
  void complex_macro_rescanning() {
    assertThat(parse(
      "#define lang_init std_init\n"
        + "#define std_init() c_init()\n"
        + "lang_init();"))
      .isEqualTo("c_init ( ) ; EOF");

    assertThat(parse(
      "#define lang_init(x) x = std_init\n"
        + "#define std_init() c_init()\n"
        + "lang_init(c)();"))
      .isEqualTo("c = c_init ( ) ; EOF");

    assertThat(parse(
      "#define _MSC_VER_WORKAROUND_GUARD 1\n"
        + "#define BOOST_MSVC_WORKAROUND_GUARD 0\n"
        + "#define _MSC_VER 1600\n"
        + "#define BOOST_MSVC _MSC_VER\n"
        + "#define TEST(symbol) symbol ## _WORKAROUND_GUARD\n"
        + "int i=TEST(BOOST_MSVC);"))
      .isEqualTo("int i = 0 ; EOF");

    assertThat(parse(
      "#define _MSC_VER_WORKAROUND_GUARD 1\n"
        + "#define BOOST_MSVC_WORKAROUND_GUARD 0\n"
        + "#define _MSC_VER 1600\n"
        + "#define BOOST_MSVC _MSC_VER\n"
        + "#define _WORKAROUND_GUARD _XXX\n"
        + "#define TEST(symbol1, symbol2) symbol1 ## symbol2\n"
        + "int i=TEST(BOOST_MSVC, _WORKAROUND_GUARD);"))
      .isEqualTo("int i = 0 ; EOF");
  }

  @Test
  void macro_arguments() {
    assertThat(parse(
      "#define min(X, Y)  ((X) < (Y) ? (X) : (Y))\n"
        + "int i = min(a + 28, *p);"))
      .isEqualTo("int i = ( ( a + 28 ) < ( * p ) ? ( a + 28 ) : ( * p ) ) ; EOF");

    assertThat(parse(
      "#define foo(x) x + \"x\"\n"
        + "string s = foo(bar);"))
      .isEqualTo("string s = bar + \"x\" ; EOF");
  }

  @Test
  void variadic_macros() {
    assertThat(parse(
      "#define eprintf(...) fprintf (stderr, __VA_ARGS__)\n"
        + "eprintf(\"%s:%d: \", input_file, lineno);"))
      .isEqualTo("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF");

    assertThat(parse(
      "#define eprintf(args...) fprintf (stderr, args)\n"
        + "eprintf(\"%s:%d: \", input_file, lineno);"))
      .isEqualTo("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF");

    assertThat(parse(
      "#define eprintf(format, ...) fprintf (stderr, format, __VA_ARGS__)\n"
        + "eprintf(\"%s:%d: \", input_file, lineno);"))
      .isEqualTo("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF");

    assertThat(parse(
      "#define eprintf(format, args...) fprintf (stderr, format, args)\n"
        + "eprintf(\"%s:%d: \", input_file, lineno);"))
      .isEqualTo("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF");

    // the Visual C++ implementation will suppress a trailing comma
    // if no arguments are passed to the ellipsis
    assertThat(parse(
      "#define EMPTY\n"
        + "#define MACRO(s, ...) printf(s, __VA_ARGS__)\n"
        + "MACRO(\"error\", EMPTY);"))
      .isEqualTo("printf ( \"error\" ) ; EOF");

    assertThat(parse(
      "#define MACRO(s, ...) printf(s, __VA_ARGS__)\n"
        + "MACRO(\"error\");"))
      .isEqualTo("printf ( \"error\" ) ; EOF");

    assertThat(parse(
      "#define MACRO(s, ...) do { printf(s, __VA_ARGS__); } while (false)\n"
        + "int main() { MACRO(\"error\"); }"))
      .isEqualTo("int main ( ) { do { printf ( \"error\" ) ; } while ( false ) ; } EOF");

    // without whitespace after the parameter list
    assertThat(parse(
      "#define foo(a...);\n"
        + "foo(a, b)"))
      .isEqualTo("; EOF");

    // with more parameters and without whitespace after the parameter list
    assertThat(parse(
      "#define foo(a, b...);\n"
        + "foo(a, b, c)"))
      .isEqualTo("; EOF");

    // GNU CPP: You are allowed to leave the variable argument out entirely
    assertThat(parse(
      "#define eprintf(format, ...) fprintf (stderr, format, __VA_ARGS__)\n"
        + "eprintf(\"success!\");"))
      .isEqualTo("fprintf ( stderr , \"success!\" ) ; EOF");
    // GNU CPP: special meaning of token paste operator - if variable argument is left out then the comma before the ‘##’ will be deleted.
    assertThat(parse(
      "#define eprintf(format, ...) fprintf (stderr, format, ##__VA_ARGS__)\n"
        + "eprintf(\"success!\");"))
      .isEqualTo("fprintf ( stderr , \"success!\" ) ; EOF");

    // C++11: see  syntax 4 at http://en.cppreference.com/w/cpp/preprocessor/replace
    assertThat(parse(
      "#define showlist(...) puts(#__VA_ARGS__)\n"
        + "showlist();"))
      .isEqualTo("puts ( \"\" ) ; EOF");

    // C++11: see  syntax 3 at http://en.cppreference.com/w/cpp/preprocessor/replace
    assertThat(parse(
      "#define showlist(...) puts(#__VA_ARGS__)\n"
        + "showlist(1, \"x\", int);"))
      .isEqualTo("puts ( \"1,\\\"x\\\",int\" ) ; EOF");
  }

  @Test
  void va_opt_macros() {
    // C++20 : Replacement-list may contain the token sequence __VA_OPT__ ( content ),
    // which is replaced by content if __VA_ARGS__ is non-empty, and expands to nothing otherwise.

    assertThat(parse(
      "#define LOG(msg, ...) printf(msg __VA_OPT__(,) __VA_ARGS__)\n"
        + "LOG(\"hello world\");"))
      .isEqualTo("printf ( \"hello world\" ) ; EOF");

    assertThat(parse(
      "#define LOG(msg, ...) printf(msg __VA_OPT__(,) __VA_ARGS__)\n"
        + "LOG(\"hello world\", );"))
      .isEqualTo("printf ( \"hello world\" ) ; EOF");

    assertThat(parse(
      "#define LOG(msg, ...) printf(msg __VA_OPT__(,) __VA_ARGS__)\n"
        + "LOG(\"hello %d\", n);"))
      .isEqualTo("printf ( \"hello %d\" , n ) ; EOF");
    assertThat(parse(
      "#define SDEF(sname, ...) S sname __VA_OPT__(= { __VA_ARGS__ })\n"
        + "SDEF(foo);"))
      .isEqualTo("S foo ; EOF");

    assertThat(parse(
      "#define SDEF(sname, ...) S sname __VA_OPT__(= { __VA_ARGS__ })\n"
        + "SDEF(bar, 1, 2);"))
      .isEqualTo("S bar = { 1 , 2 } ; EOF");

    // https://marc.info/?l=gcc-patches&m=151047200615291&w=2
    assertThat(parse(
      "#define CALL(F, ...) F(7 __VA_OPT__(,) __VA_ARGS__)\n"
        + "CALL(f1);"))
      .isEqualTo("f1 ( 7 ) ; EOF");

    assertThat(parse(
      "#define CALL(F, ...) F(7 __VA_OPT__(,) __VA_ARGS__)\n"
        + "CALL(f1, );"))
      .isEqualTo("f1 ( 7 ) ; EOF");

    assertThat(parse(
      "#define CALL(F, ...) F(7 __VA_OPT__(,) __VA_ARGS__)\n"
        + "CALL(f1, 1);"))
      .isEqualTo("f1 ( 7 , 1 ) ; EOF");

    assertThat(parse(
      "#define CP(F, X, Y, ...) F(__VA_OPT__(X ## Y,) __VA_ARGS__)\n"
        + "CP(f0, one, two);"))
      .isEqualTo("f0 ( ) ; EOF");

    assertThat(parse(
      "#define CP(F, X, Y, ...) F(__VA_OPT__(X ## Y,) __VA_ARGS__)\n"
        + "CP(f0, one, two, );"))
      .isEqualTo("f0 ( ) ; EOF");

    assertThat(parse(
      "#define CP(F, X, Y, ...) F(__VA_OPT__(X ## Y,) __VA_ARGS__)\n"
        + "CP(f0, one, two, 3);"))
      .isEqualTo("f0 ( onetwo , 3 ) ; EOF");

    assertThat(parse(
      "#define CS(F, ...) F(__VA_OPT__(s(# __VA_ARGS__)))\n"
        + "CS(f0);"))
      .isEqualTo("f0 ( ) ; EOF");

    assertThat(parse(
      "#define CS(F, ...) F(__VA_OPT__(s(# __VA_ARGS__)))\n"
        + "CS(f1, 1, 2, 3, 4);"))
      .isEqualTo("f1 ( s ( \"1,2,3,4\" ) ) ; EOF");

    assertThat(parse(
      "#define D(F, ...) F(__VA_OPT__(__VA_ARGS__) __VA_OPT__(,) __VA_ARGS__)\n"
        + "D(f0);"))
      .isEqualTo("f0 ( ) ; EOF");

    assertThat(parse(
      "#define D(F, ...) F(__VA_OPT__(__VA_ARGS__) __VA_OPT__(,) __VA_ARGS__)\n"
        + "D(f2, 1);"))
      .isEqualTo("f2 ( 1 , 1 ) ; EOF");

    assertThat(parse(
      "#define D(F, ...) F(__VA_OPT__(__VA_ARGS__) __VA_OPT__(,) __VA_ARGS__)\n"
        + "D(f2, 1, 2);"))
      .isEqualTo("f2 ( 1 , 2 , 1 , 2 ) ; EOF");

    assertThat(parse(
      "#define CALL0(...) __VA_OPT__(f2)(0 __VA_OPT__(,)__VA_ARGS__)\n"
        + "int* ptr = CALL0();"))
      .isEqualTo("int * ptr = ( 0 ) ; EOF");

    assertThat(parse(
      "#define CALL0(...) __VA_OPT__(f2)(0 __VA_OPT__(,)__VA_ARGS__)\n"
        + "CALL0(23);"))
      .isEqualTo("f2 ( 0 , 23 ) ; EOF");
  }

  @Test
  void stringification() {
    // default use case
    assertThat(parse(
      "#define make_string(x) #x\n"
        + "string s = make_string(a test);"))
      .isEqualTo("string s = \"a test\" ; EOF");

    // leading and trailing spaces were trimmed,
    // space between words was compressed to a single space character
    assertThat(parse(
      "#define make_string(x) #x\n"
        + "string s = make_string(   a    test   );"))
      .isEqualTo("string s = \"a test\" ; EOF");

    // the quotes were automatically converted
    assertThat(parse(
      "#define make_string(x) #x\n"
        + "string s = make_string(\"a\" \"test\");"))
      .isEqualTo("string s = \"\\\"a\\\" \\\"test\\\"\" ; EOF");

    // the slash were automatically converted
    assertThat(parse(
      "#define make_string(x) #x\n"
        + "string s = make_string(a\\test);"))
      .isEqualTo("string s = \"a\\\\test\" ; EOF");

    // If the token is a macro, the macro is not expanded
    // - the macro name is converted into a string.
    assertThat(parse(
      "#define make_string(x) #x\n"
        + "#define COMMA ,\n"
        + "string s = make_string(a COMMA test);"))
      .isEqualTo("string s = \"a COMMA test\" ; EOF");

    assertThat(parse(
      "#define F abc\n"
        + "#define B def\n"
        + "#define FB(arg) #arg\n"
        + "string s = FB(F B);"))
      .isEqualTo("string s = \"F B\" ; EOF");

    assertThat(parse(
      "#define F abc\n"
        + "#define B def\n"
        + "#define FB(arg) #arg\n"
        + "#define FB1(arg) FB(arg)\n"
        + "string s = FB1(F B);"))
      .isEqualTo("string s = \"abc def\" ; EOF");

    assertThat(parse(
      "#define F abc\n"
        + "#define B def\n"
        + "#define FB(arg) #arg\n"
        + "#define FB1(arg) FB(arg)\n"
        + "string s = FB1(F\\B);"))
      .isEqualTo("string s = \"abc\\\\def\" ; EOF");

    assertThat(parse(
      "#define F abc\n"
        + "#define B def\n"
        + "#define FB(arg) #arg\n"
        + "#define FB1(arg) FB(arg)\n"
        + "string s = FB1(F/B);"))
      .isEqualTo("string s = \"abc/def\" ; EOF");

    assertThat(parse(
      "#define SC_METHOD(func) declare_method_process( func ## _handle, #func, func )\n"
        + "SC_METHOD(test);"))
      .isEqualTo("declare_method_process ( test_handle , \"test\" , test ) ; EOF");
  }

  @Test
  void concatenation() {
    assertThat(parse(
      "#define A t ## 1\n"
        + "int i = A;"))
      .isEqualTo("int i = t1 ; EOF");

    assertThat(parse(
      "#define A(p) p ## 1\n"
        + "t = A(t);"))
      .isEqualTo("t = t1 ; EOF");

    assertThat(parse(
      "#define A(p1,p2) p1 ## p2\n"
        + "t = A(a,b);"))
      .isEqualTo("t = ab ; EOF");

    assertThat(parse(
      "#define A(p1,p2) p1 ## ## p2\n"
        + "t = A(a,b);"))
      .isEqualTo("t = ab ; EOF");

    assertThat(parse(
      "#define ab 1\n"
        + "#define A(p1,p2) p1 ## p2\n"
        + "t = A(a,b);"))
      .isEqualTo("t = 1 ; EOF");

    assertThat(parse(
      "#define macro_start i ## n ##t m         ##ain(void);\n"
        + "macro_start"))
      .isEqualTo("int main ( void ) ; EOF");

    assertThat(parse(
      "#define A B(cf)\n"
        + "#define B(n) 0x##n\n"
        + "i = A;"))
      .isEqualTo("i = 0xcf ; EOF");
  }

  @Test
  void undef() {
    assertThat(parse(
      "#define FOO 4\n"
        + "#undef FOO\n"
        + "x = FOO;"))
      .isEqualTo("x = FOO ; EOF");

    assertThat(parse(
      "#define BUFSIZE 1020\n"
        + "#define TABLESIZE BUFSIZE\n"
        + "#undef BUFSIZE\n"
        + "#define BUFSIZE 37\n"
        + "int i = TABLESIZE;"))
      .isEqualTo("int i = 37 ; EOF");
  }

  @Test
  void redefining_macros() {
    assertThat(parse(
      "#define FOO 1\n"
        + "#define FOO 2\n"
        + "int i = FOO;"))
      .isEqualTo("int i = 2 ; EOF");
  }

  @Test
  void prescan() {
    assertThat(parse(
      "#define AFTERX(x) X_ ## x\n"
        + "#define XAFTERX(x) AFTERX(x)\n"
        + "#define TABLESIZE 1024\n"
        + "#define BUFSIZE TABLESIZE\n"
        + "int i = XAFTERX(BUFSIZE);"))
      .isEqualTo("int i = X_1024 ; EOF");

    assertThat(parse(
      "#define FOOBAR 1\n"
        + "#define CHECK(a, b) (( a ## b + 1 == 2))\n"
        + "#if CHECK(FOO , BAR)\n"
        + "i = 1;\n"
        + "#else\n"
        + "0\n"
        + "#endif"))
      .isEqualTo("i = 1 ; EOF");
  }

  @Test
  void self_referential_macros() {
    assertThat(parse(
      "#define EPERM EPERM\n"
        + "a = EPERM;"))
      .isEqualTo("a = EPERM ; EOF");

    assertThat(parse(
      "#define foo (4 + foo)\n"
        + "i = foo;"))
      .isEqualTo("i = ( 4 + foo ) ; EOF");

    assertThat(parse(
      "#define x (4 + y)\n"
        + "#define y (2 * x)\n"
        + "i = x;"))
      .isEqualTo("i = ( 4 + ( 2 * x ) ) ; EOF");
  }

  @Test
  void has_include() {
    assertThat(parse(
      "#if __has_include\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#if defined(__has_include)\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#if __has_include(<optional>)\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 0 ; EOF");

    assertThat(parse(
      "#if __has_include(\"optional\")\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 0 ; EOF");

    assertThat(parse(
      "#define EXISTS __has_include(\"optional\")\n"
        + "#if EXISTS\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 0 ; EOF");
  }

  @Test
  void featureCheckingMacros() {
    //
    // source: https://clang.llvm.org/docs/LanguageExtensions.html
    //
    assertThat(parse(
      "#ifdef __has_builtin\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_feature\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_extension\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_cpp_attribute\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_c_attribute\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_attribute\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_attribute\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_declspec_attribute\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __is_identifier\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_attribute\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_include\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_include_next\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");

    assertThat(parse(
      "#ifdef __has_warning\n"
        + "#   define OK 1\n"
        + "#else\n"
        + "#   define OK 0\n"
        + "#endif\n"
        + "r = OK;"))
      .isEqualTo("r = 1 ; EOF");
  }

}
