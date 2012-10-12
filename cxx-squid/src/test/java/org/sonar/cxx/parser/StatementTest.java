/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
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
import org.junit.Test;
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;

public class StatementTest {

  Parser<CxxGrammar> p = CxxParser.create();
  CxxGrammar g = p.getGrammar();

  @Test
  public void statement() {
    p.setRootRule(g.statement);

    g.labeled_statement.mock();
    g.expression_statement.mock();
    g.compound_statement.mock();
    g.selection_statement.mock();
    g.iteration_statement.mock();
    g.jump_statement.mock();
    g.declaration_statement.mock();
    g.attribute_specifier_seq.mock();
    g.try_block.mock();

    assertThat(p, parse("labeled_statement"));
    assertThat(p, parse("expression_statement"));
    assertThat(p, parse("attribute_specifier_seq expression_statement"));
    assertThat(p, parse("attribute_specifier_seq compound_statement"));
    assertThat(p, parse("attribute_specifier_seq selection_statement"));
    assertThat(p, parse("attribute_specifier_seq iteration_statement"));
    assertThat(p, parse("attribute_specifier_seq jump_statement"));
    assertThat(p, parse("declaration_statement"));
    assertThat(p, parse("attribute_specifier_seq try_block"));
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
    // assertThat(p, parse("friend class SmartPtr<T>;"));
  }

  @Test
  public void labeled_statement() {
    p.setRootRule(g.labeled_statement);

    g.attribute_specifier_seq.mock();
    g.statement.mock();
    g.constant_expression.mock();

    assertThat(p, parse("foo : statement"));
    assertThat(p, parse("attribute_specifier_seq foo : statement"));
    assertThat(p, parse("attribute_specifier_seq case constant_expression : statement"));
    assertThat(p, parse("attribute_specifier_seq default : statement"));
  }

  @Test
  public void statement_seq() {
    p.setRootRule(g.statement_seq);

    g.statement.mock();

    assertThat(p, parse("statement"));
    assertThat(p, parse("statement statement"));
  }

  @Test
  public void selection_statement() {
    p.setRootRule(g.selection_statement);

    g.statement.mock();
    g.condition.mock();

    assertThat(p, parse("if ( condition ) statement"));
    assertThat(p, parse("if ( condition ) statement else statement"));
    assertThat(p, parse("switch ( condition ) statement"));
  }

  @Test
  public void selection_statement_reallife() {
    p.setRootRule(g.selection_statement);

    assertThat(p, parse("if (usedColors[(Color)c]) {}"));
  }

  @Test
  public void condition() {
    p.setRootRule(g.condition);

    g.attribute_specifier_seq.mock();
    g.expression.mock();
    g.declarator.mock();
    g.decl_specifier_seq.mock();
    g.initializer_clause.mock();
    g.braced_init_list.mock();

    assertThat(p, parse("expression"));
    assertThat(p, parse("decl_specifier_seq declarator = initializer_clause"));
    assertThat(p, parse("attribute_specifier_seq decl_specifier_seq declarator = initializer_clause"));
    assertThat(p, parse("decl_specifier_seq declarator braced_init_list"));
    assertThat(p, parse("attribute_specifier_seq decl_specifier_seq declarator braced_init_list"));
  }

  @Test
  public void condition_reallife() {
    p.setRootRule(g.condition);

    assertThat(p, parse("usedColors[(Color)c]"));
  }

  @Test
  public void iteration_statement() {
    p.setRootRule(g.iteration_statement);

    g.condition.mock();
    g.statement.mock();
    g.expression.mock();
    g.for_init_statement.mock();
    g.for_range_declaration.mock();
    g.for_range_initializer.mock();

    assertThat(p, parse("while ( condition ) statement"));
    assertThat(p, parse("do statement while ( expression ) ;"));
    assertThat(p, parse("for ( for_init_statement ; ) statement"));
    assertThat(p, parse("for ( for_init_statement condition ; ) statement"));
    assertThat(p, parse("for ( for_init_statement condition ; expression ) statement"));
    assertThat(p, parse("for ( for_range_declaration : for_range_initializer ) statement"));
  }

  @Test
  public void iteration_statement_reallife() {
    p.setRootRule(g.iteration_statement);

    assertThat(p, parse("for (int i=1; i<=9; ++i) { coll.push_back(i); }"));
  }

  @Test
  public void for_init_statement_reallife() {
    p.setRootRule(g.for_init_statement);
    assertThat(p, parse("int i=1;"));
  }

  @Test
  public void for_range_declaration() {
    p.setRootRule(g.for_range_declaration);

    g.decl_specifier_seq.mock();
    g.declarator.mock();
    g.attribute_specifier_seq.mock();

    assertThat(p, parse("decl_specifier_seq declarator"));
    assertThat(p, parse("attribute_specifier_seq decl_specifier_seq declarator"));
  }

  @Test
  public void for_range_initializer() {
    p.setRootRule(g.for_range_initializer);

    g.expression.mock();
    g.braced_init_list.mock();

    assertThat(p, parse("expression"));
    assertThat(p, parse("braced_init_list"));
  }

  @Test
  public void jump_statement() {
    p.setRootRule(g.jump_statement);

    g.expression.mock();
    g.braced_init_list.mock();

    assertThat(p, parse("break ;"));
    assertThat(p, parse("continue ;"));
    assertThat(p, parse("return expression ;"));
    assertThat(p, parse("return braced_init_list ;"));
    assertThat(p, parse("goto foo ;"));
  }

  @Test
  public void jump_statement_reallife() {
    p.setRootRule(g.jump_statement);

    assertThat(p, parse("return foo()->i;"));
  }
}
