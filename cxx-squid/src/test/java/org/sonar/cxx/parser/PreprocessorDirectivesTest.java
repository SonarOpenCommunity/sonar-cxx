/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.parser;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import com.sonar.sslr.api.Grammar;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

import static org.sonar.sslr.tests.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PreprocessorDirectivesTest {

  Parser<Grammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  Grammar g = p.getGrammar();

  private String serialize(AstNode root) {
    List<String> values = new LinkedList<String>();
    iterate(root, values);
    String s = StringUtils.join(values, " ");
    return s;
  }

  private void iterate(AstNode node, List<String> values) {
    while (node != null) {
      AstNode child = node.getFirstChild();
      if (child != null) {
        iterate(child, values);
      } else {
        if (node.getType() instanceof CxxGrammarImpl == false) {
          values.add(node.getTokenValue());
        }
      }
      node = node.getNextSibling();
    }
  }

  @Test
  public void preprocessorDirectives() {
    assertThat(p).matches(
      "#define IDX 10\n"
      + "array[IDX];");
  }

  @Test
  public void hashhash_related_parsing_problem() {
    // this reproduces a macros expansion problem where
    // necessary whitespaces get lost

    assertThat(p).matches(
      "#define CASES CASE(00)\n"
      + "#define CASE(n) case 0x##n:\n"
      + "void foo()  {\n"
      + "switch (1) {\n"
      + "CASES\n"
      + "break;\n"
      + "}\n"
      + "}\n");
  }

  @Test
  public void object_like_macros() {
    assert (serialize(p.parse(
      "#define BUFFER_SIZE 1024\n"
      + "foo = (char *) malloc (BUFFER_SIZE);"))
      .equals("foo = ( char * ) malloc ( 1024 ) ; EOF"));

    assert (serialize(p.parse(
      "#define NUMBERS 1, \\\n"
      + "2, \\\n"
      + "3\n"
      + "int x[] = { NUMBERS };"))
      .equals("int x [ ] = { 1 , 2 , 3 } ; EOF"));

    assert (p.parse(
      "#define TABLESIZE BUFSIZE\n"
      + "#define BUFSIZE 1024\n"
      + "TABLESIZE")
      .getTokenValue().equals("1024"));

    assert (serialize(p.parse(
      "#define A 1 + 1\n"
      + "A;"))
      .equals("1 + 1 ; EOF"));

    assert (serialize(p.parse(
      "#define A a // Comment\n"
      + "A;"))
      .equals("a ; EOF"));

//    @todo
//    assert (serialize(p.parse(
//      "#define A_B A/*Comment*/B\n"
//      +" A_B;"))
//      .equals("A B ; EOF"));    
  }

  @Test
  public void function_like_macros() {
    assert (serialize(p.parse(
      "#define lang_init() c_init()\n"
      + "lang_init();"))
      .equals("c_init ( ) ; EOF"));
  }

  @Test
  public void macro_arguments() {
    assert (serialize(p.parse(
      "#define min(X, Y)  ((X) < (Y) ? (X) : (Y))\n"
      + "min(a + 28, *p);"))
      .equals("( ( a + 28 ) < ( * p ) ? ( a + 28 ) : ( * p ) ) ; EOF"));

    assert (serialize(p.parse(
      "#define foo(x) x, \"x\"\n"
      + "foo(bar);"))
      .equals("bar , \"x\" ; EOF"));
  }

  @Test
  public void variadic_macros() {
    assert (serialize(p.parse(
      "#define eprintf(...) fprintf (stderr, __VA_ARGS__)\n"
      + "eprintf(\"%s:%d: \", input_file, lineno);"))
      .equals("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF"));

    assert (serialize(p.parse(
      "#define eprintf(args...) fprintf (stderr, args)\n"
      + "eprintf(\"%s:%d: \", input_file, lineno);"))
      .equals("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF"));

    assert (serialize(p.parse(
      "#define eprintf(format, ...) fprintf (stderr, format, __VA_ARGS__)\n"
      + "eprintf(\"%s:%d: \", input_file, lineno);"))
      .equals("fprintf ( stderr , \"%s:%d: \" , input_file , lineno ) ; EOF"));

    // GNU CPP: Vou are allowed to leave the variable argument out entirely
    assert (serialize(p.parse(
      "#define eprintf(format, ...) fprintf (stderr, format, __VA_ARGS__)\n"
      + "eprintf(\"success!\");"))
      .equals("fprintf ( stderr , \"success!\" , ) ; EOF"));

//    @todo
//    // GNU CPP: special meaning of token paste operator - if variable argument is left out then the comma before the ‘##’ will be deleted.
//    assert (serialize(p.parse(
//      "#define eprintf(format, ...) fprintf (stderr, format, ##__VA_ARGS__)\n"
//      + "eprintf(\"success!\");"))
//      .equals("fprintf ( stderr , \"success!\" ) ; EOF"));    
  }

  @Test
  public void stringification() {
    assert (p.parse(
      "#define str(s) #s\n"
      + "str(t)")
      .getTokenValue().equals("\"t\""));

    assert (p.parse(
      "#define xstr(s) str(s)\n"
      + "#define str(s) #s\n"
      + "#define foo 4\n"
      + "str(foo)")
      .getTokenValue().equals("\"4\""));
  }

  @Test
  public void concatenation() {
    assert (p.parse(
      "#define A t ## 1\n"
      + "A")
      .getTokenValue().equals("t1"));

    assert (p.parse(
      "#define A(p) p ## 1\n"
      + "A(t)")
      .getTokenValue().equals("t1"));

    assert (serialize(p.parse(
      "#define macro_start i ## n ##t m         ##ain(void);\n"
      + "macro_start"))
      .equals("int main ( void ) ; EOF"));

//    @todo
//    assert (p.parse(
//      "#define A B(cf)\n"
//      + "#define B 0x##n\n"
//      + "A")
//      .getTokenValue().equals("0xn(cf)"));
    
//    @todo
//    assert (p.parse(
//      "#define A B(cf)\n"
//      + "#define B(n) 0x##n\n"
//      + "A")
//      .getTokenValue().equals("0xcf"));
  }

  @Test
  public void undef() {
    assert (serialize(p.parse(
      "#define FOO 4\n"
      + "#undef FOO\n"
      + "x = FOO;"))
      .equals("x = FOO ; EOF"));

//    @todo
//    assert (p.parse(
//      "#define BUFSIZE 1020"
//      + "#define TABLESIZE BUFSIZE"
//      + "#undef BUFSIZE"
//      + "#define BUFSIZE 37"
//      + "TABLESIZE")
//      .getTokenValue().equals("37"));
  }

  @Test
  public void redefining_macros() {
    assert (p.parse(
      "#define FOO 1\n"
      + "#define FOO 2\n"
      + "FOO")
      .getTokenValue().equals("2"));
  }

  @Test
  public void prescan() {
    assert (p.parse(
      "#define AFTERX(x) X_ ## x\n"
      + "#define XAFTERX(x) AFTERX(x)\n"
      + "#define TABLESIZE 1024\n"
      + "#define BUFSIZE TABLESIZE\n"
      + "XAFTERX(BUFSIZE)")
      .getTokenValue().equals("X_1024"));

    assert (p.parse(
      "#define FOOBAR 1\n"
      + "#define CHECK(a, b) (( a ## b + 1 == 2))\n"
      + "#if CHECK(FOO , BAR)\n"
      + "1\n"
      + "#else\n"
      + "0\n"
      + "#endif")
      .getTokenValue().equals("1"));
  }

  @Test
  public void self_referential_macros() {
//    @todo         
//    assert (p.parse(
//      "#define EPERM EPERM"
//      + "EPERM")
//      .getTokenValue().equals("EPERM"));

    assert (serialize(p.parse(
      "#define foo (4 + foo)\n"
      + "foo;"))
      .equals("( 4 + foo ) ; EOF"));

    assert (serialize(p.parse(
      "#define x (4 + y)\n"
      + "#define y (2 * x)\n"
      + "x;"))
      .equals("( 4 + ( 2 * x ) ) ; EOF"));
  }
}
