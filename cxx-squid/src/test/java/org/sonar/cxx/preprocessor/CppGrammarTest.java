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
package org.sonar.cxx.preprocessor;

import com.sonar.sslr.impl.events.ExtendedStackTrace;
import com.sonar.sslr.impl.events.ExtendedStackTraceStream;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.impl.Parser;
import org.junit.Test;
import static org.sonar.sslr.tests.Assertions.assertThat;
import com.sonar.sslr.api.Grammar;


public class CppGrammarTest {
  private Parser<Grammar> p = Parser.builder(CppGrammar.create())
    .withLexer(CppLexer.create())
    .build();
  private Grammar g = p.getGrammar();

  @Test
  public void preprocessorLine() {
    g.rule(CppGrammar.defineLine).mock();
    g.rule(CppGrammar.includeLine).mock();
    g.rule(CppGrammar.ifdefLine).mock();
    g.rule(CppGrammar.ifLine).mock();
    g.rule(CppGrammar.elifLine).mock();
    g.rule(CppGrammar.elseLine).mock();
    g.rule(CppGrammar.endifLine).mock();
    g.rule(CppGrammar.undefLine).mock();
    g.rule(CppGrammar.lineLine).mock();
    g.rule(CppGrammar.errorLine).mock();
    g.rule(CppGrammar.pragmaLine).mock();
    g.rule(CppGrammar.warningLine).mock();

    assertThat(p).matches("defineLine");
    assertThat(p).matches("includeLine");
    assertThat(p).matches("ifdefLine");
    assertThat(p).matches("ifLine");
    assertThat(p).matches("elifLine");
    assertThat(p).matches("elseLine");
    assertThat(p).matches("endifLine");
    assertThat(p).matches("undefLine");
    assertThat(p).matches("lineLine");
    assertThat(p).matches("errorLine");
    assertThat(p).matches("pragmaLine");
    assertThat(p).matches("warningLine");
  }

  @Test
  public void preprocessorLine_reallife() {
    assertThat(p).matches("#include      <ace/config-all.h>");
    assertThat(p).matches("#endif  // LLVM_DEBUGINFO_DWARFDEBUGRANGELIST_H");
    assertThat(p).matches("#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0");
    assertThat(p).matches("#include <algorithm>");
    assertThat(p).matches("# /* See http://www.boost.org for most recent version. */");
    assertThat(p).matches("#if (C(A() && B()))");
  }

  @Test
  public void defineLine_reallife() {
    p.setRootRule(g.rule(CppGrammar.defineLine));

    assertThat(p).matches("#define ALGOSTUFF_HPPEOF");
    assertThat(p).matches("#define lala(a, b) a b");
    assertThat(p).matches("#define new dew_debug");
    assertThat(p).matches("#define macro(true, false) a");
    assertThat(p).matches("#define TRUE true");
    assertThat(p).matches("#define true TRUE");
    assertThat(p).matches("# define __glibcxx_assert(_Condition)");
  }

  @Test
  public void define_containing_argumentList() {
    AstNode define = p.parse("#define lala(a, b) a b");
    assert (define.findFirstChild(CppGrammar.parameterList) != null);
  }

  @Test
  public void functionlikeMacroDefinition() {
    p.setRootRule(g.rule(CppGrammar.functionlikeMacroDefinition));

    g.rule(CppGrammar.replacementList).mock();
    g.rule(CppGrammar.argumentList).mock();
    g.rule(CppGrammar.ppToken).mock();

    assertThat(p).matches("#define ppToken( argumentList ) replacementList");
    assertThat(p).matches("#define ppToken(argumentList) replacementList");
    assertThat(p).matches("#define ppToken( ... ) replacementList");
    assertThat(p).matches("#define ppToken(...) replacementList");
    assertThat(p).matches("#define ppToken( argumentList, ... ) replacementList");
    assertThat(p).matches("#define ppToken(argumentList, ...) replacementList");
  }

  @Test
  public void functionlikeMacroDefinition_reallife() {
    p.setRootRule(g.rule(CppGrammar.functionlikeMacroDefinition));

    assertThat(p).matches("#define foo() bar");
    assertThat(p).matches("#define foo() ()");
    assertThat(p).matches("#define foo(a) bar");
    assertThat(p).matches("#define foo(a,b) ab");
  }

  @Test
  public void objectlikeMacroDefinition() {
    p.setRootRule(g.rule(CppGrammar.objectlikeMacroDefinition));

    g.rule(CppGrammar.replacementList).mock();
    g.rule(CppGrammar.ppToken).mock();

    assertThat(p).matches("#define ppToken replacementList");
  }

  @Test
  public void objectlikeMacroDefinition_reallife() {
    p.setRootRule(g.rule(CppGrammar.objectlikeMacroDefinition));

    assertThat(p).matches("#define foo");
    assertThat(p).matches("#define foo bar");
    assertThat(p).matches("#define foo ()");
    assertThat(p).matches("#define new new_debug");
  }

  @Test
  public void replacementList() {
    p.setRootRule(g.rule(CppGrammar.replacementList));

    assertThat(p).matches("");
    assertThat(p).matches("ppToken");
    assertThat(p).matches("#ppToken");
    assertThat(p).matches("ppToken ## ppToken");
  }

  @Test
  public void argumentList() {
    p.setRootRule(g.rule(CppGrammar.argumentList));

    assertThat(p).matches("foo");
    assertThat(p).matches("foo, bar");
    assertThat(p).matches("4, 1");
    assertThat(p).matches("4, call()");
    assertThat(p).matches("A() && B()");
  }
  
  @Test
  public void argument() {
    p.setRootRule(g.rule(CppGrammar.argument));
    
    assertThat(p).matches("a");
    assertThat(p).matches("call()");
    assertThat(p).matches("A() && B()");
  }

  @Test
  public void somethingContainingParantheses() {
    p.setRootRule(g.rule(CppGrammar.somethingContainingParantheses));
    
    assertThat(p).matches("call()");
    assertThat(p).matches("()");
  }

  @Test
  public void somethingWithoutParantheses() {
    p.setRootRule(g.rule(CppGrammar.somethingWithoutParantheses));
    
    assertThat(p).matches("abc");
  }

  @Test
  public void ppToken() {
    p.setRootRule(g.rule(CppGrammar.ppToken));

    assertThat(p).matches("foo");
    assertThat(p).matches("(");
    assertThat(p).matches(")");
    assertThat(p).matches("*");
  }

  @Test
  public void includeLine() {
    p.setRootRule(g.rule(CppGrammar.includeLine));

    g.rule(CppGrammar.ppToken).mock();

    assertThat(p).matches("#include <ppToken>");
    assertThat(p).matches("#include_next <ppToken>");
    assertThat(p).matches("#include ppToken");
    assertThat(p).matches("#include \"jabadu\"");
  }

  @Test
  public void expandedIncludeBody() {
    p.setRootRule(g.rule(CppGrammar.expandedIncludeBody));
    
    g.rule(CppGrammar.ppToken).mock();
    
    assertThat(p).matches("<ppToken>");
    assertThat(p).matches("\"jabadu\"");
  }
  
  @Test
  public void includeLine_reallife() {
    p.setRootRule(g.rule(CppGrammar.includeLine));

    assertThat(p).matches("#include <file>");
    assertThat(p).matches("#include <file.h>");
    assertThat(p).matches("#include <fi_le.h>");
    assertThat(p).matches("#include \"file\"");
    assertThat(p).matches("#include \"file.h\"");
    assertThat(p).matches("#include \"fi_le.h\"");
    assertThat(p).matches("#include <bits/typesizes.h>	/* Defines __*_T_TYPE macros.  */");
    assertThat(p).matches("#include /**/ <ace/config-all.h>");
    assertThat(p).matches("#include <math.h> /**/ /**/");
    assertThat(p).matches("#include USER_CONFIG");
  }

  @Test
  public void ifdefLine() {
    p.setRootRule(g.rule(CppGrammar.ifdefLine));

    assertThat(p).matches("#ifdef foo");
    assertThat(p).matches("#ifndef foo");
    assertThat(p).matches("#ifdef __GNUC__ // aka CONST but following LLVM Conventions.");
    assertThat(p).matches("#ifdef /**/ lala /**/ ");
  }

  @Test
  public void elseLine() {
    p.setRootRule(g.rule(CppGrammar.elseLine));

    assertThat(p).matches("#else");
    assertThat(p).matches("#else  // if lala");
  }

  @Test
  public void endifLine() {
    p.setRootRule(g.rule(CppGrammar.endifLine));

    assertThat(p).matches("#endif");
    assertThat(p).matches("#endif  // LLVM_DEBUGINFO_DWARFDEBUGRANGELIST_H");
  }

  @Test
  public void undefLine() {
    p.setRootRule(g.rule(CppGrammar.undefLine));

    assertThat(p).matches("#undef foo");
  }

  @Test
  public void lineLine() {
    p.setRootRule(g.rule(CppGrammar.lineLine));

    assertThat(p).matches("#line foo bar");
  }

  @Test
  public void errorLine() {
    p.setRootRule(g.rule(CppGrammar.errorLine));

    assertThat(p).matches("#error foo");
    assertThat(p).matches("#error");
  }

  @Test
  public void pragmaLine() {
    p.setRootRule(g.rule(CppGrammar.pragmaLine));

    assertThat(p).matches("#pragma foo");
  }

  @Test
  public void warningLine() {
    p.setRootRule(g.rule(CppGrammar.warningLine));

    assertThat(p).matches("#warning foo");
  }

  @Test
  public void miscLine() {
    p.setRootRule(g.rule(CppGrammar.miscLine));

    assertThat(p).matches("#");
    assertThat(p).matches("# lala");
    assertThat(p).matches("#lala");
  }

  @Test
  public void ifLine() {
    p.setRootRule(g.rule(CppGrammar.ifLine));

    g.rule(CppGrammar.constantExpression).mock();

    assertThat(p).matches("#if constantExpression");
  }

  @Test
  public void elifLine() {
    p.setRootRule(g.rule(CppGrammar.elifLine));

    g.rule(CppGrammar.constantExpression).mock();

    assertThat(p).matches("#elif constantExpression");
  }

  @Test
  public void ifLine_reallive() {
    p.setRootRule(g.rule(CppGrammar.ifLine));

    assertThat(p).matches("#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0");
    assertThat(p).matches("#if 0   // Re-enable once PR13021 is fixed.");
    assertThat(p).matches("#if ((OSVER(NTDDI_VERSION) == NTDDI_WIN2K) && (1))");
    
    assert (p.parse("#if A (4, 1)").findFirstChild(CppGrammar.functionlikeMacro) != null);
    assert (p.parse("#if A ()").findFirstChild(CppGrammar.functionlikeMacro) != null);
    assert (p.parse("#if A()").findFirstChild(CppGrammar.functionlikeMacro) != null);

    assert (p.parse("#if defined(A)").findFirstChild(CppGrammar.definedExpression) != null);
    assert (p.parse("#if defined (A)").findFirstChild(CppGrammar.definedExpression) != null);
    assert (p.parse("#if defined A").findFirstChild(CppGrammar.definedExpression) != null);
  }

  @Test
  public void constantExpression() {
    p.setRootRule(g.rule(CppGrammar.constantExpression));

    g.rule(CppGrammar.conditionalExpression).mock();

    assertThat(p).matches("conditionalExpression");
  }

  @Test
  public void constantExpression_reallive() {
    p.setRootRule(g.rule(CppGrammar.constantExpression));

    assertThat(p).matches("(1 || 0) && (0 && 1)");
    assertThat(p).matches("(1)");
    assertThat(p).matches("( /**/ 1 /**/ )");
    assertThat(p).matches("__has_feature(cxx_rvalue_references)");
    assertThat(p).matches("__has_feature(/**/ cxx_rvalue_references /**/ )");
    assertThat(p).matches("(C(A() && B()))");
  }

  @Test
  public void conditionalExpression() {
    p.setRootRule(g.rule(CppGrammar.conditionalExpression));

    g.rule(CppGrammar.logicalOrExpression).mock();
    g.rule(CppGrammar.expression).mock();

    assertThat(p).matches("logicalOrExpression");
    assertThat(p).matches("logicalOrExpression ? expression : logicalOrExpression");
    assertThat(p).matches("logicalOrExpression ? : logicalOrExpression");
  }

  @Test
  public void logicalOrExpression() {
    p.setRootRule(g.rule(CppGrammar.logicalOrExpression));

    g.rule(CppGrammar.logicalAndExpression).mock();

    assertThat(p).matches("logicalAndExpression");
    assertThat(p).matches("logicalAndExpression || logicalAndExpression");
  }

  @Test
  public void logicalAndExpression() {
    p.setRootRule(g.rule(CppGrammar.logicalAndExpression));

    g.rule(CppGrammar.inclusiveOrExpression).mock();

    assertThat(p).matches("inclusiveOrExpression");
    assertThat(p).matches("inclusiveOrExpression && inclusiveOrExpression");
  }

  @Test
  public void logicalAndExpression_reallive() {
    p.setRootRule(g.rule(CppGrammar.logicalAndExpression));

    assertThat(p).matches("A() && B()");
  }
  
  @Test
  public void inclusiveOrExpression() {
    p.setRootRule(g.rule(CppGrammar.inclusiveOrExpression));

    g.rule(CppGrammar.exclusiveOrExpression).mock();

    assertThat(p).matches("exclusiveOrExpression");
    assertThat(p).matches("exclusiveOrExpression | exclusiveOrExpression");
  }

  @Test
  public void exclusiveOrExpression() {
    p.setRootRule(g.rule(CppGrammar.exclusiveOrExpression));

    g.rule(CppGrammar.andExpression).mock();

    assertThat(p).matches("andExpression");
    assertThat(p).matches("andExpression ^ andExpression");
  }

  @Test
  public void andExpression() {
    p.setRootRule(g.rule(CppGrammar.andExpression));

    g.rule(CppGrammar.equalityExpression).mock();

    assertThat(p).matches("equalityExpression");
    assertThat(p).matches("equalityExpression & equalityExpression");
  }

  @Test
  public void equalityExpression() {
    p.setRootRule(g.rule(CppGrammar.equalityExpression));

    g.rule(CppGrammar.relationalExpression).mock();

    assertThat(p).matches("relationalExpression");
    assertThat(p).matches("relationalExpression == relationalExpression");
    assertThat(p).matches("relationalExpression != relationalExpression");
  }

  @Test
  public void relationalExpression() {
    p.setRootRule(g.rule(CppGrammar.relationalExpression));

    g.rule(CppGrammar.shiftExpression).mock();

    assertThat(p).matches("shiftExpression");
    assertThat(p).matches("shiftExpression < shiftExpression");
    assertThat(p).matches("shiftExpression > shiftExpression");
    assertThat(p).matches("shiftExpression <= shiftExpression");
    assertThat(p).matches("shiftExpression >= shiftExpression");
  }

  @Test
  public void shiftExpression() {
    p.setRootRule(g.rule(CppGrammar.shiftExpression));

    g.rule(CppGrammar.additiveExpression).mock();

    assertThat(p).matches("additiveExpression");
    assertThat(p).matches("additiveExpression << additiveExpression");
    assertThat(p).matches("additiveExpression >> additiveExpression");
  }

  @Test
  public void additiveExpression() {
    p.setRootRule(g.rule(CppGrammar.additiveExpression));

    g.rule(CppGrammar.multiplicativeExpression).mock();

    assertThat(p).matches("multiplicativeExpression");
    assertThat(p).matches("multiplicativeExpression + multiplicativeExpression");
    assertThat(p).matches("multiplicativeExpression - multiplicativeExpression");
  }

  @Test
  public void multiplicativeExpression() {
    p.setRootRule(g.rule(CppGrammar.multiplicativeExpression));

    g.rule(CppGrammar.unaryExpression).mock();

    assertThat(p).matches("unaryExpression");
    assertThat(p).matches("unaryExpression * unaryExpression");
    assertThat(p).matches("unaryExpression / unaryExpression");
    assertThat(p).matches("unaryExpression % unaryExpression");
  }

  @Test
  public void unaryExpression() {
    p.setRootRule(g.rule(CppGrammar.unaryExpression));

    g.rule(CppGrammar.multiplicativeExpression).mock();
    g.rule(CppGrammar.primaryExpression).mock();
    g.rule(CppGrammar.unaryOperator).mock();

    assertThat(p).matches("unaryOperator multiplicativeExpression");
    assertThat(p).matches("primaryExpression");
  }

  @Test
  public void primaryExpression() {
    p.setRootRule(g.rule(CppGrammar.primaryExpression));

    g.rule(CppGrammar.literal).mock();
    g.rule(CppGrammar.expression).mock();
    g.rule(CppGrammar.definedExpression).mock();

    assertThat(p).matches("literal");
    assertThat(p).matches("( expression )");
    assertThat(p).matches("definedExpression");
    assertThat(p).matches("foo");
  }

  @Test
  public void primaryExpression_reallive() {
    p.setRootRule(g.rule(CppGrammar.primaryExpression));
    
    assertThat(p).matches("(C(A() && B()))");
  }
  
  @Test
  public void expression() {
    p.setRootRule(g.rule(CppGrammar.expression));

    g.rule(CppGrammar.conditionalExpression).mock();

    assertThat(p).matches("conditionalExpression");
    assertThat(p).matches("conditionalExpression, conditionalExpression");
  }

  @Test
  public void expression_reallive() {
    p.setRootRule(g.rule(CppGrammar.expression));

    assertThat(p).matches("C(A() && B())");
  }
  
  @Test
  public void definedExpression() {
    p.setRootRule(g.rule(CppGrammar.definedExpression));

    assertThat(p).matches("defined LALA");
    assertThat(p).matches("defined (LALA)");
    assertThat(p).matches("defined(LALA)");
  }

  @Test
  public void functionlikeMacro() {
    p.setRootRule(g.rule(CppGrammar.functionlikeMacro));

    g.rule(CppGrammar.argumentList).mock();

    assertThat(p).matches("__has_feature(argumentList)");
  }

  @Test
  public void functionlikeMacro_reallife() {
    p.setRootRule(g.rule(CppGrammar.functionlikeMacro));

    assertThat(p).matches("__has_feature(cxx_rvalue)");
    assertThat(p).matches("__has_feature(cxx_rvalue, bla)");
    assertThat(p).matches("__GNUC_PREREQ (4, 1)");
    assertThat(p).matches("A ()");
    assertThat(p).matches("A()");
    assertThat(p).matches("BOOST_WORKAROUND(BOOST_MSVC, < 1300)");
    assertThat(p).matches("BOOST_WORKAROUND(< 1300)");
    assertThat(p).matches("BOOST_WORKAROUND(a, call())");
    assertThat(p).matches("C(A() && B())");
  }
}
