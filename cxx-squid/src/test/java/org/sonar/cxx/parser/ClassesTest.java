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

public class ClassesTest {

  Parser<CxxGrammar> p = CxxParser.create();
  CxxGrammar g = p.getGrammar();

  @Test
  public void class_name_realLife() {
    p.setRootRule(g.class_name);

    assertThat(p, parse("lala<int>"));
  }

  @Test
  public void class_specifier_realLife() {
    p.setRootRule(g.class_specifier);

    assertThat(p, parse("class foo final : bar { }"));
    assertThat(p, parse("class foo final : bar { ; }"));
    assertThat(p, parse("class foo final : bar { int foo(); }"));
  }

  @Test
  public void class_head() {
    p.setRootRule(g.class_head);

    g.class_key.mock();
    g.class_head_name.mock();
    g.attribute_specifier_seq.mock();
    g.base_clause.mock();
    g.class_virt_specifier.mock();

    assertThat(p, parse("class_key class_head_name"));
    assertThat(p, parse("class_key attribute_specifier_seq class_head_name"));
    assertThat(p, parse("class_key attribute_specifier_seq class_head_name class_virt_specifier"));
    assertThat(p, parse("class_key attribute_specifier_seq class_head_name class_virt_specifier base_clause"));

    assertThat(p, parse("class_key"));
    assertThat(p, parse("class_key attribute_specifier_seq"));
    assertThat(p, parse("class_key attribute_specifier_seq base_clause"));
  }

  @Test
  public void class_head_name() {
    p.setRootRule(g.class_head_name);

    g.nested_name_specifier.mock();
    g.class_name.mock();

    assertThat(p, parse("class_name"));
    assertThat(p, parse("nested_name_specifier class_name"));
  }

  @Test
  public void member_specification() {
    p.setRootRule(g.member_specification);

    g.member_declaration.mock();
    g.access_specifier.mock();

    assertThat(p, parse("member_declaration"));
    assertThat(p, parse("member_declaration access_specifier :"));

    assertThat(p, parse("access_specifier :"));
    assertThat(p, parse("access_specifier : member_declaration"));
  }

  @Test
  public void member_specification_realLife() {
    p.setRootRule(g.member_specification);

    assertThat(p, parse("int foo();"));
    assertThat(p, parse("protected:"));
    assertThat(p, parse("Result (*ptr)();"));
    assertThat(p, parse("protected: Result (*ptr)();"));
  }

  @Test
  public void member_declaration() {
    p.setRootRule(g.member_declaration);

    g.attribute_specifier_seq.mock();
    g.decl_specifier_seq.mock();
    g.member_declarator_list.mock();
    g.function_definition.mock();
    g.nested_name_specifier.mock();
    g.unqualified_id.mock();
    g.using_declaration.mock();
    g.static_assert_declaration.mock();
    g.template_declaration.mock();
    g.alias_declaration.mock();

    assertThat(p, parse(";"));
    assertThat(p, parse("attribute_specifier_seq decl_specifier_seq member_declarator_list ;"));

    assertThat(p, parse("function_definition"));
    assertThat(p, parse("function_definition ;"));

    assertThat(p, parse("nested_name_specifier unqualified_id ;"));
    assertThat(p, parse(":: nested_name_specifier template unqualified_id ;"));

    assertThat(p, parse("using_declaration"));
    assertThat(p, parse("static_assert_declaration"));
    assertThat(p, parse("template_declaration"));
    assertThat(p, parse("alias_declaration"));
  }

  @Test
  public void member_declaration_realLife() {
    p.setRootRule(g.member_declaration);

    assertThat(p, parse("int foo();"));
    assertThat(p, parse("int foo(){}"));

    assertThat(p, parse("char tword[20];"));
    assertThat(p, parse("int count;"));
    assertThat(p, parse("tnode *left;"));
    assertThat(p, parse("tnode *right;"));
    assertThat(p, parse("Result (*ptr)();"));
  }

  @Test
  public void member_declarator_list() {
    p.setRootRule(g.member_declarator_list);

    g.member_declarator.mock();

    assertThat(p, parse("member_declarator"));
    assertThat(p, parse("member_declarator , member_declarator"));
  }

  @Test
  public void member_declarator_list_realLife() {
    p.setRootRule(g.member_declarator_list);

    assertThat(p, parse("tword[20]"));
  }

  @Test
  public void member_declarator() {
    p.setRootRule(g.member_declarator);

    g.declarator.mock();
    g.pure_specifier.mock();
    g.brace_or_equal_initializer.mock();
    g.constant_expression.mock();
    g.attribute_specifier_seq.mock();
    g.virt_specifier_seq.mock();

    assertThat(p, parse("declarator"));
    assertThat(p, parse("declarator virt_specifier_seq"));
    assertThat(p, parse("declarator virt_specifier_seq pure_specifier"));

    assertThat(p, parse("declarator brace_or_equal_initializer"));

    assertThat(p, parse(": constant_expression"));
    assertThat(p, parse("foo : constant_expression"));
    assertThat(p, parse("foo attribute_specifier_seq : constant_expression"));
  }

  @Test
  public void member_declarator_realLife() {
    p.setRootRule(g.member_declarator);

    assertThat(p, parse("tword[20]"));
  }

  @Test
  public void virt_specifier_seq() {
    p.setRootRule(g.virt_specifier_seq);

    g.virt_specifier.mock();

    assertThat(p, parse("virt_specifier"));
    assertThat(p, parse("virt_specifier virt_specifier"));
  }

  @Test
  public void base_specifier_list() {
    p.setRootRule(g.base_specifier_list);

    g.base_specifier.mock();

    assertThat(p, parse("base_specifier"));
    assertThat(p, parse("base_specifier ..."));

    assertThat(p, parse("base_specifier , base_specifier"));
    assertThat(p, parse("base_specifier , base_specifier ..."));
    assertThat(p, parse("base_specifier ..., base_specifier ..."));
  }

  @Test
  public void base_specifier() {
    p.setRootRule(g.base_specifier);

    g.base_type_specifier.mock();
    g.attribute_specifier_seq.mock();
    g.access_specifier.mock();

    assertThat(p, parse("base_type_specifier"));
    assertThat(p, parse("attribute_specifier_seq base_type_specifier"));

    assertThat(p, parse("virtual base_type_specifier"));
    assertThat(p, parse("attribute_specifier_seq virtual access_specifier base_type_specifier"));

    assertThat(p, parse("access_specifier base_type_specifier"));
    assertThat(p, parse("attribute_specifier_seq access_specifier virtual base_type_specifier"));
  }

  @Test
  public void class_or_decltype() {
    p.setRootRule(g.class_or_decltype);

    g.class_name.mock();
    g.nested_name_specifier.mock();
    g.decltype_specifier.mock();

    assertThat(p, parse("class_name"));
    assertThat(p, parse("nested_name_specifier class_name"));
    assertThat(p, parse("decltype_specifier"));
  }
}
