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

public class DeclarationsTest {
  Parser<Grammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  Grammar g = p.getGrammar();

  @Test
  public void declarationSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.declarationSeq));

    g.rule(CxxGrammarImpl.declaration).mock();

    assertThat(p).matches("declaration");
    assertThat(p).matches("declaration declaration");
  }

  @Test
  public void declaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.declaration));

    g.rule(CxxGrammarImpl.blockDeclaration).mock();
    g.rule(CxxGrammarImpl.functionDefinition).mock();
    g.rule(CxxGrammarImpl.templateDeclaration).mock();
    g.rule(CxxGrammarImpl.explicitInstantiation).mock();
    g.rule(CxxGrammarImpl.explicitSpecialization).mock();
    g.rule(CxxGrammarImpl.linkageSpecification).mock();
    g.rule(CxxGrammarImpl.namespaceDefinition).mock();
    g.rule(CxxGrammarImpl.emptyDeclaration).mock();
    g.rule(CxxGrammarImpl.attributeDeclaration).mock();

    assertThat(p).matches("blockDeclaration");
    assertThat(p).matches("functionDefinition");
    assertThat(p).matches("templateDeclaration");
    assertThat(p).matches("explicitInstantiation");
    assertThat(p).matches("explicitSpecialization");
    assertThat(p).matches("linkageSpecification");
    assertThat(p).matches("namespaceDefinition");
    assertThat(p).matches("emptyDeclaration");
    assertThat(p).matches("attributeDeclaration");
  }

  @Test
  public void declaration_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.declaration));

    assertThat(p).matches("t* pt;");
    assertThat(p).matches("t* pt = nullptr;");
    assertThat(p).matches("t* pt {nullptr};");
    assertThat(p).matches("int i = 0;");
    assertThat(p).matches("sometype& somefunc();");
    assertThat(p).matches("sometype foo();");
    assertThat(p).matches("sometype (*foo)(void);");
    assertThat(p).matches("aligned_storage<sizeof(result_type)> cache;");

    // We cannot parse this, unfortunately. The reasons is an ambiguity between
    // relational exressions and template parameter list syntax, which cannot be
    // resolved without name lookup, at least according to the standard. Bad c++...
    // assertThat(p).matches("mpl<N/M>();");

    assertThat(p).matches("bool operator==<B>(A const&, A const&);");
  }

  @Test
  public void aliasDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.aliasDeclaration));

    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.typeId).mock();

    assertThat(p).matches("using foo = typeId");
    assertThat(p).matches("using foo attributeSpecifierSeq = typeId");
  }

  @Test
  public void simpleDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.simpleDeclaration));

    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.simpleDeclSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.initDeclaratorList).mock();

    assertThat(p).matches(";");
    assertThat(p).matches("initDeclaratorList ;");
    assertThat(p).matches("simpleDeclSpecifierSeq ;");
    assertThat(p).matches("simpleDeclSpecifierSeq initDeclaratorList ;");

    assertThat(p).matches("attributeSpecifierSeq initDeclaratorList ;");
    assertThat(p).matches("attributeSpecifierSeq simpleDeclSpecifierSeq initDeclaratorList ;");
  }

  @Test
  public void simpleDeclaration_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.simpleDeclaration));

    assertThat(p).matches("sometype foo();");
    assertThat(p).matches("const auto_ptr<int> p(new int(42));");
    assertThat(p).matches("list<string>::iterator pos1, pos2;");
    assertThat(p).matches("vector<string> coll((istream_iterator<string>(cin)), istream_iterator<string>());");
    assertThat(p).matches("carray<int,10> a;");
    assertThat(p).matches("void foo(string, bool);");
    assertThat(p).matches("friend class ::SMLCGroupHierarchyImpl;");
    assertThat(p).matches("void foo(int, type[]);");
    assertThat(p).matches("bool operator==<B>(A const&, A const&);");
  }

  @Test
  public void staticAssertDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.staticAssertDeclaration));

    g.rule(CxxGrammarImpl.constantExpression).mock();

    assertThat(p).matches("static_assert ( constantExpression , \"foo\" ) ;");
  }

  @Test
  public void declSpecifier_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.declSpecifier));

    assertThat(p).matches("register"); // a storage class
    assertThat(p).matches("inline"); // a function specifier
    assertThat(p).matches("friend"); // a function specifier
    assertThat(p).matches("void"); // a builtin type

    // declSpecifier
    assertThat(p).matches("friend");
    assertThat(p).matches("typedef");
    assertThat(p).matches("constexpr");

    // enum specifier
    assertThat(p).matches("enum foo { MONDAY=1 }");

    // class specifier
    assertThat(p).matches("class foo final : bar { }");
    assertThat(p).matches("class foo final : bar { int foo(); }");

    // type names
    assertThat(p).matches("class_foo"); // className->identifier
    assertThat(p).matches("class_foo<bar>"); // className->simpleTemplateId
    assertThat(p).matches("enum_foo"); // enumName->identifier
    assertThat(p).matches("typedef_foo"); // typedefName->identifier
    assertThat(p).matches("foo<bar>");
    assertThat(p).matches("paramtype<T>");
    assertThat(p).matches("carray<int,10>");
    assertThat(p).matches("::P");
  }

  @Test
  public void typeSpecifier_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.typeSpecifier));

    assertThat(p).matches("enum foo { MONDAY=1 }");
    assertThat(p).matches("carray<int,10>");
  }

  @Test
  public void typeSpecifierSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.typeSpecifierSeq));

    g.rule(CxxGrammarImpl.typeSpecifier).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();

    assertThat(p).matches("typeSpecifier");
    assertThat(p).matches("typeSpecifier attributeSpecifierSeq");
    assertThat(p).matches("typeSpecifier typeSpecifier");
    assertThat(p).matches("typeSpecifier typeSpecifier attributeSpecifierSeq");
  }

  @Test
  public void typeSpecifierSeq_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.typeSpecifierSeq));

    assertThat(p).matches("templatetype<T>");
    assertThat(p).matches("templatetype<T> int");
  }

  @Test
  public void trailingTypeSpecifierSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.trailingTypeSpecifierSeq));

    g.rule(CxxGrammarImpl.trailingTypeSpecifier).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();

    assertThat(p).matches("trailingTypeSpecifier");
    assertThat(p).matches("trailingTypeSpecifier attributeSpecifierSeq");
    assertThat(p).matches("trailingTypeSpecifier trailingTypeSpecifier");
    assertThat(p).matches("trailingTypeSpecifier trailingTypeSpecifier attributeSpecifierSeq");
  }

  @Test
  public void simpleTypeSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.simpleTypeSpecifier));

    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();
    g.rule(CxxGrammarImpl.typeName).mock();
    g.rule(CxxGrammarImpl.simpleTemplateId).mock();
    g.rule(CxxGrammarImpl.decltypeSpecifier).mock();

    assertThat(p).matches("typeName");
    assertThat(p).matches("nestedNameSpecifier typeName");

    assertThat(p).matches("nestedNameSpecifier template simpleTemplateId");

    assertThat(p).matches("char");
    assertThat(p).matches("char16_t");
    assertThat(p).matches("char32_t");
    assertThat(p).matches("wchar_t");
    assertThat(p).matches("bool");
    assertThat(p).matches("short");
    assertThat(p).matches("int");
    assertThat(p).matches("long");
    assertThat(p).matches("signed");
    assertThat(p).matches("unsigned");
    assertThat(p).matches("float");
    assertThat(p).matches("double");
    assertThat(p).matches("void");
    assertThat(p).matches("auto");
    assertThat(p).matches("decltypeSpecifier");
  }

  @Test
  public void simpleTypeSpecifier_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.simpleTypeSpecifier));
    assertThat(p).matches("::P");
  }

  @Test
  public void typeName() {
    p.setRootRule(g.rule(CxxGrammarImpl.typeName));

    g.rule(CxxGrammarImpl.className).mock();
    g.rule(CxxGrammarImpl.enumName).mock();
    g.rule(CxxGrammarImpl.typedefName).mock();
    g.rule(CxxGrammarImpl.simpleTemplateId).mock();

    assertThat(p).matches("className");
    assertThat(p).matches("enumName");
    assertThat(p).matches("typedefName");
    assertThat(p).matches("simpleTemplateId");
  }

  @Test
  public void typeName_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.typeName));

    assertThat(p).matches("sometype<int>");
  }

  @Test
  public void elaboratedTypeSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.elaboratedTypeSpecifier));

    g.rule(CxxGrammarImpl.classKey).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();
    g.rule(CxxGrammarImpl.simpleTemplateId).mock();

    assertThat(p).matches("classKey foo");
    assertThat(p).matches("classKey attributeSpecifierSeq foo");
    assertThat(p).matches("classKey nestedNameSpecifier foo");
    assertThat(p).matches("classKey attributeSpecifierSeq nestedNameSpecifier foo");

    assertThat(p).matches("classKey simpleTemplateId");
    assertThat(p).matches("classKey nestedNameSpecifier simpleTemplateId");
    assertThat(p).matches("classKey nestedNameSpecifier template simpleTemplateId");

    assertThat(p).matches("enum foo");
    assertThat(p).matches("enum nestedNameSpecifier foo");
  }

  @Test
  public void elaboratedTypeSpecifier_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.elaboratedTypeSpecifier));

    assertThat(p).matches("class ::A");
  }

  @Test
  public void enumSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumSpecifier));

    g.rule(CxxGrammarImpl.enumHead).mock();
    g.rule(CxxGrammarImpl.enumeratorList).mock();

    assertThat(p).matches("enumHead { }");
    assertThat(p).matches("enumHead { enumeratorList }");
    assertThat(p).matches("enumHead { enumeratorList , }");
  }

  @Test
  public void enumSpecifier_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumSpecifier));

    assertThat(p).matches("enum foo { MONDAY=1 }");
  }

  @Test
  public void enumHead() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumHead));

    g.rule(CxxGrammarImpl.enumKey).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.enumBase).mock();
    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();

    assertThat(p).matches("enumKey");
    assertThat(p).matches("enumKey attributeSpecifierSeq");
    assertThat(p).matches("enumKey attributeSpecifierSeq foo");
    assertThat(p).matches("enumKey attributeSpecifierSeq foo enumBase");

    assertThat(p).matches("enumKey nestedNameSpecifier foo");
    assertThat(p).matches("enumKey attributeSpecifierSeq nestedNameSpecifier foo");
    assertThat(p).matches("enumKey attributeSpecifierSeq nestedNameSpecifier foo enumBase");
  }

  @Test
  public void enumeratorList() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumeratorList));

    g.rule(CxxGrammarImpl.enumeratorDefinition).mock();

    assertThat(p).matches("enumeratorDefinition");
    assertThat(p).matches("enumeratorDefinition , enumeratorDefinition");
  }

  @Test
  public void enumeratorDefinition() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumeratorDefinition));

    g.rule(CxxGrammarImpl.enumerator).mock();
    g.rule(CxxGrammarImpl.constantExpression).mock();

    assertThat(p).matches("enumerator");
    assertThat(p).matches("enumerator = constantExpression");
  }

  @Test
  public void namespaceDefinition_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.namespaceDefinition));

    assertThat(p).matches("namespace MyLib { double readAndProcessSum (std::istream&); }");
  }

  @Test
  public void usingDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.usingDeclaration));

    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();
    g.rule(CxxGrammarImpl.unqualifiedId).mock();

    assertThat(p).matches("using nestedNameSpecifier unqualifiedId ;");
    assertThat(p).matches("using typename nestedNameSpecifier unqualifiedId ;");
    assertThat(p).matches("using :: unqualifiedId ;");
  }

  @Test
  public void usingDirective() {
    p.setRootRule(g.rule(CxxGrammarImpl.usingDirective));

    assertThat(p).matches("using namespace std;");
  }

  @Test
  public void linkageSpecification() {
    p.setRootRule(g.rule(CxxGrammarImpl.linkageSpecification));

    g.rule(CxxGrammarImpl.declaration).mock();
    g.rule(CxxGrammarImpl.declarationSeq).mock();

    assertThat(p).matches("extern \"foo\" { declarationSeq }");
    assertThat(p).matches("extern \"foo\" declaration");
  }
}
