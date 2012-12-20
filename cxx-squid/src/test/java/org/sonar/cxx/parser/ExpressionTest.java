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

public class ExpressionTest {

  Parser<CxxGrammar> p = CxxParser.create();
  CxxGrammar g = p.getGrammar();

  @Test
  public void primary_expression() {
    p.setRootRule(g.primary_expression);

    g.literal.mock();
    g.expression.mock();
    g.id_expression.mock();
    g.lambda_expression.mock();

    assertThat(p, parse("literal"));
    assertThat(p, parse("this"));
    assertThat(p, parse("( expression )"));
    assertThat(p, parse("id_expression"));
    assertThat(p, parse("lambda_expression"));
  }

  @Test
  public void primary_expression_reallife() {
    p.setRootRule(g.primary_expression);

    assertThat(p, parse("(istream_iterator<string>(cin))"));
  }

  @Test
  public void id_expression_reallife() {
    p.setRootRule(g.id_expression);

    assertThat(p, parse("numeric_limits<char>::is_signed"));
    assertThat(p, parse("foo<int>"));
  }

  @Test
  public void unqualified_id() {
    p.setRootRule(g.unqualified_id);

    g.operator_function_id.mock();
    g.conversion_function_id.mock();
    g.literal_operator_id.mock();
    g.class_name.mock();
    g.decltype_specifier.mock();
    g.template_id.mock();

    assertThat(p, parse("foo"));
    assertThat(p, parse("operator_function_id"));
    assertThat(p, parse("conversion_function_id"));
    assertThat(p, parse("literal_operator_id"));
    assertThat(p, parse("~ class_name"));
    assertThat(p, parse("~ decltype_specifier"));
    assertThat(p, parse("template_id"));
  }

  @Test
  public void unqualified_id_reallife() {
    p.setRootRule(g.unqualified_id);
    assertThat(p, parse("foo<int>"));
  }
  
  @Test
  public void qualified_id() {
    p.setRootRule(g.qualified_id);

    g.literal.mock();
    g.nested_name_specifier.mock();
    g.unqualified_id.mock();
    g.operator_function_id.mock();
    g.literal_operator_id.mock();
    g.template_id.mock();

    assertThat(p, parse("nested_name_specifier unqualified_id"));
    assertThat(p, parse("nested_name_specifier template unqualified_id"));
    assertThat(p, parse(":: foo"));
    assertThat(p, parse(":: operator_function_id"));
    assertThat(p, parse(":: literal_operator_id"));
    assertThat(p, parse(":: template_id"));
  }

  @Test
  public void qualified_id_reallife() {
    p.setRootRule(g.qualified_id);

    assertThat(p, parse("numeric_limits<char>::is_signed"));
  }

  @Test
  public void nested_name_specifier() {
    p.setRootRule(g.nested_name_specifier);

    g.type_name.mock();
    g.namespace_name.mock();
    g.decltype_specifier.mock();
    g.simple_template_id.mock();

    assertThat(p, parse(":: type_name ::"));
    assertThat(p, parse("type_name ::"));
    assertThat(p, parse(":: namespace_name ::"));
    assertThat(p, parse("namespace_name ::"));
    assertThat(p, parse("decltype_specifier ::"));
    assertThat(p, parse("type_name :: foo ::"));
    assertThat(p, parse("namespace_name :: simple_template_id ::"));
  }

  @Test
  public void postfix_expression() {
    p.setRootRule(g.postfix_expression);

    g.primary_expression.mock();
    g.simple_type_specifier.mock();
    g.expression_list.mock();
    g.typename_specifier.mock();
    g.braced_init_list.mock();
    g.primary_expression.mock();
    g.expression.mock();
    g.id_expression.mock();
    g.type_id.mock();
    g.delete_expression.mock();
    g.pseudo_destructor_name.mock();

    assertThat(p, parse("primary_expression"));
    assertThat(p, parse("primary_expression [ expression ]"));
    assertThat(p, parse("primary_expression ( expression_list )"));
    assertThat(p, parse("simple_type_specifier ( expression_list )"));
    assertThat(p, parse("typename_specifier ( expression_list )"));
    assertThat(p, parse("simple_type_specifier braced_init_list"));
    assertThat(p, parse("typename_specifier braced_init_list"));
    assertThat(p, parse("primary_expression . template id_expression"));
    assertThat(p, parse("primary_expression -> template id_expression"));

    assertThat(p, parse("primary_expression . pseudo_destructor_name"));
    assertThat(p, parse("primary_expression -> pseudo_destructor_name"));

    assertThat(p, parse("primary_expression ++"));
    assertThat(p, parse("primary_expression --"));
    assertThat(p, parse("dynamic_cast < type_id > ( expression )"));
    assertThat(p, parse("static_cast < type_id > ( expression )"));
    assertThat(p, parse("reinterpret_cast < type_id > ( expression )"));
    assertThat(p, parse("const_cast < type_id > ( expression )"));
    assertThat(p, parse("typeid ( expression )"));
    assertThat(p, parse("typeid ( type_id )"));
  }

  @Test
  public void postfix_expression_reallife() {
    p.setRootRule(g.postfix_expression);

    assertThat(p, parse("usedColors[(Color)c]"));
    assertThat(p, parse("foo()->i"));
    assertThat(p, parse("dynamic_cast<Type*>(myop)->op()"));
    assertThat(p, parse("::foo()"));
    assertThat(p, parse("obj.foo<int>()"));
  }

  @Test
  public void expression_list_reallife() {
    p.setRootRule(g.expression_list);

    assertThat(p, parse("(istream_iterator<string>(cin)), istream_iterator<string>()"));
  }

  @Test
  public void pseudo_destructor_name() {
    p.setRootRule(g.pseudo_destructor_name);

    g.type_name.mock();
    g.nested_name_specifier.mock();
    g.simple_template_id.mock();
    g.decltype_specifier.mock();

    assertThat(p, parse("type_name :: ~ type_name"));
    assertThat(p, parse("nested_name_specifier type_name :: ~ type_name"));
    assertThat(p, parse("nested_name_specifier template simple_template_id :: ~ type_name"));
    assertThat(p, parse("~ type_name"));
    assertThat(p, parse("nested_name_specifier ~ type_name"));
    assertThat(p, parse("~ decltype_specifier"));
  }

  @Test
  public void unary_expression() {
    p.setRootRule(g.unary_expression);

    g.postfix_expression.mock();
    g.cast_expression.mock();
    g.unary_operator.mock();
    g.type_id.mock();
    g.noexcept_expression.mock();
    g.new_expression.mock();
    g.delete_expression.mock();

    assertThat(p, parse("postfix_expression"));
    assertThat(p, parse("sizeof postfix_expression"));
  }

  @Test
  public void unary_expression_reallife() {
    p.setRootRule(g.unary_expression);

    assertThat(p, parse("(istream_iterator<string>(cin))"));
    assertThat(p, parse("~CDB::mask"));
  }

  @Test
  public void new_expression() {
    p.setRootRule(g.new_expression);

    g.new_placement.mock();
    g.new_type_id.mock();
    g.new_initializer.mock();
    g.type_id.mock();

    assertThat(p, parse(":: new new_placement new_type_id new_initializer"));
    assertThat(p, parse(":: new new_placement ( type_id ) new_initializer"));
  }

  @Test
  public void new_expression_reallife() {
    p.setRootRule(g.new_expression);
    
    assertThat(p, parse("new Table()"));
    assertThat(p, parse("new Table"));
    assertThat(p, parse("new(Table)"));
  }
  
  @Test
  public void new_declarator() {
    p.setRootRule(g.new_declarator);

    g.ptr_operator.mock();
    g.noptr_new_declarator.mock();

    assertThat(p, parse("ptr_operator ptr_operator noptr_new_declarator"));
    assertThat(p, parse("ptr_operator ptr_operator"));
    assertThat(p, parse("ptr_operator"));
    assertThat(p, parse("ptr_operator noptr_new_declarator"));
    assertThat(p, parse("noptr_new_declarator"));
  }

  @Test
  public void noptr_new_declarator() {
    p.setRootRule(g.noptr_new_declarator);

    g.expression.mock();
    g.attribute_specifier_seq.mock();
    g.constant_expression.mock();

    assertThat(p, parse("[ expression ]"));
    assertThat(p, parse("[ expression ] attribute_specifier_seq"));
    assertThat(p, parse("[ expression ] attribute_specifier_seq [ constant_expression ]"));
    assertThat(p, parse("[ expression ] attribute_specifier_seq [ constant_expression ] attribute_specifier_seq"));
  }

  @Test
  public void new_initializer() {
    p.setRootRule(g.new_initializer);

    g.expression_list.mock();
    g.braced_init_list.mock();

    assertThat(p, parse("(  )"));
    assertThat(p, parse("( expression_list )"));
    assertThat(p, parse("braced_init_list"));
  }

  @Test
  public void delete_expression() {
    p.setRootRule(g.delete_expression);

    g.cast_expression.mock();

    assertThat(p, parse(":: delete cast_expression"));
    assertThat(p, parse(":: delete [ ] cast_expression"));
  }

  @Test
  public void expression() {
    p.setRootRule(g.expression);
    g.assignment_expression.mock();

    assertThat(p, parse("assignment_expression"));
    assertThat(p, parse("assignment_expression, assignment_expression"));
    assertThat(p, parse("assignment_expression, assignment_expression, assignment_expression"));
  }

  @Test
  public void expression_realLife() {
    p.setRootRule(g.expression);

    assertThat(p, parse("1 + 1"));
    assertThat(p, parse("(1 + 1) * 2"));
    assertThat("array subscript", p, parse("arr[i]"));
    assertThat(p, parse("( y > 4)"));
    assertThat(p, parse("( x== 8) && (c=='U')"));
    assertThat(p, parse("(a > b) ? a : b"));
    assertThat(p, parse("m = 1"));
    assertThat(p, parse("cout << endl"));
    assertThat(p, parse("numeric_limits<char>::is_signed"));
    assertThat(p, parse("cout << numeric_limits<char>::is_signed << endl"));
    assertThat(p, parse("usedColors[(Color)c]"));
    assertThat(p, parse("(Color)c"));
    assertThat(p, parse("foo()->i"));
    assertThat(p, parse("which ^= 1u"));
  }

  @Test
  public void assignment_expression() {
    p.setRootRule(g.assignment_expression);
    g.conditional_expression.mock();
    g.logical_or_expression.mock();
    g.assignment_operator.mock();
    g.initializer_clause.mock();
    g.throw_expression.mock();

    assertThat(p, parse("conditional_expression"));
    assertThat(p, parse("logical_or_expression assignment_operator initializer_clause"));
    assertThat(p, parse("throw_expression"));
  }

  @Test
  public void assignment_expression_realLife() {
    p.setRootRule(g.assignment_expression);

    assertThat(p, parse("i=0"));
    assertThat(p, parse("(istream_iterator<string>(cin))"));
    assertThat(p, parse("which ^= 1u"));
  }

  @Test
  public void logical_or_expression() {
    p.setRootRule(g.logical_or_expression);
    g.logical_and_expression.mock();

    assertThat(p, parse("logical_and_expression"));
    assertThat(p, parse("logical_and_expression || logical_and_expression"));
  }

  @Test
  public void logical_or_expression_reallife() {
    p.setRootRule(g.logical_or_expression);

    assertThat(p, parse("(istream_iterator<string>(cin))"));
  }

  @Test
  public void conditional_expression() {
    p.setRootRule(g.conditional_expression);

    g.logical_or_expression.mock();
    g.expression.mock();
    g.assignment_expression.mock();

    assertThat(p, parse("logical_or_expression"));
    assertThat(p, parse("logical_or_expression ? expression : assignment_expression"));
  }

  @Test
  public void logical_and_expression() {
    p.setRootRule(g.logical_and_expression);
    g.inclusive_or_expression.mock();

    assertThat(p, parse("inclusive_or_expression"));
    assertThat(p, parse("inclusive_or_expression && inclusive_or_expression"));
  }

  @Test
  public void inclusive_or_expression() {
    p.setRootRule(g.inclusive_or_expression);
    g.exclusive_or_expression.mock();

    assertThat(p, parse("exclusive_or_expression"));
    assertThat(p, parse("exclusive_or_expression | exclusive_or_expression"));
  }

  @Test
  public void exclusive_or_expression() {
    p.setRootRule(g.exclusive_or_expression);
    g.and_expression.mock();

    assertThat(p, parse("and_expression"));
    assertThat(p, parse("and_expression ^ and_expression"));
  }

  @Test
  public void and_expression() {
    p.setRootRule(g.and_expression);
    g.equality_expression.mock();

    assertThat(p, parse("equality_expression"));
    assertThat(p, parse("equality_expression & equality_expression"));
  }

  @Test
  public void equality_expression() {
    p.setRootRule(g.equality_expression);
    g.relational_expression.mock();

    assertThat(p, parse("relational_expression"));
    assertThat(p, parse("relational_expression == relational_expression"));
    assertThat(p, parse("relational_expression != relational_expression"));
  }

  @Test
  public void relational_expression() {
    p.setRootRule(g.relational_expression);
    g.shift_expression.mock();

    assertThat(p, parse("shift_expression"));
    assertThat(p, parse("shift_expression < shift_expression"));
    assertThat(p, parse("shift_expression > shift_expression"));
    assertThat(p, parse("shift_expression <= shift_expression"));
    assertThat(p, parse("shift_expression >= shift_expression"));
  }

  @Test
  public void shift_expression() {
    p.setRootRule(g.shift_expression);
    g.additive_expression.mock();

    assertThat(p, parse("additive_expression"));
    assertThat(p, parse("additive_expression << additive_expression"));
    assertThat(p, parse("additive_expression >> additive_expression"));
  }

  @Test
  public void additive_expression() {
    p.setRootRule(g.additive_expression);
    g.multiplicative_expression.mock();

    assertThat(p, parse("multiplicative_expression"));
    assertThat(p, parse("multiplicative_expression + multiplicative_expression"));
    assertThat(p, parse("multiplicative_expression - multiplicative_expression"));
  }

  @Test
  public void multiplicative_expression() {
    p.setRootRule(g.multiplicative_expression);
    g.pm_expression.mock();

    assertThat(p, parse("pm_expression"));
    assertThat(p, parse("pm_expression * pm_expression"));
    assertThat(p, parse("pm_expression / pm_expression"));
    assertThat(p, parse("pm_expression % pm_expression"));
  }

  @Test
  public void pm_expression() {
    p.setRootRule(g.pm_expression);
    g.cast_expression.mock();

    assertThat(p, parse("cast_expression"));
    assertThat(p, parse("cast_expression .* cast_expression"));
    assertThat(p, parse("cast_expression ->* cast_expression"));
  }

  @Test
  public void cast_expression() {
    p.setRootRule(g.pm_expression);
    g.unary_expression.mock();
    g.type_id.mock();

    assertThat(p, parse("unary_expression"));
    assertThat(p, parse("(type_id) unary_expression"));
    assertThat(p, parse("(type_id)(type_id) unary_expression"));
  }

  @Test
  public void cast_expression_reallife() {
    p.setRootRule(g.cast_expression);

    assertThat(p, parse("(istream_iterator<string>(cin))"));
    assertThat(p, parse("(Color)c"));
    assertThat(p, parse("CDB::mask"));
  }
}
