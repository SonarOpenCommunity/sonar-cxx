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

public class DeclarationsTest {

  ExtendedStackTrace stackTrace = new ExtendedStackTrace();
  Parser<CxxGrammar> p = CxxParser.createDebugParser(mock(SquidAstVisitorContext.class), stackTrace);
  CxxGrammar g = p.getGrammar();

  @Test
  public void declarationSeq() {
    p.setRootRule(g.declarationSeq);

    g.declaration.mock();

    assertThat(p, parse("declaration"));
    assertThat(p, parse("declaration declaration"));
  }

  @Test
  public void declaration() {
    p.setRootRule(g.declaration);

    g.blockDeclaration.mock();
    g.functionDefinition.mock();
    g.templateDeclaration.mock();
    g.explicitInstantiation.mock();
    g.explicitSpecialization.mock();
    g.linkageSpecification.mock();
    g.namespaceDefinition.mock();
    g.emptyDeclaration.mock();
    g.attributeDeclaration.mock();

    assertThat(p, parse("blockDeclaration"));
    assertThat(p, parse("functionDefinition"));
    assertThat(p, parse("templateDeclaration"));
    assertThat(p, parse("explicitInstantiation"));
    assertThat(p, parse("explicitSpecialization"));
    assertThat(p, parse("linkageSpecification"));
    assertThat(p, parse("namespaceDefinition"));
    assertThat(p, parse("emptyDeclaration"));
    assertThat(p, parse("attributeDeclaration"));
  }

  @Test
  public void declaration_reallife() {
    p.setRootRule(g.declaration);

    assertThat(p, parse("t* pt;"));
    assertThat(p, parse("int i = 0;"));
    assertThat(p, parse("sometype& somefunc();"));
    assertThat(p, parse("sometype foo();"));
    assertThat(p, parse("sometype (*foo)(void);"));
    assertThat(p, parse("aligned_storage<sizeof(result_type)> cache;"));

    // We cannot parse this, unfortunately. The reasons is an ambiguity between
    // relational exressions and template parameter list syntax, which cannot be
    // resolved without name lookup, at least according to the standard. Bad c++...
    // assertThat(p, parse("mpl<N/M>();"));

    assertThat(p, parse("bool operator==<B>(A const&, A const&);"));
  }

  @Test
  public void aliasDeclaration() {
    p.setRootRule(g.aliasDeclaration);

    g.attributeSpecifierSeq.mock();
    g.typeId.mock();

    assertThat(p, parse("using foo = typeId"));
    assertThat(p, parse("using foo attributeSpecifierSeq = typeId"));
  }

  @Test
  public void simpleDeclaration() {
    p.setRootRule(g.simpleDeclaration);

    g.attributeSpecifierSeq.mock();
    g.simpleDeclSpecifierSeq.mock();
    g.initDeclaratorList.mock();

    assertThat(p, parse(";"));
    assertThat(p, parse("initDeclaratorList ;"));
    assertThat(p, parse("simpleDeclSpecifierSeq ;"));
    assertThat(p, parse("simpleDeclSpecifierSeq initDeclaratorList ;"));

    assertThat(p, parse("attributeSpecifierSeq initDeclaratorList ;"));
    assertThat(p, parse("attributeSpecifierSeq simpleDeclSpecifierSeq initDeclaratorList ;"));
  }

  @Test
  public void simpleDeclaration_reallife() {
    p.setRootRule(g.simpleDeclaration);

    assertThat(p, parse("sometype foo();"));
    assertThat(p, parse("const auto_ptr<int> p(new int(42));"));
    assertThat(p, parse("list<string>::iterator pos1, pos2;"));
    assertThat(p, parse("vector<string> coll((istream_iterator<string>(cin)), istream_iterator<string>());"));
    assertThat(p, parse("carray<int,10> a;"));
    assertThat(p, parse("void foo(string, bool);"));
    assertThat(p, parse("friend class ::SMLCGroupHierarchyImpl;"));
    assertThat(p, parse("void foo(int, type[]);"));
    assertThat(p, parse("bool operator==<B>(A const&, A const&);"));
  }

  @Test
  public void staticAssertDeclaration() {
    p.setRootRule(g.staticAssertDeclaration);

    g.constantExpression.mock();

    assertThat(p, parse("static_assert ( constantExpression , \"foo\" ) ;"));
  }

  @Test
  public void declSpecifier_reallife() {
    p.setRootRule(g.declSpecifier);

    assertThat(p, parse("register")); // a storage class
    assertThat(p, parse("inline")); // a function specifier
    assertThat(p, parse("friend")); // a function specifier
    assertThat(p, parse("void")); // a builtin type

    // declSpecifier
    assertThat(p, parse("friend"));
    assertThat(p, parse("typedef"));
    assertThat(p, parse("constexpr"));

    // enum specifier
    assertThat(p, parse("enum foo { MONDAY=1 }"));

    // class specifier
    assertThat(p, parse("class foo final : bar { }"));
    assertThat(p, parse("class foo final : bar { int foo(); }"));

    // type names
    assertThat(p, parse("class_foo")); // className->identifier
    assertThat(p, parse("class_foo<bar>")); // className->simpleTemplateId
    assertThat(p, parse("enum_foo")); // enumName->identifier
    assertThat(p, parse("typedef_foo")); // typedefName->identifier
    assertThat(p, parse("foo<bar>"));
    assertThat(p, parse("paramtype<T>"));
    assertThat(p, parse("carray<int,10>"));
    assertThat(p, parse("::P"));
  }

  @Test
  public void typeSpecifier_reallife() {
    p.setRootRule(g.typeSpecifier);

    assertThat(p, parse("enum foo { MONDAY=1 }"));
    assertThat(p, parse("carray<int,10>"));
  }

  @Test
  public void typeSpecifierSeq() {
    p.setRootRule(g.typeSpecifierSeq);

    g.typeSpecifier.mock();
    g.attributeSpecifierSeq.mock();

    assertThat(p, parse("typeSpecifier"));
    assertThat(p, parse("typeSpecifier attributeSpecifierSeq"));
    assertThat(p, parse("typeSpecifier typeSpecifier"));
    assertThat(p, parse("typeSpecifier typeSpecifier attributeSpecifierSeq"));
  }

  @Test
  public void typeSpecifierSeq_reallife() {
    p.setRootRule(g.typeSpecifierSeq);

    assertThat(p, parse("templatetype<T>"));
    assertThat(p, parse("templatetype<T> int"));
  }

  @Test
  public void trailingTypeSpecifierSeq() {
    p.setRootRule(g.trailingTypeSpecifierSeq);

    g.trailingTypeSpecifier.mock();
    g.attributeSpecifierSeq.mock();

    assertThat(p, parse("trailingTypeSpecifier"));
    assertThat(p, parse("trailingTypeSpecifier attributeSpecifierSeq"));
    assertThat(p, parse("trailingTypeSpecifier trailingTypeSpecifier"));
    assertThat(p, parse("trailingTypeSpecifier trailingTypeSpecifier attributeSpecifierSeq"));
  }

  @Test
  public void simpleTypeSpecifier() {
    p.setRootRule(g.simpleTypeSpecifier);

    g.nestedNameSpecifier.mock();
    g.typeName.mock();
    g.simpleTemplateId.mock();
    g.decltypeSpecifier.mock();

    assertThat(p, parse("typeName"));
    assertThat(p, parse("nestedNameSpecifier typeName"));

    assertThat(p, parse("nestedNameSpecifier template simpleTemplateId"));

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
    assertThat(p, parse("decltypeSpecifier"));
  }

  @Test
  public void simpleTypeSpecifier_reallife() {
    p.setRootRule(g.simpleTypeSpecifier);
    assertThat(p, parse("::P"));
  }

  @Test
  public void typeName() {
    p.setRootRule(g.typeName);

    g.className.mock();
    g.enumName.mock();
    g.typedefName.mock();
    g.simpleTemplateId.mock();

    assertThat(p, parse("className"));
    assertThat(p, parse("enumName"));
    assertThat(p, parse("typedefName"));
    assertThat(p, parse("simpleTemplateId"));
  }

  @Test
  public void typeName_reallife() {
    p.setRootRule(g.typeName);

    assertThat(p, parse("sometype<int>"));
  }

  @Test
  public void elaboratedTypeSpecifier() {
    p.setRootRule(g.elaboratedTypeSpecifier);

    g.classKey.mock();
    g.attributeSpecifierSeq.mock();
    g.nestedNameSpecifier.mock();
    g.simpleTemplateId.mock();

    assertThat(p, parse("classKey foo"));
    assertThat(p, parse("classKey attributeSpecifierSeq foo"));
    assertThat(p, parse("classKey nestedNameSpecifier foo"));
    assertThat(p, parse("classKey attributeSpecifierSeq nestedNameSpecifier foo"));

    assertThat(p, parse("classKey simpleTemplateId"));
    assertThat(p, parse("classKey nestedNameSpecifier simpleTemplateId"));
    assertThat(p, parse("classKey nestedNameSpecifier template simpleTemplateId"));

    assertThat(p, parse("enum foo"));
    assertThat(p, parse("enum nestedNameSpecifier foo"));
  }

  @Test
  public void elaboratedTypeSpecifier_reallife() {
    p.setRootRule(g.elaboratedTypeSpecifier);

    assertThat(p, parse("class ::A"));
  }

  @Test
  public void enumSpecifier() {
    p.setRootRule(g.enumSpecifier);

    g.enumHead.mock();
    g.enumeratorList.mock();

    assertThat(p, parse("enumHead { }"));
    assertThat(p, parse("enumHead { enumeratorList }"));
    assertThat(p, parse("enumHead { enumeratorList , }"));
  }

  @Test
  public void enumSpecifier_reallife() {
    p.setRootRule(g.enumSpecifier);

    assertThat(p, parse("enum foo { MONDAY=1 }"));
  }

  @Test
  public void enumHead() {
    p.setRootRule(g.enumHead);

    g.enumKey.mock();
    g.attributeSpecifierSeq.mock();
    g.enumBase.mock();
    g.nestedNameSpecifier.mock();

    assertThat(p, parse("enumKey"));
    assertThat(p, parse("enumKey attributeSpecifierSeq"));
    assertThat(p, parse("enumKey attributeSpecifierSeq foo"));
    assertThat(p, parse("enumKey attributeSpecifierSeq foo enumBase"));

    assertThat(p, parse("enumKey nestedNameSpecifier foo"));
    assertThat(p, parse("enumKey attributeSpecifierSeq nestedNameSpecifier foo"));
    assertThat(p, parse("enumKey attributeSpecifierSeq nestedNameSpecifier foo enumBase"));
  }

  @Test
  public void enumeratorList() {
    p.setRootRule(g.enumeratorList);

    g.enumeratorDefinition.mock();

    assertThat(p, parse("enumeratorDefinition"));
    assertThat(p, parse("enumeratorDefinition , enumeratorDefinition"));
  }

  @Test
  public void enumeratorDefinition() {
    p.setRootRule(g.enumeratorDefinition);

    g.enumerator.mock();
    g.constantExpression.mock();

    assertThat(p, parse("enumerator"));
    assertThat(p, parse("enumerator = constantExpression"));
  }

  @Test
  public void namespaceDefinition_reallife() {
    p.setRootRule(g.namespaceDefinition);

    assertThat(p, parse("namespace MyLib { double readAndProcessSum (std::istream&); }"));
  }

  @Test
  public void usingDeclaration() {
    p.setRootRule(g.usingDeclaration);

    g.nestedNameSpecifier.mock();
    g.unqualifiedId.mock();

    assertThat(p, parse("using nestedNameSpecifier unqualifiedId ;"));
    assertThat(p, parse("using typename nestedNameSpecifier unqualifiedId ;"));
    assertThat(p, parse("using :: unqualifiedId ;"));
  }

  @Test
  public void usingDirective() {
    p.setRootRule(g.usingDirective);

    assertThat(p, parse("using namespace std;"));
  }

  @Test
  public void linkageSpecification() {
    p.setRootRule(g.linkageSpecification);

    g.declaration.mock();
    g.declarationSeq.mock();

    assertThat(p, parse("extern \"foo\" { declarationSeq }"));
    assertThat(p, parse("extern \"foo\" declaration"));
  }
}
