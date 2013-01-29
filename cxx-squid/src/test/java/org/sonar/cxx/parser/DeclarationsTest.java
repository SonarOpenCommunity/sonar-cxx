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

public class DeclarationsTest {

  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();

  @Test
  public void declaration_seq() {
    p.setRootRule(g.declaration_seq);

    g.declaration.mock();

    assertThat(p, parse("declaration"));
    assertThat(p, parse("declaration declaration"));
  }

  @Test
  public void declaration() {
    p.setRootRule(g.declaration);

    g.block_declaration.mock();
    g.function_definition.mock();
    g.template_declaration.mock();
    g.explicit_instantiation.mock();
    g.explicit_specialization.mock();
    g.linkage_specification.mock();
    g.namespace_definition.mock();
    g.empty_declaration.mock();
    g.attribute_declaration.mock();

    assertThat(p, parse("block_declaration"));
    assertThat(p, parse("function_definition"));
    assertThat(p, parse("template_declaration"));
    assertThat(p, parse("explicit_instantiation"));
    assertThat(p, parse("explicit_specialization"));
    assertThat(p, parse("linkage_specification"));
    assertThat(p, parse("namespace_definition"));
    assertThat(p, parse("empty_declaration"));
    assertThat(p, parse("attribute_declaration"));
  }

  @Test
  public void declaration_realLife() {
    p.setRootRule(g.declaration);

    assertThat(p, parse("t* pt;"));
    assertThat(p, parse("int i = 0;"));
    assertThat(p, parse("sometype& somefunc();"));
    assertThat(p, parse("sometype foo();"));
    assertThat(p, parse("sometype (*foo)(void);"));
    assertThat(p, parse("aligned_storage<sizeof(result_type)> cache;"));
    assertThat(p, parse("template<typename Args> void result(Args const &(2) ) const {}"));
  }

  @Test
  public void alias_declaration() {
    p.setRootRule(g.alias_declaration);

    g.attribute_specifier_seq.mock();
    g.type_id.mock();

    assertThat(p, parse("using foo = type_id"));
    assertThat(p, parse("using foo attribute_specifier_seq = type_id"));
  }

  @Test
  public void simple_declaration() {
    p.setRootRule(g.simple_declaration);

    g.attribute_specifier_seq.mock();
    g.simple_decl_specifier_seq.mock();
    g.init_declarator_list.mock();

    assertThat(p, parse(";"));
    assertThat(p, parse("init_declarator_list ;"));
    assertThat(p, parse("simple_decl_specifier_seq ;"));
    assertThat(p, parse("simple_decl_specifier_seq init_declarator_list ;"));

    assertThat(p, parse("attribute_specifier_seq init_declarator_list ;"));
    assertThat(p, parse("attribute_specifier_seq simple_decl_specifier_seq init_declarator_list ;"));
  }

  @Test
  public void simple_declaration_reallife() {
    p.setRootRule(g.simple_declaration);

    assertThat(p, parse("sometype foo();"));
    assertThat(p, parse("const auto_ptr<int> p(new int(42));"));
    assertThat(p, parse("list<string>::iterator pos1, pos2;"));
    assertThat(p, parse("vector<string> coll((istream_iterator<string>(cin)), istream_iterator<string>());"));
    assertThat(p, parse("carray<int,10> a;"));
    assertThat(p, parse("void foo(string, bool);"));
    assertThat(p, parse("friend class ::SMLCGroupHierarchyImpl;"));
    assertThat(p, parse("void foo(int, type[]);"));
  }

  @Test
  public void static_assert_declaration() {
    p.setRootRule(g.static_assert_declaration);

    g.constant_expression.mock();

    assertThat(p, parse("static_assert ( constant_expression , \"foo\" ) ;"));
  }

  @Test
  public void decl_specifier_realLife() {
    p.setRootRule(g.decl_specifier);

    assertThat(p, parse("register")); // a storage class
    assertThat(p, parse("inline")); // a function specifier
    assertThat(p, parse("friend")); // a function specifier
    assertThat(p, parse("void")); // a builtin type

    // decl_specifier
    assertThat(p, parse("friend"));
    assertThat(p, parse("typedef"));
    assertThat(p, parse("constexpr"));

    // enum specifier
    assertThat(p, parse("enum foo { MONDAY=1 }"));

    // class specifier
    assertThat(p, parse("class foo final : bar { }"));
    assertThat(p, parse("class foo final : bar { int foo(); }"));

    // type names
    assertThat(p, parse("class_foo")); // class_name->identifier
    assertThat(p, parse("class_foo<bar>")); // class_name->simple_template_id
    assertThat(p, parse("enum_foo")); // enum_name->identifier
    assertThat(p, parse("typedef_foo")); // typedef_name->identifier
    assertThat(p, parse("foo<bar>"));
    assertThat(p, parse("paramtype<T>"));
    assertThat(p, parse("carray<int,10>"));
    assertThat(p, parse("::P"));
  }

  @Test
  public void type_specifier_realLife() {
    p.setRootRule(g.type_specifier);
    
    assertThat(p, parse("enum foo { MONDAY=1 }"));
    assertThat(p, parse("carray<int,10>"));
  }

  @Test
  public void type_specifier_seq() {
    p.setRootRule(g.type_specifier_seq);

    g.type_specifier.mock();
    g.attribute_specifier_seq.mock();

    assertThat(p, parse("type_specifier"));
    assertThat(p, parse("type_specifier attribute_specifier_seq"));
    assertThat(p, parse("type_specifier type_specifier"));
    assertThat(p, parse("type_specifier type_specifier attribute_specifier_seq"));
  }

  @Test
  public void type_specifier_seq_realLife() {
    p.setRootRule(g.type_specifier_seq);

    assertThat(p, parse("templatetype<T>"));
    assertThat(p, parse("templatetype<T> int"));
  }

  @Test
  public void trailing_type_specifier_seq() {
    p.setRootRule(g.trailing_type_specifier_seq);

    g.trailing_type_specifier.mock();
    g.attribute_specifier_seq.mock();

    assertThat(p, parse("trailing_type_specifier"));
    assertThat(p, parse("trailing_type_specifier attribute_specifier_seq"));
    assertThat(p, parse("trailing_type_specifier trailing_type_specifier"));
    assertThat(p, parse("trailing_type_specifier trailing_type_specifier attribute_specifier_seq"));
  }

  @Test
  public void simple_type_specifier() {
    p.setRootRule(g.simple_type_specifier);

    g.nested_name_specifier.mock();
    g.type_name.mock();
    g.simple_template_id.mock();
    g.decltype_specifier.mock();

    assertThat(p, parse("type_name"));
    assertThat(p, parse("nested_name_specifier type_name"));

    assertThat(p, parse("nested_name_specifier template simple_template_id"));

    assertThat(p, parse("char"));
    assertThat(p, parse("char16_t"));
    assertThat(p, parse("char32_t"));
    assertThat(p, parse("wchar_t"));
    assertThat(p, parse("bool"));
    assertThat(p, parse("short"));
    assertThat(p, parse("int"));
    assertThat(p, parse("long"));
    assertThat(p, parse("signed"));
    assertThat(p, parse("unsigned"));
    assertThat(p, parse("float"));
    assertThat(p, parse("double"));
    assertThat(p, parse("void"));
    assertThat(p, parse("auto"));
    assertThat(p, parse("decltype_specifier"));
  }

  @Test
  public void simple_type_specifier_real() {
    p.setRootRule(g.simple_type_specifier);
    assertThat(p, parse("::P"));
  }
  
  @Test
  public void type_name() {
    p.setRootRule(g.type_name);

    g.class_name.mock();
    g.enum_name.mock();
    g.typedef_name.mock();
    g.simple_template_id.mock();

    assertThat(p, parse("class_name"));
    assertThat(p, parse("enum_name"));
    assertThat(p, parse("typedef_name"));
    assertThat(p, parse("simple_template_id"));
  }

  @Test
  public void type_name_reallife() {
    p.setRootRule(g.type_name);

    assertThat(p, parse("sometype<int>"));
  }

  @Test
  public void elaborated_type_specifier() {
    p.setRootRule(g.elaborated_type_specifier);

    g.class_key.mock();
    g.attribute_specifier_seq.mock();
    g.nested_name_specifier.mock();
    g.simple_template_id.mock();

    assertThat(p, parse("class_key foo"));
    assertThat(p, parse("class_key attribute_specifier_seq foo"));
    assertThat(p, parse("class_key nested_name_specifier foo"));
    assertThat(p, parse("class_key attribute_specifier_seq nested_name_specifier foo"));

    assertThat(p, parse("class_key simple_template_id"));
    assertThat(p, parse("class_key nested_name_specifier simple_template_id"));
    assertThat(p, parse("class_key nested_name_specifier template simple_template_id"));

    assertThat(p, parse("enum foo"));
    assertThat(p, parse("enum nested_name_specifier foo"));
  }

  @Test
  public void elaborated_type_specifier_reallife() {
    p.setRootRule(g.elaborated_type_specifier);

    assertThat(p, parse("class ::A"));
  }

  @Test
  public void enum_specifier() {
    p.setRootRule(g.enum_specifier);

    g.enum_head.mock();
    g.enumerator_list.mock();

    assertThat(p, parse("enum_head { }"));
    assertThat(p, parse("enum_head { enumerator_list }"));
    assertThat(p, parse("enum_head { enumerator_list , }"));
  }

  @Test
  public void enum_specifier_realLife() {
    p.setRootRule(g.enum_specifier);

    assertThat(p, parse("enum foo { MONDAY=1 }"));
  }

  @Test
  public void enum_head() {
    p.setRootRule(g.enum_head);

    g.enum_key.mock();
    g.attribute_specifier_seq.mock();
    g.enum_base.mock();
    g.nested_name_specifier.mock();

    assertThat(p, parse("enum_key"));
    assertThat(p, parse("enum_key attribute_specifier_seq"));
    assertThat(p, parse("enum_key attribute_specifier_seq foo"));
    assertThat(p, parse("enum_key attribute_specifier_seq foo enum_base"));

    assertThat(p, parse("enum_key nested_name_specifier foo"));
    assertThat(p, parse("enum_key attribute_specifier_seq nested_name_specifier foo"));
    assertThat(p, parse("enum_key attribute_specifier_seq nested_name_specifier foo enum_base"));
  }

  @Test
  public void enumerator_list() {
    p.setRootRule(g.enumerator_list);

    g.enumerator_definition.mock();

    assertThat(p, parse("enumerator_definition"));
    assertThat(p, parse("enumerator_definition , enumerator_definition"));
  }

  @Test
  public void enumerator_definition() {
    p.setRootRule(g.enumerator_definition);

    g.enumerator.mock();
    g.constant_expression.mock();

    assertThat(p, parse("enumerator"));
    assertThat(p, parse("enumerator = constant_expression"));
  }

  @Test
  public void namespace_definition_reallife() {
    p.setRootRule(g.namespace_definition);

    assertThat(p, parse("namespace MyLib { double readAndProcessSum (std::istream&); }"));
  }

  @Test
  public void using_declaration() {
    p.setRootRule(g.using_declaration);

    g.nested_name_specifier.mock();
    g.unqualified_id.mock();

    assertThat(p, parse("using nested_name_specifier unqualified_id ;"));
    assertThat(p, parse("using typename nested_name_specifier unqualified_id ;"));
    assertThat(p, parse("using :: unqualified_id ;"));
  }

  @Test
  public void using_directive() {
    p.setRootRule(g.using_directive);

    assertThat(p, parse("using namespace std;"));
  }

  @Test
  public void linkage_specification() {
    p.setRootRule(g.linkage_specification);

    g.declaration.mock();
    g.declaration_seq.mock();

    assertThat(p, parse("extern \"foo\" { declaration_seq }"));
    assertThat(p, parse("extern \"foo\" declaration"));
  }
}
