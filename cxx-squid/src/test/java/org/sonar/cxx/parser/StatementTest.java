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
import com.sonar.sslr.impl.events.ExtendedStackTrace;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class StatementTest {

  ExtendedStackTrace stackTrace = new ExtendedStackTrace();
  Parser<CxxGrammar> p = CxxParser.createDebugParser(mock(SquidAstVisitorContext.class), stackTrace);
  CxxGrammar g = p.getGrammar();

  @Test
  public void statement() {
    p.setRootRule(g.statement);

    g.labeledStatement.mock();
    g.expressionStatement.mock();
    g.compoundStatement.mock();
    g.selectionStatement.mock();
    g.iterationStatement.mock();
    g.jumpStatement.mock();
    g.declarationStatement.mock();
    g.attributeSpecifierSeq.mock();
    g.tryBlock.mock();

    assertThat(p, parse("labeledStatement"));
    assertThat(p, parse("expressionStatement"));
    assertThat(p, parse("attributeSpecifierSeq expressionStatement"));
    assertThat(p, parse("attributeSpecifierSeq compoundStatement"));
    assertThat(p, parse("attributeSpecifierSeq selectionStatement"));
    assertThat(p, parse("attributeSpecifierSeq iterationStatement"));
    assertThat(p, parse("attributeSpecifierSeq jumpStatement"));
    assertThat(p, parse("declarationStatement"));
    assertThat(p, parse("attributeSpecifierSeq tryBlock"));
  }

  @Test
  public void statement_reallife() {
    p.setRootRule(g.statement);

    // 'Arrow parameter after a cast' problem
    assertThat(p, parse("dynamic_cast<Type*>(myop)->op();"));

    // 'Anonymous parameters' problem
    assertThat(p, parse("void foo(string, bool);"));
    assertThat(p, parse("foo(int param, int=2);"));

    // 'bracket operator isnt welcome here' problem
    assertThat(p, parse("foo(param1, instance()[1]);"));

    // 'decraring friend a class in the global namespace' problem
    assertThat(p, parse("friend class ::SMLCGroupHierarchyImpl;"));

    // "'bitwise not' applied to a mask inside a namespace" problem
    assertThat(p, parse("~CDB::mask;"));

    // the 'default value for an anonymous parameter' problem
    assertThat(p, parse("CDBCheckResultItem(int a=1, CDB::CheckResultKind=0);"));

    // the 'template class as friend' problem
    assertThat(p, parse("friend class SmartPtr<T>;"));
  }

  @Test
  public void labeledStatement() {
    p.setRootRule(g.labeledStatement);

    g.attributeSpecifierSeq.mock();
    g.statement.mock();
    g.constantExpression.mock();

    assertThat(p, parse("foo : statement"));
    assertThat(p, parse("attributeSpecifierSeq foo : statement"));
    assertThat(p, parse("attributeSpecifierSeq case constantExpression : statement"));
    assertThat(p, parse("attributeSpecifierSeq default : statement"));
  }

  @Test
  public void statementSeq() {
    p.setRootRule(g.statementSeq);

    g.statement.mock();

    assertThat(p, parse("statement"));
    assertThat(p, parse("statement statement"));
  }

  @Test
  public void selectionStatement() {
    p.setRootRule(g.selectionStatement);

    g.statement.mock();
    g.condition.mock();

    assertThat(p, parse("if ( condition ) statement"));
    assertThat(p, parse("if ( condition ) statement else statement"));
    assertThat(p, parse("switch ( condition ) statement"));
  }

  @Test
  public void selectionStatement_reallife() {
    p.setRootRule(g.selectionStatement);

    assertThat(p, parse("if (usedColors[(Color)c]) {}"));
  }

  @Test
  public void condition() {
    p.setRootRule(g.condition);

    g.attributeSpecifierSeq.mock();
    g.expression.mock();
    g.declarator.mock();
    g.conditionDeclSpecifierSeq.mock();
    g.initializerClause.mock();
    g.bracedInitList.mock();

    assertThat(p, parse("expression"));
    assertThat(p, parse("conditionDeclSpecifierSeq declarator = initializerClause"));
    assertThat(p, parse("attributeSpecifierSeq conditionDeclSpecifierSeq declarator = initializerClause"));
    assertThat(p, parse("conditionDeclSpecifierSeq declarator bracedInitList"));
    assertThat(p, parse("attributeSpecifierSeq conditionDeclSpecifierSeq declarator bracedInitList"));
  }

  @Test
  public void condition_reallife() {
    p.setRootRule(g.condition);

    assertThat(p, parse("usedColors[(Color)c]"));
    assertThat(p, parse("error_code ec = 1"));
    assertThat(p, parse("a"));
  }

  @Test
  public void iterationStatement() {
    p.setRootRule(g.iterationStatement);

    g.condition.mock();
    g.statement.mock();
    g.expression.mock();
    g.forInitStatement.mock();
    g.forRangeDeclaration.mock();
    g.forRangeInitializer.mock();

    assertThat(p, parse("while ( condition ) statement"));
    assertThat(p, parse("do statement while ( expression ) ;"));
    assertThat(p, parse("for ( forInitStatement ; ) statement"));
    assertThat(p, parse("for ( forInitStatement condition ; ) statement"));
    assertThat(p, parse("for ( forInitStatement condition ; expression ) statement"));
    assertThat(p, parse("for ( forRangeDeclaration : forRangeInitializer ) statement"));
  }

  @Test
  public void iterationStatement_reallife() {
    p.setRootRule(g.iterationStatement);

    assertThat(p, parse("for (int i=1; i<=9; ++i) { coll.push_back(i); }"));
  }

  @Test
  public void forInitStatement_reallife() {
    p.setRootRule(g.forInitStatement);
    assertThat(p, parse("int i=1;"));
  }

  @Test
  public void forRangeDeclaration() {
    p.setRootRule(g.forRangeDeclaration);

    g.forrangeDeclSpecifierSeq.mock();
    g.declarator.mock();
    g.attributeSpecifierSeq.mock();

    assertThat(p, parse("forrangeDeclSpecifierSeq declarator"));
    assertThat(p, parse("attributeSpecifierSeq forrangeDeclSpecifierSeq declarator"));
  }

  @Test
  public void forRangeInitializer() {
    p.setRootRule(g.forRangeInitializer);

    g.expression.mock();
    g.bracedInitList.mock();

    assertThat(p, parse("expression"));
    assertThat(p, parse("bracedInitList"));
  }

  @Test
  public void jumpStatement() {
    p.setRootRule(g.jumpStatement);

    g.expression.mock();
    g.bracedInitList.mock();

    assertThat(p, parse("break ;"));
    assertThat(p, parse("continue ;"));
    assertThat(p, parse("return expression ;"));
    assertThat(p, parse("return bracedInitList ;"));
    assertThat(p, parse("goto foo ;"));
  }

  @Test
  public void jumpStatement_reallife() {
    p.setRootRule(g.jumpStatement);

    assertThat(p, parse("return foo()->i;"));
  }
}
