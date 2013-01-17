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
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class DeclaratorsTest {

  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();

  @Test
  public void init_declarator_list() {
    p.setRootRule(g.init_declarator_list);

    g.init_declarator.mock();

    assertThat(p, parse("init_declarator"));
    assertThat(p, parse("init_declarator , init_declarator"));
  }

  @Test
  public void init_declarator_list_reallife() {
    p.setRootRule(g.init_declarator_list);

    assertThat(p, parse("a"));
    assertThat(p, parse("foo(string, bool)"));
  }

  @Test
  public void init_declarator_reallife() {
    p.setRootRule(g.init_declarator);

    assertThat(p, parse("coll((istream_iterator<string>(cin)), istream_iterator<string>())"));
    assertThat(p, parse("a"));
  }

  @Test
  public void declarator() {
    p.setRootRule(g.declarator);

    g.ptr_declarator.mock();
    g.noptr_declarator.mock();
    g.parameters_and_qualifiers.mock();
    g.trailing_return_type.mock();

    assertThat(p, parse("ptr_declarator"));
    assertThat(p, parse("noptr_declarator parameters_and_qualifiers trailing_return_type"));
  }

  @Test
  public void declarator_realLife() {
    p.setRootRule(g.declarator);

    assertThat(p, parse("a"));
    assertThat(p, parse("foo()"));
    assertThat(p, parse("max(int a, int b, int c)"));
    assertThat(p, parse("tword[20]"));
    assertThat(p, parse("*what() throw()"));
    assertThat(p, parse("foo(string, bool)"));
  }

  @Test
  public void noptr_declarator() {
    p.setRootRule(g.noptr_declarator);

    g.declarator_id.mock();
    g.attribute_specifier_seq.mock();
    g.parameters_and_qualifiers.mock();
    g.constant_expression.mock();
    g.ptr_declarator.mock();

    assertThat(p, parse("declarator_id"));
    assertThat(p, parse("declarator_id attribute_specifier_seq"));
    assertThat(p, parse("declarator_id parameters_and_qualifiers"));
    assertThat(p, parse("declarator_id [ ]"));
    assertThat(p, parse("declarator_id [ constant_expression ]"));
    assertThat(p, parse("declarator_id [ ] attribute_specifier_seq"));
    assertThat(p, parse("declarator_id [ constant_expression ] attribute_specifier_seq"));
    assertThat(p, parse("declarator_id [ ] attribute_specifier_seq"));
    assertThat(p, parse("( ptr_declarator )"));
  }

  @Test
  public void noptr_declarator_reallife() {
    p.setRootRule(g.noptr_declarator);

    assertThat(p, parse("coll"));
  }

  @Test
  public void parameters_and_qualifiers() {
    p.setRootRule(g.parameters_and_qualifiers);

    g.parameter_declaration_clause.mock();
    g.attribute_specifier_seq.mock();
    g.cv_qualifier_seq.mock();
    g.ref_qualifier.mock();
    g.exception_specification.mock();

    assertThat(p, parse("( parameter_declaration_clause )"));
    assertThat(p, parse("( parameter_declaration_clause ) attribute_specifier_seq"));
    assertThat(p, parse("( parameter_declaration_clause ) attribute_specifier_seq cv_qualifier_seq"));
    assertThat(p, parse("( parameter_declaration_clause ) attribute_specifier_seq cv_qualifier_seq ref_qualifier"));
    assertThat(p, parse("( parameter_declaration_clause ) attribute_specifier_seq cv_qualifier_seq ref_qualifier exception_specification"));
  }

  @Test
  public void parameters_and_qualifiers_realLife() {
    p.setRootRule(g.parameters_and_qualifiers);

    assertThat(p, parse("(ostream& strm, const int& i)"));
    assertThat(p, parse("(string, bool)"));
  }

  @Test
  public void ptr_declarator() {
    p.setRootRule(g.ptr_declarator);

    g.noptr_declarator.mock();
    g.ptr_operator.mock();

    assertThat(p, parse("noptr_declarator"));
    assertThat(p, parse("ptr_operator noptr_declarator"));
    assertThat(p, parse("ptr_operator ptr_operator noptr_declarator"));
    
  }

  @Test
  public void ptr_declarator_reallife() {
    p.setRootRule(g.ptr_declarator);

    assertThat(p, parse("A::*foo"));
  }
  
  @Test
  public void ptr_operator() {
    p.setRootRule(g.ptr_operator);

    g.attribute_specifier_seq.mock();
    g.cv_qualifier_seq.mock();
    g.nested_name_specifier.mock();

    assertThat(p, parse("*"));
    assertThat(p, parse("* attribute_specifier_seq"));
    assertThat(p, parse("* attribute_specifier_seq cv_qualifier_seq"));
    assertThat(p, parse("&"));
    assertThat(p, parse("& attribute_specifier_seq"));
    assertThat(p, parse("&&"));
    assertThat(p, parse("&& attribute_specifier_seq"));
    assertThat(p, parse("nested_name_specifier *"));
    assertThat(p, parse("nested_name_specifier * cv_qualifier_seq"));
    assertThat(p, parse("nested_name_specifier * attribute_specifier_seq cv_qualifier_seq"));
  }

  @Test
  public void ptr_operator_reallife() {
    p.setRootRule(g.ptr_operator);
    
    assertThat(p, parse("A::*"));
  }
  
  @Test
  public void cv_qualifier_seq() {
    p.setRootRule(g.cv_qualifier_seq);

    g.cv_qualifier.mock();

    assertThat(p, parse("cv_qualifier"));
    assertThat(p, parse("cv_qualifier cv_qualifier"));
  }

  @Test
  public void declarator_id() {
    p.setRootRule(g.declarator_id);

    g.id_expression.mock();
    g.nested_name_specifier.mock();
    g.class_name.mock();

    assertThat(p, parse("id_expression"));
    assertThat(p, parse("... id_expression"));
    assertThat(p, parse("class_name"));
    assertThat(p, parse("nested_name_specifier class_name"));
  }

  @Test
  public void declarator_id_reallife() {
    p.setRootRule(g.declarator_id);

    assertThat(p, parse("lala<int>"));
  }

  @Test
  public void type_id() {
    p.setRootRule(g.type_id);

    assertThat(p, parse("int"));
    assertThat(p, parse("int *"));
    assertThat(p, parse("int *[3]"));
    assertThat(p, parse("int (*)[3]"));
    assertThat(p, parse("int *()"));
    assertThat(p, parse("int (*)(double)"));
  }

  @Test
  public void abstract_declarator() {
    p.setRootRule(g.abstract_declarator);

    g.ptr_abstract_declarator.mock();
    g.noptr_abstract_declarator.mock();
    g.parameters_and_qualifiers.mock();
    g.trailing_return_type.mock();
    g.abstract_pack_declarator.mock();

    assertThat(p, parse("ptr_abstract_declarator"));
    assertThat(p, parse("parameters_and_qualifiers trailing_return_type"));
    assertThat(p, parse("noptr_abstract_declarator parameters_and_qualifiers trailing_return_type"));
    assertThat(p, parse("abstract_pack_declarator"));
  }

  @Test
  public void ptr_abstract_declarator() {
    p.setRootRule(g.ptr_abstract_declarator);

    g.noptr_abstract_declarator.mock();
    g.ptr_operator.mock();

    assertThat(p, parse("ptr_operator"));
    assertThat(p, parse("ptr_operator ptr_operator"));
    assertThat(p, parse("ptr_operator noptr_abstract_declarator"));
    assertThat(p, parse("ptr_operator ptr_operator noptr_abstract_declarator"));
    assertThat(p, parse("noptr_abstract_declarator"));
  }

  @Test
  public void noptr_abstract_declarator() {
    p.setRootRule(g.noptr_abstract_declarator);

    g.parameters_and_qualifiers.mock();
    g.constant_expression.mock();
    g.attribute_specifier_seq.mock();
    g.ptr_abstract_declarator.mock();

    assertThat(p, parse("parameters_and_qualifiers"));
    assertThat(p, parse("( ptr_abstract_declarator ) parameters_and_qualifiers"));

    assertThat(p, parse("[ ]"));
    assertThat(p, parse("[ constant_expression ]"));
    assertThat(p, parse("[ constant_expression ] attribute_specifier_seq"));
    assertThat(p, parse("( ptr_abstract_declarator ) [ constant_expression ] attribute_specifier_seq"));

    assertThat(p, parse("( ptr_abstract_declarator )"));
  }

  @Test
  public void abstract_pack_declarator() {
    p.setRootRule(g.abstract_pack_declarator);

    g.noptr_abstract_pack_declarator.mock();
    g.ptr_operator.mock();

    assertThat(p, parse("noptr_abstract_pack_declarator"));
    assertThat(p, parse("ptr_operator noptr_abstract_pack_declarator"));
    assertThat(p, parse("ptr_operator ptr_operator noptr_abstract_pack_declarator"));
  }

  @Test
  public void noptr_abstract_pack_declarator() {
    p.setRootRule(g.noptr_abstract_pack_declarator);

    g.parameters_and_qualifiers.mock();
    g.constant_expression.mock();
    g.attribute_specifier_seq.mock();

    assertThat(p, parse("..."));
    assertThat(p, parse("... parameters_and_qualifiers"));
    assertThat(p, parse("... [ ] "));
    assertThat(p, parse("... [ constant_expression ] "));
    assertThat(p, parse("... [ constant_expression ] attribute_specifier_seq"));
  }

  @Test
  public void parameter_declaration_list() {
    p.setRootRule(g.parameter_declaration_list);

    g.parameter_declaration.mock();

    assertThat(p, parse("parameter_declaration"));
    assertThat(p, parse("parameter_declaration , parameter_declaration"));
  }

  @Test
  public void parameter_declaration_list_realLife() {
    p.setRootRule(g.parameter_declaration_list);

    assertThat(p, parse("ostream& strm, const int& i"));
    assertThat(p, parse("string, bool"));
  }

  @Test
  public void parameter_declaration_clause() {
    p.setRootRule(g.parameter_declaration_clause);

    g.parameter_declaration_list.mock();

    assertThat(p, parse(""));
    assertThat(p, parse("parameter_declaration_list"));
    assertThat(p, parse("..."));
    assertThat(p, parse("parameter_declaration_list ..."));
    assertThat(p, parse("parameter_declaration_list , ..."));
  }

  @Test
  public void parameter_declaration_clause_realLife() {
    p.setRootRule(g.parameter_declaration_clause);

    assertThat(p, parse("ostream& strm, const int& i"));
    assertThat(p, parse("string, bool"));
  }

  @Test
  public void parameter_declaration() {
    p.setRootRule(g.parameter_declaration);

    g.attribute_specifier_seq.mock();
    g.parameter_decl_specifier_seq.mock();
    g.declarator.mock();
    g.initializer_clause.mock();
    g.abstract_declarator.mock();

    assertThat(p, parse("parameter_decl_specifier_seq declarator"));
    assertThat(p, parse("attribute_specifier_seq parameter_decl_specifier_seq declarator"));

    assertThat(p, parse("parameter_decl_specifier_seq declarator = initializer_clause"));
    assertThat(p, parse("attribute_specifier_seq parameter_decl_specifier_seq declarator = initializer_clause"));

    assertThat(p, parse("parameter_decl_specifier_seq"));
    assertThat(p, parse("parameter_decl_specifier_seq abstract_declarator"));
    assertThat(p, parse("attribute_specifier_seq parameter_decl_specifier_seq abstract_declarator"));

    assertThat(p, parse("parameter_decl_specifier_seq = initializer_clause"));
    assertThat(p, parse("parameter_decl_specifier_seq abstract_declarator = initializer_clause"));
    assertThat(p, parse("attribute_specifier_seq parameter_decl_specifier_seq abstract_declarator = initializer_clause"));
  }

  @Test
  public void parameter_declaration_realLife() {
    p.setRootRule(g.parameter_declaration);

    assertThat(p, parse("ostream& strm"));
    assertThat(p, parse("const int& i"));
    assertThat(p, parse("const paramtype<T> param"));
    assertThat(p, parse("const auto_ptr<T>& p"));
    assertThat(p, parse("string"));
    assertThat(p, parse("::P& c"));
  }

  @Test
  public void function_definition() {
    p.setRootRule(g.function_definition);

    g.attribute_specifier_seq.mock();
    g.function_decl_specifier_seq.mock();
    g.declarator.mock();
    g.virt_specifier_seq.mock();
    g.function_body.mock();

    assertThat(p, parse("declarator function_body"));
    assertThat(p, parse("attribute_specifier_seq declarator function_body"));
    assertThat(p, parse("attribute_specifier_seq function_decl_specifier_seq declarator function_body"));
    assertThat(p, parse("attribute_specifier_seq function_decl_specifier_seq declarator virt_specifier_seq function_body"));
  }

  @Test
  public void function_definition_realLife() {
    p.setRootRule(g.function_definition);

    assertThat(p, parse("int foo(){}"));
    assertThat(p, parse("int A::foo(){}"));
    assertThat(p, parse("static int foo(){}"));
    assertThat(p, parse("main(){}"));
    assertThat(p, parse("int max(int a, int b, int c) { int m = (a > b) ? a : b; return (m > c) ? m : c; }"));
    assertThat(p, parse("AddValue (const T& v) : theValue(v) {}"));
    assertThat(p, parse("void operator[] () {}"));
    assertThat(p, parse("void operator() (T& elem) const {elem += theValue;}"));
    assertThat(p, parse("int main(){}"));
    assertThat(p, parse("virtual const char* what() const throw() { return \"read empty stack\"; }"));
    assertThat(p, parse("void foo() override {}"));
    assertThat(p, parse("void foo(::P& c) {}"));
  }

  @Test
  public void function_body() {
    p.setRootRule(g.function_body);

    g.compound_statement.mock();
    g.ctor_initializer.mock();
    g.function_try_block.mock();

    assertThat(p, parse("compound_statement"));
    assertThat(p, parse("ctor_initializer compound_statement"));

    assertThat(p, parse("function_try_block"));
    assertThat(p, parse("= default ;"));
    assertThat(p, parse("= delete ;"));
  }

  @Test
  public void function_body_realLife() {
    p.setRootRule(g.function_body);

    assertThat(p, parse("{ /* ... */ }"));
    assertThat(p, parse(": lala(0) {}"));
    assertThat(p, parse("{ return \"read empty stack\"; }"));
  }

  @Test
  public void initializer_realLife() {
    p.setRootRule(g.initializer);

    assertThat(p, parse("(new int(42))"));
    assertThat(p, parse("((istream_iterator<string>(cin)), istream_iterator<string>())"));
  }

  @Test
  public void initializer_clause_reallife() {
    p.setRootRule(g.initializer_clause);

    assertThat(p, parse("(istream_iterator<string>(cin))"));
    assertThat(p, parse("istream_iterator<string>()"));
  }

  @Test
  public void initializer_list() {
    p.setRootRule(g.initializer_list);

    g.initializer_clause.mock();

    assertThat(p, parse("initializer_clause"));
    assertThat(p, parse("initializer_clause ..."));
    assertThat(p, parse("initializer_clause , initializer_clause"));
    assertThat(p, parse("initializer_clause , initializer_clause ..."));
  }

  @Test
  public void initializer_list_reallife() {
    p.setRootRule(g.initializer_list);

    assertThat(p, parse("(istream_iterator<string>(cin)), istream_iterator<string>()"));
  }

  @Test
  public void braced_init_list() {
    p.setRootRule(g.braced_init_list);

    g.initializer_list.mock();

    assertThat(p, parse("{}"));
    assertThat(p, parse("{ initializer_list }"));
    assertThat(p, parse("{ initializer_list , }"));
  }
}
