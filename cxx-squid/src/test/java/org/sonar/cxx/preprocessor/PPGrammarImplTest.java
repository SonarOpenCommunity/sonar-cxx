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
package org.sonar.cxx.preprocessor;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Parser;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import static org.sonar.cxx.sslr.tests.Assertions.assertThat;

class PPGrammarImplTest {

  private final Parser<Grammar> p = Parser.builder(PPGrammarImpl.create())
    .withLexer(PPLexer.create())
    .build();
  private final Grammar g = p.getGrammar();

  @Test
  void preprocessorLine() {
    mockRule(PPGrammarImpl.defineLine);
    mockRule(PPGrammarImpl.includeLine);
    mockRule(PPGrammarImpl.ifdefLine);
    mockRule(PPGrammarImpl.ifLine);
    mockRule(PPGrammarImpl.elifLine);
    mockRule(PPGrammarImpl.elseLine);
    mockRule(PPGrammarImpl.endifLine);
    mockRule(PPGrammarImpl.undefLine);
    mockRule(PPGrammarImpl.lineLine);
    mockRule(PPGrammarImpl.errorLine);
    mockRule(PPGrammarImpl.pragmaLine);
    mockRule(PPGrammarImpl.warningLine);

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
  void preprocessorLine_reallife() {
    assertThat(p).matches("#include      <ace/config-all.h>");
    assertThat(p).matches("#endif  // LLVM_DEBUGINFO_DWARFDEBUGRANGELIST_H");
    assertThat(p).matches(
      "#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0");
    assertThat(p).matches("#include <algorithm>");
    assertThat(p).matches("# /* See http://www.boost.org for most recent version. */");
    assertThat(p).matches("#if (C(A() && B()))");
  }

  @Test
  void defineLine_reallife() {
    p.setRootRule(g.rule(PPGrammarImpl.defineLine));

    assertThat(p).matches("#define ALGOSTUFF_HPPEOF");
    assertThat(p).matches("#define lala(a, b) a b");
    assertThat(p).matches("#define new dew_debug");
    assertThat(p).matches("#define macro(true, false) a");
    assertThat(p).matches("#define TRUE true");
    assertThat(p).matches("#define true TRUE");
    assertThat(p).matches("# define __glibcxx_assert(_Condition)");
  }

  @Test
  void define_containing_argumentList() {
    AstNode define = p.parse("#define lala(a, b) a b");
    org.assertj.core.api.Assertions.assertThat(define.getDescendants(PPGrammarImpl.parameterList)).isNotNull();
  }

  @Test
  void functionlikeMacroDefinition() {
    p.setRootRule(g.rule(PPGrammarImpl.functionlikeMacroDefinition));

    mockRule(PPGrammarImpl.replacementList);
    mockRule(PPGrammarImpl.argumentList);
    mockRule(PPGrammarImpl.ppToken);

    assertThat(p).matches("#define ppToken( argumentList ) replacementList");
    assertThat(p).matches("#define ppToken(argumentList) replacementList");
    assertThat(p).matches("#define ppToken( ... ) replacementList");
    assertThat(p).matches("#define ppToken(...) replacementList");
    assertThat(p).matches("#define ppToken( argumentList, ... ) replacementList");
    assertThat(p).matches("#define ppToken(argumentList, ...) replacementList");
  }

  @Test
  void functionlikeMacroDefinition_reallife() {
    p.setRootRule(g.rule(PPGrammarImpl.functionlikeMacroDefinition));

    assertThat(p).matches("#define foo() bar");
    assertThat(p).matches("#define foo() ()");
    assertThat(p).matches("#define foo(a) bar");
    assertThat(p).matches("#define foo(a,b) ab");
  }

  @Test
  void objectlikeMacroDefinition() {
    p.setRootRule(g.rule(PPGrammarImpl.objectlikeMacroDefinition));

    mockRule(PPGrammarImpl.replacementList);
    mockRule(PPGrammarImpl.ppToken);

    assertThat(p).matches("#define ppToken replacementList");
  }

  @Test
  void objectlikeMacroDefinition_reallife() {
    p.setRootRule(g.rule(PPGrammarImpl.objectlikeMacroDefinition));

    assertThat(p).matches("#define foo");
    assertThat(p).matches("#define foo bar");
    assertThat(p).matches("#define foo ()");
    assertThat(p).matches("#define new new_debug");
  }

  @Test
  void replacementList() {
    p.setRootRule(g.rule(PPGrammarImpl.replacementList));

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
  void argumentList() {
    p.setRootRule(g.rule(PPGrammarImpl.argumentList));

    assertThat(p).matches("foo");
    assertThat(p).matches("foo, bar");
    assertThat(p).matches("4, 1");
    assertThat(p).matches("4, call()");
    assertThat(p).matches("A() && B()");
  }

  @Test
  void argument() {
    p.setRootRule(g.rule(PPGrammarImpl.argument));

    assertThat(p).matches("a");
    assertThat(p).matches("call()");
    assertThat(p).matches("A() && B()");
  }

  @Test
  void somethingContainingParantheses() {
    p.setRootRule(g.rule(PPGrammarImpl.somethingContainingParantheses));

    assertThat(p).matches("call()");
    assertThat(p).matches("()");
  }

  @Test
  void somethingWithoutParantheses() {
    p.setRootRule(g.rule(PPGrammarImpl.somethingWithoutParantheses));

    assertThat(p).matches("abc");
  }

  @Test
  void ppToken() {
    p.setRootRule(g.rule(PPGrammarImpl.ppToken));

    assertThat(p).matches("foo");
    assertThat(p).matches("(");
    assertThat(p).matches(")");
    assertThat(p).matches("*");
  }

  @Test
  void includeLine() {
    p.setRootRule(g.rule(PPGrammarImpl.includeLine));

    mockRule(PPGrammarImpl.ppToken);

    assertThat(p).matches("#include <ppToken>");
    assertThat(p).matches("#include_next <ppToken>");
    assertThat(p).matches("#include ppToken");
    assertThat(p).matches("#include \"jabadu\"");
  }

  @Test
  void importLine_reallife() {
    p.setRootRule(g.rule(PPGrammarImpl.ppImport));

    assertThat(p).matches("import foo;");
    assertThat(p).matches("export import foo;");
    assertThat(p).matches("import foo.foo;");
    assertThat(p).matches("export import :foo;");

    assertThat(p).matches("import <file>");
    assertThat(p).matches("import \"jabadu\"");
  }

  @Test
  void moduleLine_reallife() {
    p.setRootRule(g.rule(PPGrammarImpl.ppModule));

    assertThat(p).matches("module;");
    assertThat(p).matches("module :private;");
    assertThat(p).matches("export module foo;");
    assertThat(p).matches("export module foo:foo;");
  }

  @Test
  void expandedIncludeBody() {
    p.setRootRule(g.rule(PPGrammarImpl.expandedIncludeBody));

    mockRule(PPGrammarImpl.ppToken);

    assertThat(p).matches("<ppToken>");
    assertThat(p).matches("\"jabadu\"");
  }

  @Test
  void includeLine_reallife() {
    p.setRootRule(g.rule(PPGrammarImpl.includeLine));

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
    assertThat(p).matches(
      "#include BOOST_PP_TUPLE_ELEM_2(0,10,<boost/utility/detail/result_of_iterate.hpp>,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
  }

  @Test
  void ifdefLine() {
    p.setRootRule(g.rule(PPGrammarImpl.ifdefLine));

    assertThat(p).matches("#ifdef foo");
    assertThat(p).matches("#ifndef foo");
    assertThat(p).matches("#ifdef __GNUC__ // aka CONST but following LLVM Conventions.");
    assertThat(p).matches("#ifdef /**/ lala /**/ ");
  }

  @Test
  void elifdefLine() {
    p.setRootRule(g.rule(PPGrammarImpl.elifdefLine));

    assertThat(p).matches("#elifdef foo");
    assertThat(p).matches("#elifndef foo");
    assertThat(p).matches("#elifdef __GNUC__ // aka CONST but following LLVM Conventions.");
    assertThat(p).matches("#elifdef /**/ lala /**/ ");
  }

  @Test
  void elseLine() {
    p.setRootRule(g.rule(PPGrammarImpl.elseLine));

    assertThat(p).matches("#else");
    assertThat(p).matches("#else  // if lala");
  }

  @Test
  void endifLine() {
    p.setRootRule(g.rule(PPGrammarImpl.endifLine));

    assertThat(p).matches("#endif");
    assertThat(p).matches("#endif  // LLVM_DEBUGINFO_DWARFDEBUGRANGELIST_H");
  }

  @Test
  void undefLine() {
    p.setRootRule(g.rule(PPGrammarImpl.undefLine));

    assertThat(p).matches("#undef foo");
  }

  @Test
  void lineLine() {
    p.setRootRule(g.rule(PPGrammarImpl.lineLine));

    assertThat(p).matches("#line foo bar");
  }

  @Test
  void errorLine() {
    p.setRootRule(g.rule(PPGrammarImpl.errorLine));

    assertThat(p).matches("#error foo");
    assertThat(p).matches("#error");
  }

  @Test
  void pragmaLine() {
    p.setRootRule(g.rule(PPGrammarImpl.pragmaLine));

    assertThat(p).matches("#pragma foo");
  }

  @Test
  void warningLine() {
    p.setRootRule(g.rule(PPGrammarImpl.warningLine));

    assertThat(p).matches("#warning foo");
  }

  @Test
  void miscLine() {
    p.setRootRule(g.rule(PPGrammarImpl.miscLine));

    assertThat(p).matches("#");
    assertThat(p).matches("# lala");
    assertThat(p).matches("#lala");
  }

  @Test
  void ifLine() {
    p.setRootRule(g.rule(PPGrammarImpl.ifLine));

    mockRule(PPGrammarImpl.constantExpression);

    assertThat(p).matches("#if constantExpression");
  }

  @Test
  void elifLine() {
    p.setRootRule(g.rule(PPGrammarImpl.elifLine));

    mockRule(PPGrammarImpl.constantExpression);

    assertThat(p).matches("#elif constantExpression");
  }

  @Test
  void ifLine_reallive() {
    p.setRootRule(g.rule(PPGrammarImpl.ifLine));

    assertThat(p).matches(
      "#if defined _FORTIFY_SOURCE && _FORTIFY_SOURCE > 0 && __GNUC_PREREQ (4, 1) && defined __OPTIMIZE__ && __OPTIMIZE__ > 0");
    assertThat(p).matches("#if 0   // Re-enable once PR13021 is fixed.");
    assertThat(p).matches("#if ((OSVER(NTDDI_VERSION) == NTDDI_WIN2K) && (1))");

    assert (p.parse("#if A (4, 1)").getFirstDescendant(PPGrammarImpl.functionlikeMacro) != null);
    assert (p.parse("#if A ()").getFirstDescendant(PPGrammarImpl.functionlikeMacro) != null);
    assert (p.parse("#if A()").getFirstDescendant(PPGrammarImpl.functionlikeMacro) != null);

    assert (p.parse("#if defined(A)").getFirstDescendant(PPGrammarImpl.definedExpression) != null);
    assert (p.parse("#if defined (A)").getFirstDescendant(PPGrammarImpl.definedExpression) != null);
    assert (p.parse("#if defined A").getFirstDescendant(PPGrammarImpl.definedExpression) != null);
  }

  @Test
  void constantExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.constantExpression));

    mockRule(PPGrammarImpl.conditionalExpression);

    assertThat(p).matches("conditionalExpression");
  }

  @Test
  void constantExpression_reallive() {
    p.setRootRule(g.rule(PPGrammarImpl.constantExpression));

    assertThat(p).matches("(1 || 0) && (0 && 1)");
    assertThat(p).matches("(1)");
    assertThat(p).matches("( /**/ 1 /**/ )");
    assertThat(p).matches("__has_feature(cxx_rvalue_references)");
    assertThat(p).matches("__has_feature(/**/ cxx_rvalue_references /**/ )");
    assertThat(p).matches("(C(A() && B()))");
  }

  @Test
  void conditionalExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.conditionalExpression));

    mockRule(PPGrammarImpl.logicalOrExpression);
    mockRule(PPGrammarImpl.expression);

    assertThat(p).matches("logicalOrExpression");
    assertThat(p).matches("logicalOrExpression ? expression : logicalOrExpression");
    assertThat(p).matches("logicalOrExpression ? : logicalOrExpression");
  }

  @Test
  void logicalOrExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.logicalOrExpression));

    mockRule(PPGrammarImpl.logicalAndExpression);

    assertThat(p).matches("logicalAndExpression");
    assertThat(p).matches("logicalAndExpression || logicalAndExpression");
  }

  @Test
  void logicalAndExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.logicalAndExpression));

    mockRule(PPGrammarImpl.inclusiveOrExpression);

    assertThat(p).matches("inclusiveOrExpression");
    assertThat(p).matches("inclusiveOrExpression && inclusiveOrExpression");
  }

  @Test
  void logicalAndExpression_reallive() {
    p.setRootRule(g.rule(PPGrammarImpl.logicalAndExpression));

    assertThat(p).matches("A() && B()");
  }

  @Test
  void inclusiveOrExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.inclusiveOrExpression));

    mockRule(PPGrammarImpl.exclusiveOrExpression);

    assertThat(p).matches("exclusiveOrExpression");
    assertThat(p).matches("exclusiveOrExpression | exclusiveOrExpression");
  }

  @Test
  void exclusiveOrExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.exclusiveOrExpression));

    mockRule(PPGrammarImpl.andExpression);

    assertThat(p).matches("andExpression");
    assertThat(p).matches("andExpression ^ andExpression");
  }

  @Test
  void andExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.andExpression));

    mockRule(PPGrammarImpl.equalityExpression);

    assertThat(p).matches("equalityExpression");
    assertThat(p).matches("equalityExpression & equalityExpression");
  }

  @Test
  void equalityExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.equalityExpression));

    mockRule(PPGrammarImpl.relationalExpression);

    assertThat(p).matches("relationalExpression");
    assertThat(p).matches("relationalExpression == relationalExpression");
    assertThat(p).matches("relationalExpression != relationalExpression");
  }

  @Test
  void relationalExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.relationalExpression));

    mockRule(PPGrammarImpl.shiftExpression);

    assertThat(p).matches("shiftExpression");
    assertThat(p).matches("shiftExpression < shiftExpression");
    assertThat(p).matches("shiftExpression > shiftExpression");
    assertThat(p).matches("shiftExpression <= shiftExpression");
    assertThat(p).matches("shiftExpression >= shiftExpression");
  }

  @Test
  void shiftExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.shiftExpression));

    mockRule(PPGrammarImpl.additiveExpression);

    assertThat(p).matches("additiveExpression");
    assertThat(p).matches("additiveExpression << additiveExpression");
    assertThat(p).matches("additiveExpression >> additiveExpression");
  }

  @Test
  void additiveExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.additiveExpression));

    mockRule(PPGrammarImpl.multiplicativeExpression);

    assertThat(p).matches("multiplicativeExpression");
    assertThat(p).matches("multiplicativeExpression + multiplicativeExpression");
    assertThat(p).matches("multiplicativeExpression - multiplicativeExpression");
  }

  @Test
  void multiplicativeExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.multiplicativeExpression));

    mockRule(PPGrammarImpl.unaryExpression);

    assertThat(p).matches("unaryExpression");
    assertThat(p).matches("unaryExpression * unaryExpression");
    assertThat(p).matches("unaryExpression / unaryExpression");
    assertThat(p).matches("unaryExpression % unaryExpression");
  }

  @Test
  void unaryExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.unaryExpression));

    mockRule(PPGrammarImpl.multiplicativeExpression);
    mockRule(PPGrammarImpl.primaryExpression);
    mockRule(PPGrammarImpl.unaryOperator);

    assertThat(p).matches("unaryOperator multiplicativeExpression");
    assertThat(p).matches("primaryExpression");
  }

  @Test
  void primaryExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.primaryExpression));

    mockRule(PPGrammarImpl.literal);
    mockRule(PPGrammarImpl.expression);
    mockRule(PPGrammarImpl.hasIncludeExpression);
    mockRule(PPGrammarImpl.definedExpression);

    assertThat(p).matches("literal");
    assertThat(p).matches("( expression )");
    assertThat(p).matches("hasIncludeExpression");
    assertThat(p).matches("definedExpression");
    assertThat(p).matches("foo");
  }

  @Test
  void primaryExpression_reallive() {
    p.setRootRule(g.rule(PPGrammarImpl.primaryExpression));

    assertThat(p).matches("(C(A() && B()))");
  }

  @Test
  void expression() {
    p.setRootRule(g.rule(PPGrammarImpl.expression));

    mockRule(PPGrammarImpl.conditionalExpression);

    assertThat(p).matches("conditionalExpression");
    assertThat(p).matches("conditionalExpression, conditionalExpression");
  }

  @Test
  void expression_reallive() {
    p.setRootRule(g.rule(PPGrammarImpl.expression));

    assertThat(p).matches("C(A() && B())");
  }

  @Test
  void definedExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.definedExpression));

    assertThat(p).matches("defined LALA");
    assertThat(p).matches("defined (LALA)");
    assertThat(p).matches("defined(LALA)");

    assertThat(p).matches("defined __has_include");
    assertThat(p).matches("defined (__has_include)");
  }

  @Test
  void functionlikeMacro() {
    p.setRootRule(g.rule(PPGrammarImpl.functionlikeMacro));

    mockRule(PPGrammarImpl.argumentList);

    assertThat(p).matches("__has_feature(argumentList)");
  }

  @Test
  void hasIncludeExpression() {
    p.setRootRule(g.rule(PPGrammarImpl.hasIncludeExpression));

    mockRule(PPGrammarImpl.includeBodyBracketed);
    mockRule(PPGrammarImpl.includeBodyQuoted);

    assertThat(p).matches("__has_include( includeBodyBracketed )");
    assertThat(p).matches("__has_include( includeBodyQuoted )");
  }

  @Test
  void hasIncludeExpression_reallife() {
    p.setRootRule(g.rule(PPGrammarImpl.hasIncludeExpression));

    assertThat(p).matches("__has_include( <optional> )");
    assertThat(p).matches("__has_include( \"optional.hpp\" )");
  }

  @Test
  void functionlikeMacro_reallife() {
    p.setRootRule(g.rule(PPGrammarImpl.functionlikeMacro));

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

  private void mockRule(GrammarRuleKey key) {
    g.rule(key).mock();
  }

}
