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

public class TemplatesTest {

  Parser<CxxGrammar> p = CxxParser.create();
  CxxGrammar g = p.getGrammar();

  @Test
  public void template_declaration() {
    p.setRootRule(g.template_declaration);

    g.template_parameter_list.mock();
    g.declaration.mock();

    assertThat(p, parse("template < template_parameter_list > declaration"));
  }

  @Test
  public void template_declaration_realLife() {
    p.setRootRule(g.template_declaration);

    assertThat(p, parse("template <class T> ostream& operator<<();"));
    assertThat(p, parse("template <class T> ostream& operator<<(ostream& strm, const int& i);"));

    assertThat(p, parse("template <class T> ostream& operator<< (ostream& strm);"));
    assertThat(p, parse("template <class T> ostream& operator<< (const auto_ptr<T>& p);"));
    assertThat(p, parse("template <class T> ostream& operator<< (ostream& strm, const auto_ptr<T>& p);"));
  }

  @Test
  public void template_parameter_list() {
    p.setRootRule(g.template_parameter_list);

    g.template_parameter.mock();

    assertThat(p, parse("template_parameter"));
    assertThat(p, parse("template_parameter , template_parameter"));
  }

  @Test
  public void type_parameter() {
    p.setRootRule(g.type_parameter);

    g.type_id.mock();
    g.template_parameter_list.mock();
    g.id_expression.mock();

    assertThat(p, parse("class"));
    assertThat(p, parse("class T"));
    assertThat(p, parse("class ... foo"));

    assertThat(p, parse("class = type_id"));
    assertThat(p, parse("class foo = type_id"));

    assertThat(p, parse("typename"));
    assertThat(p, parse("typename ... foo"));

    assertThat(p, parse("typename = type_id"));
    assertThat(p, parse("typename foo = type_id"));

    assertThat(p, parse("template < template_parameter_list > class"));
    assertThat(p, parse("template < template_parameter_list > class ... foo"));

    assertThat(p, parse("template < template_parameter_list > class = id_expression"));
    assertThat(p, parse("template < template_parameter_list > class foo = id_expression"));
  }

  @Test
  public void simple_template_id_reallife() {
    p.setRootRule(g.simple_template_id);

    assertThat(p, parse("sometype<int>"));
  }

  @Test
  public void template_id() {
    p.setRootRule(g.template_id);

    g.simple_template_id.mock();
    g.operator_function_id.mock();
    g.template_argument_list.mock();
    g.literal_operator_id.mock();

    assertThat(p, parse("simple_template_id"));
    assertThat(p, parse("operator_function_id < >"));
    assertThat(p, parse("operator_function_id < template_argument_list >"));
    assertThat(p, parse("literal_operator_id < >"));
    assertThat(p, parse("literal_operator_id < template_argument_list >"));
  }

  @Test
  public void template_id_reallife() {
    p.setRootRule(g.template_id);
    assertThat(p, parse("foo<int>"));
  }

  @Test
  public void template_argument_list() {
    p.setRootRule(g.template_argument_list);

    g.template_argument.mock();

    assertThat(p, parse("template_argument"));
    assertThat(p, parse("template_argument ..."));
    assertThat(p, parse("template_argument , template_argument"));
    assertThat(p, parse("template_argument , template_argument ..."));
  }

  @Test
  public void typename_specifier() {
    p.setRootRule(g.typename_specifier);

    g.nested_name_specifier.mock();
    g.simple_template_id.mock();

    assertThat(p, parse("typename nested_name_specifier foo"));

    assertThat(p, parse("typename nested_name_specifier simple_template_id"));
    assertThat(p, parse("typename nested_name_specifier template simple_template_id"));
  }
}
