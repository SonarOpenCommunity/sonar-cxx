/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.impl.Parser;
import org.junit.Test;
import org.sonar.sslr.grammar.GrammarRuleKey;
import static org.sonar.sslr.tests.Assertions.assertThat;

public class CppGrammarTest {

  private final Parser<Grammar> p = Parser.builder(CppGrammar.create())
    .withLexer(CppLexer.create())
    .build();
  private final Grammar g = p.getGrammar();

  private void mockRule(GrammarRuleKey key) {
    g.rule(key).mock(); //@todo deprecated mock
  }

  @Test
  public void preprocessorLine() {
    mockRule(CppGrammar.defineLine);
    mockRule(CppGrammar.includeLine);
    mockRule(CppGrammar.ifdefLine);
    mockRule(CppGrammar.ifLine);
    mockRule(CppGrammar.elifLine);
    mockRule(CppGrammar.elseLine);
    mockRule(CppGrammar.endifLine);
    mockRule(CppGrammar.undefLine);
    mockRule(CppGrammar.lineLine);
    mockRule(CppGrammar.errorLine);
    mockRule(CppGrammar.pragmaLine);
    mockRule(CppGrammar.warningLine);

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
    org.assertj.core.api.Assertions.assertThat(define.getDescendants(CppGrammar.parameterList)).isNotNull();
  }

  @Test
  public void functionlikeMacroDefinition() {
    p.setRootRule(g.rule(CppGrammar.functionlikeMacroDefinition));

    mockRule(CppGrammar.replacementList);
    mockRule(CppGrammar.argumentList);
    mockRule(CppGrammar.ppToken);

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

    mockRule(CppGrammar.replacementList);
    mockRule(CppGrammar.ppToken);

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
    assertThat(p).matches("L#ppToken");
    assertThat(p).matches("u8#ppToken");
    assertThat(p).matches("u#ppToken");
    assertThat(p).matches("U#ppToken");
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

    mockRule(CppGrammar.ppToken);

    assertThat(p).matches("#include <ppToken>");
    assertThat(p).matches("#include_next <ppToken>");
    assertThat(p).matches("#include ppToken");
    assertThat(p).matches("#include \"jabadu\"");
  }

  @Test
  public void expandedIncludeBody() {
    p.setRootRule(g.rule(CppGrammar.expandedIncludeBody));

    mockRule(CppGrammar.ppToken);

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
    assertThat(p).matches("#include macro(a,b,c)");
    assertThat(p).matches("#include BOOST_PP_STRINGIZE(boost/mpl/aux_/preprocessed/AUX778076_PREPROCESSED_HEADER)");
    assertThat(p).matches("#include BOOST_PP_TUPLE_ELEM_2(0,10,<boost/utility/detail/result_of_iterate.hpp>,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
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

    mockRule(CppGrammar.constantExpression);

    assertThat(p).matches("#if constantExpression");
  }

  @Test
  public void elifLine() {
    p.setRootRule(g.rule(CppGrammar.elifLine));

    mockRule(CppGrammar.constantExpression);

    assertThat(p).matches("#elif constantExpression");
  }

  @Test
  public void ifLine_reallive() {
    p.setRootRule(g.rule(CppGrammar.ifLine));

    assertThat(p).matches("#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0");
    assertThat(p).matches("#if 0   // Re-enable once PR13021 is fixed.");
    assertThat(p).matches("#if ((OSVER(NTDDI_VERSION) == NTDDI_WIN2K) && (1))");

    assert (p.parse("#if A (4, 1)").findFirstChild(CppGrammar.functionlikeMacro) != null); //@todo deprecated findFirstChild
    assert (p.parse("#if A ()").findFirstChild(CppGrammar.functionlikeMacro) != null); //@todo deprecated findFirstChild
    assert (p.parse("#if A()").findFirstChild(CppGrammar.functionlikeMacro) != null); //@todo deprecated findFirstChild

    assert (p.parse("#if defined(A)").findFirstChild(CppGrammar.definedExpression) != null); //@todo deprecated findFirstChild
    assert (p.parse("#if defined (A)").findFirstChild(CppGrammar.definedExpression) != null); //@todo deprecated findFirstChild
    assert (p.parse("#if defined A").findFirstChild(CppGrammar.definedExpression) != null); //@todo deprecated findFirstChild
  }

  @Test
  public void constantExpression() {
    p.setRootRule(g.rule(CppGrammar.constantExpression));

    mockRule(CppGrammar.conditionalExpression);

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

    mockRule(CppGrammar.logicalOrExpression);
    mockRule(CppGrammar.expression);

    assertThat(p).matches("logicalOrExpression");
    assertThat(p).matches("logicalOrExpression ? expression : logicalOrExpression");
    assertThat(p).matches("logicalOrExpression ? : logicalOrExpression");
  }

  @Test
  public void logicalOrExpression() {
    p.setRootRule(g.rule(CppGrammar.logicalOrExpression));

    mockRule(CppGrammar.logicalAndExpression);

    assertThat(p).matches("logicalAndExpression");
    assertThat(p).matches("logicalAndExpression || logicalAndExpression");
  }

  @Test
  public void logicalAndExpression() {
    p.setRootRule(g.rule(CppGrammar.logicalAndExpression));

    mockRule(CppGrammar.inclusiveOrExpression);

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

    mockRule(CppGrammar.exclusiveOrExpression);

    assertThat(p).matches("exclusiveOrExpression");
    assertThat(p).matches("exclusiveOrExpression | exclusiveOrExpression");
  }

  @Test
  public void exclusiveOrExpression() {
    p.setRootRule(g.rule(CppGrammar.exclusiveOrExpression));

    mockRule(CppGrammar.andExpression);

    assertThat(p).matches("andExpression");
    assertThat(p).matches("andExpression ^ andExpression");
  }

  @Test
  public void andExpression() {
    p.setRootRule(g.rule(CppGrammar.andExpression));

    mockRule(CppGrammar.equalityExpression);

    assertThat(p).matches("equalityExpression");
    assertThat(p).matches("equalityExpression & equalityExpression");
  }

  @Test
  public void equalityExpression() {
    p.setRootRule(g.rule(CppGrammar.equalityExpression));

    mockRule(CppGrammar.relationalExpression);

    assertThat(p).matches("relationalExpression");
    assertThat(p).matches("relationalExpression == relationalExpression");
    assertThat(p).matches("relationalExpression != relationalExpression");
  }

  @Test
  public void relationalExpression() {
    p.setRootRule(g.rule(CppGrammar.relationalExpression));

    mockRule(CppGrammar.shiftExpression);

    assertThat(p).matches("shiftExpression");
    assertThat(p).matches("shiftExpression < shiftExpression");
    assertThat(p).matches("shiftExpression > shiftExpression");
    assertThat(p).matches("shiftExpression <= shiftExpression");
    assertThat(p).matches("shiftExpression >= shiftExpression");
  }

  @Test
  public void shiftExpression() {
    p.setRootRule(g.rule(CppGrammar.shiftExpression));

    mockRule(CppGrammar.additiveExpression);

    assertThat(p).matches("additiveExpression");
    assertThat(p).matches("additiveExpression << additiveExpression");
    assertThat(p).matches("additiveExpression >> additiveExpression");
  }

  @Test
  public void additiveExpression() {
    p.setRootRule(g.rule(CppGrammar.additiveExpression));

    mockRule(CppGrammar.multiplicativeExpression);

    assertThat(p).matches("multiplicativeExpression");
    assertThat(p).matches("multiplicativeExpression + multiplicativeExpression");
    assertThat(p).matches("multiplicativeExpression - multiplicativeExpression");
  }

  @Test
  public void multiplicativeExpression() {
    p.setRootRule(g.rule(CppGrammar.multiplicativeExpression));

    mockRule(CppGrammar.unaryExpression);

    assertThat(p).matches("unaryExpression");
    assertThat(p).matches("unaryExpression * unaryExpression");
    assertThat(p).matches("unaryExpression / unaryExpression");
    assertThat(p).matches("unaryExpression % unaryExpression");
  }

  @Test
  public void unaryExpression() {
    p.setRootRule(g.rule(CppGrammar.unaryExpression));

    mockRule(CppGrammar.multiplicativeExpression);
    mockRule(CppGrammar.primaryExpression);
    mockRule(CppGrammar.unaryOperator);

    assertThat(p).matches("unaryOperator multiplicativeExpression");
    assertThat(p).matches("primaryExpression");
  }

  @Test
  public void primaryExpression() {
    p.setRootRule(g.rule(CppGrammar.primaryExpression));

    mockRule(CppGrammar.literal);
    mockRule(CppGrammar.expression);
    mockRule(CppGrammar.hasIncludeExpression);
    mockRule(CppGrammar.definedExpression);

    assertThat(p).matches("literal");
    assertThat(p).matches("( expression )");
    assertThat(p).matches("hasIncludeExpression");
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

    mockRule(CppGrammar.conditionalExpression);

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

    assertThat(p).matches("defined __has_include");
    assertThat(p).matches("defined (__has_include)");
  }

  @Test
  public void functionlikeMacro() {
    p.setRootRule(g.rule(CppGrammar.functionlikeMacro));

    mockRule(CppGrammar.argumentList);

    assertThat(p).matches("__has_feature(argumentList)");
  }

  @Test
  public void hasIncludeExpression() {
    p.setRootRule(g.rule(CppGrammar.hasIncludeExpression));

    mockRule(CppGrammar.includeBodyBracketed);
    mockRule(CppGrammar.includeBodyQuoted);

    assertThat(p).matches("__has_include( includeBodyBracketed )");
    assertThat(p).matches("__has_include( includeBodyQuoted )");
  }

  @Test
  public void hasIncludeExpression_reallife() {
    p.setRootRule(g.rule(CppGrammar.hasIncludeExpression));

    assertThat(p).matches("__has_include( <optional> )");
    assertThat(p).matches("__has_include( \"optional.hpp\" )");
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
