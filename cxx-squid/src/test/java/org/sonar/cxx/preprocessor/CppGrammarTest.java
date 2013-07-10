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

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;

public class CppGrammarTest {
  ExtendedStackTrace stackTrace = new ExtendedStackTrace();
  private Parser<CppGrammar> p = Parser.builder(new CppGrammar())
    .withLexer(CppLexer.create())
    .setExtendedStackTrace(stackTrace)
    .build();
  private CppGrammar g = p.getGrammar();

  @Test
  public void preprocessorLine() {
    g.defineLine.mock();
    g.includeLine.mock();
    g.ifdefLine.mock();
    g.ifLine.mock();
    g.elifLine.mock();
    g.elseLine.mock();
    g.endifLine.mock();
    g.undefLine.mock();
    g.lineLine.mock();
    g.errorLine.mock();
    g.pragmaLine.mock();
    g.warningLine.mock();

    assertThat(p, parse("defineLine"));
    assertThat(p, parse("includeLine"));
    assertThat(p, parse("ifdefLine"));
    assertThat(p, parse("ifLine"));
    assertThat(p, parse("elifLine"));
    assertThat(p, parse("elseLine"));
    assertThat(p, parse("endifLine"));
    assertThat(p, parse("undefLine"));
    assertThat(p, parse("lineLine"));
    assertThat(p, parse("errorLine"));
    assertThat(p, parse("pragmaLine"));
    assertThat(p, parse("warningLine"));
  }

  @Test
  public void preprocessorLine_reallife() {
    assertThat(p, parse("#include      <ace/config-all.h>"));
    assertThat(p, parse("#endif  // LLVM_DEBUGINFO_DWARFDEBUGRANGELIST_H"));
    assertThat(p, parse("#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0"));
    assertThat(p, parse("#include <algorithm>"));
    assertThat(p, parse("# /* See http://www.boost.org for most recent version. */"));
    assertThat(p, parse("#if (C(A() && B()))"));
  }

  @Test
  public void defineLine_reallife() {
    p.setRootRule(g.defineLine);

    assertThat(p, parse("#define ALGOSTUFF_HPPEOF"));
    assertThat(p, parse("#define lala(a, b) a b"));
    assertThat(p, parse("#define new dew_debug"));
    assertThat(p, parse("#define macro(true, false) a"));
    assertThat(p, parse("#define TRUE true"));
    assertThat(p, parse("#define true TRUE"));
    assertThat(p, parse("# define __glibcxx_assert(_Condition)"));
  }

  @Test
  public void define_containing_argumentList() {
    AstNode define = p.parse("#define lala(a, b) a b");
    assert (define.findFirstChild(g.parameterList) != null);
  }

  @Test
  public void functionlikeMacroDefinition() {
    p.setRootRule(g.functionlikeMacroDefinition);

    g.replacementList.mock();
    g.argumentList.mock();
    g.ppToken.mock();

    assertThat(p, parse("#define ppToken( argumentList ) replacementList"));
    assertThat(p, parse("#define ppToken(argumentList) replacementList"));
    assertThat(p, parse("#define ppToken( ... ) replacementList"));
    assertThat(p, parse("#define ppToken(...) replacementList"));
    assertThat(p, parse("#define ppToken( argumentList, ... ) replacementList"));
    assertThat(p, parse("#define ppToken(argumentList, ...) replacementList"));
  }

  @Test
  public void functionlikeMacroDefinition_reallife() {
    p.setRootRule(g.functionlikeMacroDefinition);

    assertThat(p, parse("#define foo() bar"));
    assertThat(p, parse("#define foo() ()"));
    assertThat(p, parse("#define foo(a) bar"));
    assertThat(p, parse("#define foo(a,b) ab"));
  }

  @Test
  public void objectlikeMacroDefinition() {
    p.setRootRule(g.objectlikeMacroDefinition);

    g.replacementList.mock();
    g.ppToken.mock();

    assertThat(p, parse("#define ppToken replacementList"));
  }

  @Test
  public void objectlikeMacroDefinition_reallife() {
    p.setRootRule(g.objectlikeMacroDefinition);

    assertThat(p, parse("#define foo"));
    assertThat(p, parse("#define foo bar"));
    assertThat(p, parse("#define foo ()"));
    assertThat(p, parse("#define new new_debug"));
  }

  @Test
  public void replacementList() {
    p.setRootRule(g.replacementList);

    assertThat(p, parse(""));
    assertThat(p, parse("ppToken"));
    assertThat(p, parse("#ppToken"));
    assertThat(p, parse("ppToken ## ppToken"));
  }

  @Test
  public void argumentList() {
    p.setRootRule(g.argumentList);

    assertThat(p, parse("foo"));
    assertThat(p, parse("foo, bar"));
    assertThat(p, parse("4, 1"));
    assertThat(p, parse("4, call()"));
    assertThat(p, parse("A() && B()"));
  }
  
  @Test
  public void argument() {
    p.setRootRule(g.argument);
    
    assertThat(p, parse("a"));
    assertThat(p, parse("call()"));
    assertThat(p, parse("A() && B()"));
  }

  @Test
  public void somethingContainingParantheses() {
    p.setRootRule(g.somethingContainingParantheses);
    
    assertThat(p, parse("call()"));
    assertThat(p, parse("()"));
  }

  @Test
  public void somethingWithoutParantheses() {
    p.setRootRule(g.somethingWithoutParantheses);
    
    assertThat(p, parse("abc"));
  }

  @Test
  public void ppToken() {
    p.setRootRule(g.ppToken);

    assertThat(p, parse("foo"));
    assertThat(p, parse("("));
    assertThat(p, parse(")"));
    assertThat(p, parse("*"));
  }

  @Test
  public void includeLine() {
    p.setRootRule(g.includeLine);

    g.ppToken.mock();

    assertThat(p, parse("#include <ppToken>"));
    assertThat(p, parse("#include_next <ppToken>"));
    assertThat(p, parse("#include ppToken"));
    assertThat(p, parse("#include \"jabadu\""));
  }

  @Test
  public void expandedIncludeBody() {
    p.setRootRule(g.expandedIncludeBody);
    
    g.ppToken.mock();
    
    assertThat(p, parse("<ppToken>"));
    assertThat(p, parse("\"jabadu\""));
  }
  
  @Test
  public void includeLine_reallife() {
    p.setRootRule(g.includeLine);

    assertThat(p, parse("#include <file>"));
    assertThat(p, parse("#include <file.h>"));
    assertThat(p, parse("#include <fi_le.h>"));
    assertThat(p, parse("#include \"file\""));
    assertThat(p, parse("#include \"file.h\""));
    assertThat(p, parse("#include \"fi_le.h\""));
    assertThat(p, parse("#include <bits/typesizes.h>	/* Defines __*_T_TYPE macros.  */"));
    assertThat(p, parse("#include /**/ <ace/config-all.h>"));
    assertThat(p, parse("#include <math.h> /**/ /**/"));
    assertThat(p, parse("#include USER_CONFIG"));
  }

  @Test
  public void ifdefLine() {
    p.setRootRule(g.ifdefLine);

    assertThat(p, parse("#ifdef foo"));
    assertThat(p, parse("#ifndef foo"));
    assertThat(p, parse("#ifdef __GNUC__ // aka CONST but following LLVM Conventions."));
    assertThat(p, parse("#ifdef /**/ lala /**/ "));
  }

  @Test
  public void elseLine() {
    p.setRootRule(g.elseLine);

    assertThat(p, parse("#else"));
    assertThat(p, parse("#else  // if lala"));
  }

  @Test
  public void endifLine() {
    p.setRootRule(g.endifLine);

    assertThat(p, parse("#endif"));
    assertThat(p, parse("#endif  // LLVM_DEBUGINFO_DWARFDEBUGRANGELIST_H"));
  }

  @Test
  public void undefLine() {
    p.setRootRule(g.undefLine);

    assertThat(p, parse("#undef foo"));
  }

  @Test
  public void lineLine() {
    p.setRootRule(g.lineLine);

    assertThat(p, parse("#line foo bar"));
  }

  @Test
  public void errorLine() {
    p.setRootRule(g.errorLine);

    assertThat(p, parse("#error foo"));
    assertThat(p, parse("#error"));
  }

  @Test
  public void pragmaLine() {
    p.setRootRule(g.pragmaLine);

    assertThat(p, parse("#pragma foo"));
  }

  @Test
  public void warningLine() {
    p.setRootRule(g.warningLine);

    assertThat(p, parse("#warning foo"));
  }

  @Test
  public void miscLine() {
    p.setRootRule(g.miscLine);

    assertThat(p, parse("#"));
    assertThat(p, parse("# lala"));
    assertThat(p, parse("#lala"));
  }

  @Test
  public void ifLine() {
    p.setRootRule(g.ifLine);

    g.constantExpression.mock();

    assertThat(p, parse("#if constantExpression"));
  }

  @Test
  public void elifLine() {
    p.setRootRule(g.elifLine);

    g.constantExpression.mock();

    assertThat(p, parse("#elif constantExpression"));
  }

  @Test
  public void ifLine_reallive() {
    p.setRootRule(g.ifLine);

    assertThat(p, parse("#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0"));
    assertThat(p, parse("#if 0   // Re-enable once PR13021 is fixed."));
    assertThat(p, parse("#if ((OSVER(NTDDI_VERSION) == NTDDI_WIN2K) && (1))"));
    
    assert (p.parse("#if A (4, 1)").findFirstChild(g.functionlikeMacro) != null);
    assert (p.parse("#if A ()").findFirstChild(g.functionlikeMacro) != null);
    assert (p.parse("#if A()").findFirstChild(g.functionlikeMacro) != null);

    assert (p.parse("#if defined(A)").findFirstChild(g.definedExpression) != null);
    assert (p.parse("#if defined (A)").findFirstChild(g.definedExpression) != null);
    assert (p.parse("#if defined A").findFirstChild(g.definedExpression) != null);
  }

  @Test
  public void constantExpression() {
    p.setRootRule(g.constantExpression);

    g.conditionalExpression.mock();

    assertThat(p, parse("conditionalExpression"));
  }

  @Test
  public void constantExpression_reallive() {
    p.setRootRule(g.constantExpression);

    assertThat(p, parse("(1 || 0) && (0 && 1)"));
    assertThat(p, parse("(1)"));
    assertThat(p, parse("( /**/ 1 /**/ )"));
    assertThat(p, parse("__has_feature(cxx_rvalue_references)"));
    assertThat(p, parse("__has_feature(/**/ cxx_rvalue_references /**/ )"));
    assertThat(p, parse("(C(A() && B()))"));
  }

  @Test
  public void conditionalExpression() {
    p.setRootRule(g.conditionalExpression);

    g.logicalOrExpression.mock();
    g.expression.mock();

    assertThat(p, parse("logicalOrExpression"));
    assertThat(p, parse("logicalOrExpression ? expression : logicalOrExpression"));
  }

  @Test
  public void logicalOrExpression() {
    p.setRootRule(g.logicalOrExpression);

    g.logicalAndExpression.mock();

    assertThat(p, parse("logicalAndExpression"));
    assertThat(p, parse("logicalAndExpression || logicalAndExpression"));
  }

  @Test
  public void logicalAndExpression() {
    p.setRootRule(g.logicalAndExpression);

    g.inclusiveOrExpression.mock();

    assertThat(p, parse("inclusiveOrExpression"));
    assertThat(p, parse("inclusiveOrExpression && inclusiveOrExpression"));
  }

  @Test
  public void logicalAndExpression_reallive() {
    p.setRootRule(g.logicalAndExpression);

    assertThat(p, parse("A() && B()"));
  }
  
  @Test
  public void inclusiveOrExpression() {
    p.setRootRule(g.inclusiveOrExpression);

    g.exclusiveOrExpression.mock();

    assertThat(p, parse("exclusiveOrExpression"));
    assertThat(p, parse("exclusiveOrExpression | exclusiveOrExpression"));
  }

  @Test
  public void exclusiveOrExpression() {
    p.setRootRule(g.exclusiveOrExpression);

    g.andExpression.mock();

    assertThat(p, parse("andExpression"));
    assertThat(p, parse("andExpression ^ andExpression"));
  }

  @Test
  public void andExpression() {
    p.setRootRule(g.andExpression);

    g.equalityExpression.mock();

    assertThat(p, parse("equalityExpression"));
    assertThat(p, parse("equalityExpression & equalityExpression"));
  }

  @Test
  public void equalityExpression() {
    p.setRootRule(g.equalityExpression);

    g.relationalExpression.mock();

    assertThat(p, parse("relationalExpression"));
    assertThat(p, parse("relationalExpression == relationalExpression"));
    assertThat(p, parse("relationalExpression != relationalExpression"));
  }

  @Test
  public void relationalExpression() {
    p.setRootRule(g.relationalExpression);

    g.shiftExpression.mock();

    assertThat(p, parse("shiftExpression"));
    assertThat(p, parse("shiftExpression < shiftExpression"));
    assertThat(p, parse("shiftExpression > shiftExpression"));
    assertThat(p, parse("shiftExpression <= shiftExpression"));
    assertThat(p, parse("shiftExpression >= shiftExpression"));
  }

  @Test
  public void shiftExpression() {
    p.setRootRule(g.shiftExpression);

    g.additiveExpression.mock();

    assertThat(p, parse("additiveExpression"));
    assertThat(p, parse("additiveExpression << additiveExpression"));
    assertThat(p, parse("additiveExpression >> additiveExpression"));
  }

  @Test
  public void additiveExpression() {
    p.setRootRule(g.additiveExpression);

    g.multiplicativeExpression.mock();

    assertThat(p, parse("multiplicativeExpression"));
    assertThat(p, parse("multiplicativeExpression + multiplicativeExpression"));
    assertThat(p, parse("multiplicativeExpression - multiplicativeExpression"));
  }

  @Test
  public void multiplicativeExpression() {
    p.setRootRule(g.multiplicativeExpression);

    g.unaryExpression.mock();

    assertThat(p, parse("unaryExpression"));
    assertThat(p, parse("unaryExpression * unaryExpression"));
    assertThat(p, parse("unaryExpression / unaryExpression"));
    assertThat(p, parse("unaryExpression % unaryExpression"));
  }

  @Test
  public void unaryExpression() {
    p.setRootRule(g.unaryExpression);

    g.multiplicativeExpression.mock();
    g.primaryExpression.mock();
    g.unaryOperator.mock();

    assertThat(p, parse("unaryOperator multiplicativeExpression"));
    assertThat(p, parse("primaryExpression"));
  }

  @Test
  public void primaryExpression() {
    p.setRootRule(g.primaryExpression);

    g.literal.mock();
    g.expression.mock();
    g.definedExpression.mock();

    assertThat(p, parse("literal"));
    assertThat(p, parse("( expression )"));
    assertThat(p, parse("definedExpression"));
    assertThat(p, parse("foo"));
  }

  @Test
  public void primaryExpression_reallive() {
    p.setRootRule(g.primaryExpression);
    
    assertThat(p, parse("(C(A() && B()))"));
  }
  
  @Test
  public void expression() {
    p.setRootRule(g.expression);

    g.conditionalExpression.mock();

    assertThat(p, parse("conditionalExpression"));
    assertThat(p, parse("conditionalExpression, conditionalExpression"));
  }

  @Test
  public void expression_reallive() {
    p.setRootRule(g.expression);

    assertThat(p, parse("C(A() && B())"));
  }
  
  @Test
  public void definedExpression() {
    p.setRootRule(g.definedExpression);

    assertThat(p, parse("defined LALA"));
    assertThat(p, parse("defined (LALA)"));
    assertThat(p, parse("defined(LALA)"));
  }

  @Test
  public void functionlikeMacro() {
    p.setRootRule(g.functionlikeMacro);

    g.argumentList.mock();

    assertThat(p, parse("__has_feature(argumentList)"));
  }

  @Test
  public void functionlikeMacro_reallife() {
    p.setRootRule(g.functionlikeMacro);

    assertThat(p, parse("__has_feature(cxx_rvalue)"));
    assertThat(p, parse("__has_feature(cxx_rvalue, bla)"));
    assertThat(p, parse("__GNUC_PREREQ (4, 1)"));
    assertThat(p, parse("A ()"));
    assertThat(p, parse("A()"));
    assertThat(p, parse("BOOST_WORKAROUND(BOOST_MSVC, < 1300)"));
    assertThat(p, parse("BOOST_WORKAROUND(< 1300)"));
    assertThat(p, parse("BOOST_WORKAROUND(a, call())"));
    assertThat(p, parse("C(A() && B())"));
  }
}
