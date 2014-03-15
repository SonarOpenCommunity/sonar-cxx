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

import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import com.sonar.sslr.api.Grammar;

import static org.sonar.sslr.tests.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class StatementTest {
  Parser<Grammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  Grammar g = p.getGrammar();

  @Test
  public void statement() {
    p.setRootRule(g.rule(CxxGrammarImpl.statement));

    g.rule(CxxGrammarImpl.labeledStatement).mock();
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

    // 'bracket operator isnt welcome here' problem
    assertThat(p).matches("foo(param1, instance()[1]);");

    // 'decraring friend a class in the global namespace' problem
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
    assertThat(p).matches("attributeSpecifierSeq case constantExpression : statement");
    assertThat(p).matches("attributeSpecifierSeq default : statement");
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
  public void selectionStatement_reallife() {
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
    g.rule(CxxGrammarImpl.forRangeDeclSpecs).mock();
    g.rule(CxxGrammarImpl.declarator).mock();
    g.rule(CxxGrammarImpl.forRangeInitializer).mock();

    assertThat(p).matches("while ( condition ) statement");
    assertThat(p).matches("do statement while ( expression ) ;");
    assertThat(p).matches("for ( forInitStatement ; ) statement");
    assertThat(p).matches("for ( forInitStatement condition ; ) statement");
    assertThat(p).matches("for ( forInitStatement condition ; expression ) statement");
    assertThat(p).matches("for ( forRangeDeclSpecs declarator : forRangeInitializer ) statement");
  }

  @Test
  public void iterationStatement_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.iterationStatement));

    assertThat(p).matches("for (int i=1; i<=9; ++i) { coll.push_back(i); }");
    assertThat(p).matches("for (long long item : testVector) {;}");
    assertThat(p).matches("for (UDrivetrainObject* DrivetrainObject : DrivetrainObjects) {;}");
    assertThat(p).matches("for (TPair<int32, FDrivetrainObjectConnection> const& Conn : Connections) {;}");
  }

  @Test
  public void forInitStatement_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.forInitStatement));
    assertThat(p).matches("int i=99;");
    System.out.println(p.toString());
  }

  @Test
  public void forRangeDeclSpecs() {
    p.setRootRule(g.rule(CxxGrammarImpl.forRangeDeclSpecs));

    g.rule(CxxGrammarImpl.forrangeDeclSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();

    assertThat(p).matches("forrangeDeclSpecifierSeq");
    assertThat(p).matches("attributeSpecifierSeq forrangeDeclSpecifierSeq");
  }

  @Test
  public void forRangeDeclSpecs_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.forRangeDeclSpecs));
    assertThat(p).matches("long long");
  }

  @Test
  public void declarator_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.declarator));
    assertThat(p).matches("item");
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
  public void forRangeInitializer_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.forRangeInitializer));
    assertThat(p).matches("testVector");
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
