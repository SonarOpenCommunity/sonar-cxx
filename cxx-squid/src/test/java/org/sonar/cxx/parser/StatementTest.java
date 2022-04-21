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

import org.junit.jupiter.api.Test;

public class StatementTest extends ParserBaseTestHelper {

  @Test
  public void statement() {
    setRootRule(CxxGrammarImpl.statement);

    mockRule(CxxGrammarImpl.labeledStatement);
    mockRule(CxxGrammarImpl.expressionStatement);
    mockRule(CxxGrammarImpl.compoundStatement);
    mockRule(CxxGrammarImpl.selectionStatement);
    mockRule(CxxGrammarImpl.iterationStatement);
    mockRule(CxxGrammarImpl.jumpStatement);
    mockRule(CxxGrammarImpl.declarationStatement);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.tryBlock);

    assertThatParser()
      .matches("labeledStatement")
      .matches("expressionStatement")
      .matches("attributeSpecifierSeq expressionStatement")
      .matches("attributeSpecifierSeq compoundStatement")
      .matches("attributeSpecifierSeq selectionStatement")
      .matches("attributeSpecifierSeq iterationStatement")
      .matches("attributeSpecifierSeq jumpStatement")
      .matches("declarationStatement")
      .matches("attributeSpecifierSeq tryBlock");
  }

  @Test
  public void statement_reallife() {
    setRootRule(CxxGrammarImpl.statement);

    assertThatParser()
      // 'Arrow parameter after a cast' problem
      .matches("dynamic_cast<Type*>(myop)->op();")
      // 'Anonymous parameters' problem
      .matches("void foo(string, bool);")
      .matches("foo(int param, int=2);")
      // 'bracket operator isn't welcome here' problem
      .matches("foo(param1, instance()[1]);")
      // 'declaring friend a class in the global namespace' problem
      .matches("friend class ::SMLCGroupHierarchyImpl;")
      // "'bitwise not' applied to a mask inside a namespace" problem
      .matches("~CDB::mask;")
      // the 'default value for an anonymous parameter' problem
      .matches("CDBCheckResultItem(int a=1, CDB::CheckResultKind=0);")
      // the 'template class as friend' problem
      .matches("friend class SmartPtr<T>;");
  }

  @Test
  public void expressionStatement_reallife() {
    setRootRule(CxxGrammarImpl.expressionStatement);

    assertThatParser()
      // fix #2286: use expressionStatement with ; at the end to reset RightAngleBracketsChannel after each matches
      .matches("a() < 0 || c >= 1;")
      .matches("a() < 0 || c > 1;")
      .matches("a() < 0 || c >> 1;");
  }

  @Test
  public void selectionStatement_reallife() {
    setRootRule(CxxGrammarImpl.selectionStatement);

    assertThatParser()
      // fix #2286
      .matches("if(a() < 0 || c >= 1) {}")
      .matches("if(a() < 0 || c > 1) {}")
      .matches("if(a() < 0 || c >> 1) {}");
  }

  @Test
  public void labeledStatement() {
    setRootRule(CxxGrammarImpl.labeledStatement);

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.statement);
    mockRule(CxxGrammarImpl.constantExpression);

    assertThatParser()
      .matches("foo : statement")
      .matches("attributeSpecifierSeq foo : statement")
      .matches("case constantExpression : statement")
      .matches("attributeSpecifierSeq case constantExpression : statement")
      .matches("default : statement")
      .matches("attributeSpecifierSeq default : statement")
      // EXTENSION: gcc's case range
      .matches("case constantExpression ... constantExpression : statement")
      .matches("attributeSpecifierSeq case constantExpression ... constantExpression : statement");
  }

  @Test
  public void statementSeq() {
    setRootRule(CxxGrammarImpl.statementSeq);

    mockRule(CxxGrammarImpl.statement);

    assertThatParser()
      .matches("statement")
      .matches("statement statement");
  }

  @Test
  public void selectionStatement() {
    setRootRule(CxxGrammarImpl.selectionStatement);

    mockRule(CxxGrammarImpl.statement);
    mockRule(CxxGrammarImpl.condition);
    mockRule(CxxGrammarImpl.initStatement);

    assertThatParser()
      .matches("if ( condition ) statement")
      .matches("if constexpr ( condition ) statement")
      .matches("if ( initStatement condition ) statement")
      .matches("if constexpr ( initStatement condition ) statement")
      .matches("if ( condition ) statement else statement")
      .matches("if constexpr ( condition ) statement else statement")
      .matches("if ( initStatement condition ) statement else statement")
      .matches("if constexpr ( initStatement condition ) statement else statement")
      .matches("switch ( condition ) statement")
      .matches("switch ( initStatement condition ) statement");
  }

  @Test
  public void switchStatement_reallife() {
    setRootRule(CxxGrammarImpl.selectionStatement);

    assertThatParser()
      .matches("switch (0) { default : break; }")
      .matches("switch (0) { {default : break;} }")
      .matches("switch (0) { {case 0: default : break;} }");
  }

  @Test
  public void ifStatement_reallife() {
    setRootRule(CxxGrammarImpl.selectionStatement);

    assertThatParser()
      .matches("if (usedColors[(Color)c]) {}");
  }

  @Test
  public void condition() {
    setRootRule(CxxGrammarImpl.condition);

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.conditionDeclSpecifierSeq);
    mockRule(CxxGrammarImpl.initializerClause);
    mockRule(CxxGrammarImpl.bracedInitList);

    assertThatParser()
      .matches("expression")
      .matches("conditionDeclSpecifierSeq declarator = initializerClause")
      .matches("attributeSpecifierSeq conditionDeclSpecifierSeq declarator = initializerClause")
      .matches("conditionDeclSpecifierSeq declarator bracedInitList")
      .matches("attributeSpecifierSeq conditionDeclSpecifierSeq declarator bracedInitList");
  }

  @Test
  public void condition_reallife() {
    setRootRule(CxxGrammarImpl.condition);

    assertThatParser()
      .matches("usedColors[(Color)c]")
      .matches("error_code ec = 1")
      .matches("a");
  }

  @Test
  public void iterationStatement() {
    setRootRule(CxxGrammarImpl.iterationStatement);

    mockRule(CxxGrammarImpl.condition);
    mockRule(CxxGrammarImpl.statement);
    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.initStatement);
    mockRule(CxxGrammarImpl.forRangeDeclaration);
    mockRule(CxxGrammarImpl.forRangeInitializer);

    assertThatParser()
      .matches("while ( condition ) statement")
      .matches("do statement while ( expression ) ;")
      .matches("for ( initStatement ; ) statement")
      .matches("for ( initStatement condition ; ) statement")
      .matches("for ( initStatement condition ; expression ) statement")
      .matches("for ( forRangeDeclaration : forRangeInitializer ) statement")
      .matches("for ( initStatement forRangeDeclaration : forRangeInitializer ) statement");
  }

  @Test
  public void iterationStatement_reallife() {
    setRootRule(CxxGrammarImpl.iterationStatement);

    assertThatParser()
      .matches("for (int i=1; i<=9; ++i) { coll.push_back(i); }")
      .matches("for (int i : v) { std::cout << i << ' '; }")
      .matches("for (std::string i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (auto i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (const int i : v) { std::cout << i << ' '; }")
      .matches("for (const std::string i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (const auto i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (int& i : v) { std::cout << i << ' '; }")
      .matches("for (std::string& i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (auto& i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (const int& i : v) { std::cout << i << ' '; }")
      .matches("for (const std::string& i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (const auto& i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (int&& i : v) { std::cout << i << ' '; }")
      .matches("for (std::string&& i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (auto&& i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (const int&& i : v) { std::cout << i << ' '; }")
      .matches("for (const std::string&& i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for (const auto&& i : v) { std::cout << i.c_str() << ' '; }")
      .matches("for(int n : {0,1,2,3,4,5}) {std::cout << n << ' ';}")
      .matches("for (XMLFluidPlacementEntry* entry: m_pipeFluidPlacementEntries->entries) {delete entry;}")
      .matches("for (ICurveComparer* curveComparer : m_curveComparers) delete curveComparer;")
      // CLI extension
      .matches("for each(String^% s in arr) { s = i++.ToString(); }")
      // C++17 structered bindings
      .matches("for (const auto&[key, val] : mymap) { std::cout << key << \": \" << val << std::endl; }");
  }

  @Test
  public void forInitStatement_reallife() {
    setRootRule(CxxGrammarImpl.initStatement);

    assertThatParser()
      .matches("int i=1;");
  }

  @Test
  public void forRangeDeclaration() {
    setRootRule(CxxGrammarImpl.forRangeDeclaration);

    mockRule(CxxGrammarImpl.forRangeDeclSpecifierSeq);
    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.declSpecifierSeq);
    mockRule(CxxGrammarImpl.identifierList);
    mockRule(CxxGrammarImpl.refQualifier);

    assertThatParser()
      .matches("forRangeDeclSpecifierSeq declarator")
      .matches("attributeSpecifierSeq forRangeDeclSpecifierSeq declarator")
      .matches("declSpecifierSeq [ identifierList ]")
      .matches("attributeSpecifierSeq declSpecifierSeq [ identifierList ]")
      .matches("attributeSpecifierSeq declSpecifierSeq [ identifierList ]")
      .matches("declSpecifierSeq refQualifier [ identifierList ]")
      .matches("attributeSpecifierSeq declSpecifierSeq refQualifier [ identifierList ]");
  }

  @Test
  public void forRangeInitializer() {
    setRootRule(CxxGrammarImpl.forRangeInitializer);

    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.bracedInitList);

    assertThatParser()
      .matches("expression")
      .matches("bracedInitList");
  }

  @Test
  public void jumpStatement() {
    setRootRule(CxxGrammarImpl.jumpStatement);

    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.bracedInitList);
    mockRule(CxxGrammarImpl.coroutineReturnStatement);

    assertThatParser()
      .matches("break ;")
      .matches("continue ;")
      .matches("return expression ;")
      .matches("return bracedInitList ;")
      .matches("coroutineReturnStatement")
      .matches("goto foo ;");
  }

  @Test
  public void jumpStatement_reallife() {
    setRootRule(CxxGrammarImpl.jumpStatement);

    assertThatParser()
      .matches("return foo()->i;");
  }

  @Test
  public void coroutineReturnStatement() {
    setRootRule(CxxGrammarImpl.jumpStatement);
    mockRule(CxxGrammarImpl.exprOrBracedInitList);

    assertThatParser()
      .matches("co_return ;")
      .matches("co_return exprOrBracedInitList ;");
  }

}
