/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011-2016 SonarOpenCommunity
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

import static org.sonar.sslr.tests.Assertions.assertThat;

import org.junit.Test;

public class StatementTest extends ParserBaseTest {

  @Test
  public void statement() {
    p.setRootRule(g.rule(CxxGrammarImpl.statement));

    g.rule(CxxGrammarImpl.labeledStatement).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.expressionStatement).mock();
    g.rule(CxxGrammarImpl.compoundStatement).mock();
    g.rule(CxxGrammarImpl.selectionStatement).mock();
    g.rule(CxxGrammarImpl.iterationStatement).mock();
    g.rule(CxxGrammarImpl.jumpStatement).mock();
    g.rule(CxxGrammarImpl.declarationStatement).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.tryBlock).mock();

    assertThat(p).matches("labeledStatement");
    assertThat(p).matches("expressionStatement");
    assertThat(p).matches("attributeSpecifierSeq expressionStatement");
    assertThat(p).matches("attributeSpecifierSeq compoundStatement");
    assertThat(p).matches("attributeSpecifierSeq selectionStatement");
    assertThat(p).matches("attributeSpecifierSeq iterationStatement");
    assertThat(p).matches("attributeSpecifierSeq jumpStatement");
    assertThat(p).matches("declarationStatement");
    assertThat(p).matches("attributeSpecifierSeq tryBlock");
  }

  @Test
  public void statement_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.statement));

    // 'Arrow parameter after a cast' problem
    assertThat(p).matches("dynamic_cast<Type*>(myop)->op();");

    // 'Anonymous parameters' problem
    assertThat(p).matches("void foo(string, bool);");
    assertThat(p).matches("foo(int param, int=2);");

    // 'bracket operator isn't welcome here' problem
    assertThat(p).matches("foo(param1, instance()[1]);");

    // 'declaring friend a class in the global namespace' problem
    assertThat(p).matches("friend class ::SMLCGroupHierarchyImpl;");

    // "'bitwise not' applied to a mask inside a namespace" problem
    assertThat(p).matches("~CDB::mask;");

    // the 'default value for an anonymous parameter' problem
    assertThat(p).matches("CDBCheckResultItem(int a=1, CDB::CheckResultKind=0);");

    // the 'template class as friend' problem
    assertThat(p).matches("friend class SmartPtr<T>;");
  }

  @Test
  public void labeledStatement() {
    p.setRootRule(g.rule(CxxGrammarImpl.labeledStatement));

    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.statement).mock();
    g.rule(CxxGrammarImpl.constantExpression).mock();

    assertThat(p).matches("foo : statement");
    assertThat(p).matches("attributeSpecifierSeq foo : statement");
    assertThat(p).matches("case constantExpression : statement");
    assertThat(p).matches("attributeSpecifierSeq case constantExpression : statement");
    assertThat(p).matches("default : statement");
    assertThat(p).matches("attributeSpecifierSeq default : statement");

    // EXTENSION: gcc's case range
    assertThat(p).matches("case constantExpression ... constantExpression : statement");
    assertThat(p).matches("attributeSpecifierSeq case constantExpression ... constantExpression : statement");
  }

  @Test
  public void statementSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.statementSeq));

    g.rule(CxxGrammarImpl.statement).mock();

    assertThat(p).matches("statement");
    assertThat(p).matches("statement statement");
  }

  @Test
  public void selectionStatement() {
    p.setRootRule(g.rule(CxxGrammarImpl.selectionStatement));

    g.rule(CxxGrammarImpl.statement).mock();
    g.rule(CxxGrammarImpl.condition).mock();

    assertThat(p).matches("if ( condition ) statement");
    assertThat(p).matches("if ( condition ) statement else statement");
    assertThat(p).matches("switch ( condition ) statement");
  }
 
  @Test
  public void switchStatement_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.selectionStatement));

    assertThat(p).matches("switch (0) { default : break; }");
    assertThat(p).matches("switch (0) { {default : break;} }");
  }

  @Test
  public void ifStatement_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.selectionStatement));

    assertThat(p).matches("if (usedColors[(Color)c]) {}");
  }

  @Test
  public void condition() {
    p.setRootRule(g.rule(CxxGrammarImpl.condition));

    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.expression).mock();
    g.rule(CxxGrammarImpl.declarator).mock();
    g.rule(CxxGrammarImpl.conditionDeclSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.initializerClause).mock();
    g.rule(CxxGrammarImpl.bracedInitList).mock();

    assertThat(p).matches("expression");
    assertThat(p).matches("conditionDeclSpecifierSeq declarator = initializerClause");
    assertThat(p).matches("attributeSpecifierSeq conditionDeclSpecifierSeq declarator = initializerClause");
    assertThat(p).matches("conditionDeclSpecifierSeq declarator bracedInitList");
    assertThat(p).matches("attributeSpecifierSeq conditionDeclSpecifierSeq declarator bracedInitList");
  }

  @Test
  public void condition_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.condition));

    assertThat(p).matches("usedColors[(Color)c]");
    assertThat(p).matches("error_code ec = 1");
    assertThat(p).matches("a");
  }

  @Test
  public void iterationStatement() {
    p.setRootRule(g.rule(CxxGrammarImpl.iterationStatement));

    g.rule(CxxGrammarImpl.condition).mock();
    g.rule(CxxGrammarImpl.statement).mock();
    g.rule(CxxGrammarImpl.expression).mock();
    g.rule(CxxGrammarImpl.forInitStatement).mock();
    g.rule(CxxGrammarImpl.forRangeDeclaration).mock();
    g.rule(CxxGrammarImpl.forRangeInitializer).mock();

    assertThat(p).matches("while ( condition ) statement");
    assertThat(p).matches("do statement while ( expression ) ;");
    assertThat(p).matches("for ( forInitStatement ; ) statement");
    assertThat(p).matches("for ( forInitStatement condition ; ) statement");
    assertThat(p).matches("for ( forInitStatement condition ; expression ) statement");
    assertThat(p).matches("for ( forRangeDeclaration : forRangeInitializer ) statement");
  }

  @Test
  public void iterationStatement_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.iterationStatement));

    assertThat(p).matches("for (int i=1; i<=9; ++i) { coll.push_back(i); }");

    assertThat(p).matches("for (int i : v) { std::cout << i << ' '; }");
    assertThat(p).matches("for (std::string i : v) { std::cout << i.c_str() << ' '; }");
    assertThat(p).matches("for (auto i : v) { std::cout << i.c_str() << ' '; }");
    assertThat(p).matches("for (const int i : v) { std::cout << i << ' '; }");
    assertThat(p).matches("for (const std::string i : v) { std::cout << i.c_str() << ' '; }");
    assertThat(p).matches("for (const auto i : v) { std::cout << i.c_str() << ' '; }");

    assertThat(p).matches("for (int& i : v) { std::cout << i << ' '; }");
    assertThat(p).matches("for (std::string& i : v) { std::cout << i.c_str() << ' '; }");
    assertThat(p).matches("for (auto& i : v) { std::cout << i.c_str() << ' '; }");
    assertThat(p).matches("for (const int& i : v) { std::cout << i << ' '; }");
    assertThat(p).matches("for (const std::string& i : v) { std::cout << i.c_str() << ' '; }");
    assertThat(p).matches("for (const auto& i : v) { std::cout << i.c_str() << ' '; }");

    assertThat(p).matches("for (int&& i : v) { std::cout << i << ' '; }");
    assertThat(p).matches("for (std::string&& i : v) { std::cout << i.c_str() << ' '; }");
    assertThat(p).matches("for (auto&& i : v) { std::cout << i.c_str() << ' '; }");
    assertThat(p).matches("for (const int&& i : v) { std::cout << i << ' '; }");
    assertThat(p).matches("for (const std::string&& i : v) { std::cout << i.c_str() << ' '; }");
    assertThat(p).matches("for (const auto&& i : v) { std::cout << i.c_str() << ' '; }");

    assertThat(p).matches("for(int n : {0,1,2,3,4,5}) {std::cout << n << ' ';}");

    assertThat(p).matches("for (XMLFluidPlacementEntry* entry: m_pipeFluidPlacementEntries->entries) {delete entry;}");
    assertThat(p).matches("for (ICurveComparer* curveComparer : m_curveComparers) delete curveComparer;");

    // CLI extension
    assertThat(p).matches("for each(String^% s in arr) { s = i++.ToString(); }");
  }

  @Test
  public void forInitStatement_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.forInitStatement));
    assertThat(p).matches("int i=1;");
  }

  @Test
  public void forRangeDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.forRangeDeclaration));

    g.rule(CxxGrammarImpl.forRangeDeclSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.declarator).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();

    assertThat(p).matches("forRangeDeclSpecifierSeq declarator");
    assertThat(p).matches("attributeSpecifierSeq forRangeDeclSpecifierSeq declarator");
  }

  @Test
  public void forRangeInitializer() {
    p.setRootRule(g.rule(CxxGrammarImpl.forRangeInitializer));

    g.rule(CxxGrammarImpl.expression).mock();
    g.rule(CxxGrammarImpl.bracedInitList).mock();

    assertThat(p).matches("expression");
    assertThat(p).matches("bracedInitList");
  }

  @Test
  public void jumpStatement() {
    p.setRootRule(g.rule(CxxGrammarImpl.jumpStatement));

    g.rule(CxxGrammarImpl.expression).mock();
    g.rule(CxxGrammarImpl.bracedInitList).mock();

    assertThat(p).matches("break ;");
    assertThat(p).matches("continue ;");
    assertThat(p).matches("return expression ;");
    assertThat(p).matches("return bracedInitList ;");
    assertThat(p).matches("goto foo ;");
  }

  @Test
  public void jumpStatement_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.jumpStatement));

    assertThat(p).matches("return foo()->i;");
  }
}
