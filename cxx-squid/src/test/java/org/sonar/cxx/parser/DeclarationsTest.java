/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * sonarqube@googlegroups.com
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

import static org.sonar.sslr.tests.Assertions.assertThat;

import org.junit.Test;

public class DeclarationsTest extends ParserBaseTest {

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
    // relational expressions and template parameter list syntax, which cannot be
    // resolved without name lookup, at least according to the standard. Bad c++...
    // assertThat(p).matches("mpl<N/M>();");

    assertThat(p).matches("bool operator==<B>(A const&, A const&);");
    assertThat(p).matches("sometype foo(int& var1);");
    assertThat(p).matches("auto foo(int& var1) -> int;");
    assertThat(p).matches("auto fp11() -> void(*)(const std::string&);");

    assertThat(p).matches("t^ pt;");
    assertThat(p).matches("t% pt;");
    assertThat(p).matches("t^% pt;");
    
//    assertThat(p).matches("int property;");
//    assertThat(p).matches("int property = 0;");

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
    assertThat(p).matches("bool operator==(const lhs&, const rhs&);");
    assertThat(p).matches("bool operator==<B>(A const&, A const&);");

    assertThat(p).matches("int foo();");
    assertThat(p).matches("const int foo();");
    assertThat(p).matches("int* foo();");
    assertThat(p).matches("const int* foo();");
    assertThat(p).matches("int& foo();");
    assertThat(p).matches("const int& foo();");
    assertThat(p).matches("long long foo();");
    assertThat(p).matches("const long long foo();");
    assertThat(p).matches("long long* foo();");
    assertThat(p).matches("const long long* foo();");
    assertThat(p).matches("long long& foo();");
    assertThat(p).matches("const long long& foo();");    
    assertThat(p).matches("MyClass foo();");
    assertThat(p).matches("const MyClass foo();");
    assertThat(p).matches("MyClass* foo();");
    assertThat(p).matches("const MyClass* foo();");
    assertThat(p).matches("MyClass* foo();");
    assertThat(p).matches("const MyClass* foo();");
    
    assertThat(p).matches("extern int foo();");
    
    assertThat(p).matches("auto to_string(int value) -> int;");
    assertThat(p).matches("auto to_string(int value) -> long long;");
    assertThat(p).matches("auto to_string(int value) -> std::string;");
    assertThat(p).matches("auto size() const -> std::size_t;");
    assertThat(p).matches("auto str() const;");
    assertThat(p).matches("auto equal_range(ForwardIterator first, ForwardIterator last, const Type& value) -> std::pair<ForwardIterator, ForwardIterator>;");

    assertThat(p).matches("auto str() const -> const char*;");
    assertThat(p).matches("auto std::map::at(const key_type& key) -> mapped_type&;");
    assertThat(p).matches("auto f() -> int(*)[4];");
    assertThat(p).matches("auto fpif(int) -> int(*)(int);");

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
    assertThat(p).matches("void"); // a built-in type

    // declSpecifier
    assertThat(p).matches("friend");
    assertThat(p).matches("typedef");
    assertThat(p).matches("constexpr");

    // enum specifier
    assertThat(p).matches("enum foo { MONDAY=1 }");

    // class specifier
    assertThat(p).matches("class foo final : bar { }");
    assertThat(p).matches("class foo final : bar { int foo(); }");
    assertThat(p).matches("class foo final : public ::bar { int foo(); }");
    // CLI extension
    assertThat(p).matches("class foo sealed : bar { }");
    assertThat(p).matches("ref class foo : bar { }");
    assertThat(p).matches("public ref class foo : bar { }");
    assertThat(p).matches("private ref class foo : bar { }");

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
    g.rule(CxxGrammarImpl.cliAttribute).mock();

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
    g.rule(CxxGrammarImpl.cliAttribute).mock();

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

  @Test
  public void propertyDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.cliPropertyDefinition));

    g.rule(CxxGrammarImpl.cliAttributes).mock();
    g.rule(CxxGrammarImpl.cliPropertyModifiers).mock();
    g.rule(CxxGrammarImpl.declarator).mock();
    g.rule(CxxGrammarImpl.cliPropertyIndexes).mock();
    g.rule(CxxGrammarImpl.cliAccessorSpecification).mock();

    assertThat(p).matches("property typeSpecifier declarator ;");
    assertThat(p).matches("cliPropertyModifiers property typeSpecifier declarator ;");
    assertThat(p).matches("cliAttributes cliPropertyModifiers property typeSpecifier declarator ;");
    assertThat(p).matches("cliAttributes cliPropertyModifiers property cliPropertyOrEventName declarator ;");
    assertThat(p).matches("cliAttributes cliPropertyModifiers property nestedNameSpecifier cliPropertyOrEventName declarator ;");

    assertThat(p).matches("property typeSpecifier declarator { cliAccessorSpecification }");
    assertThat(p).matches("cliPropertyModifiers property typeSpecifier declarator { cliAccessorSpecification }");
    assertThat(p).matches("cliPropertyModifiers property typeSpecifier declarator cliPropertyIndexes { cliAccessorSpecification }");
    assertThat(p).matches("cliAttributes cliPropertyModifiers property typeSpecifier declarator { cliAccessorSpecification }");
    assertThat(p).matches("cliAttributes cliPropertyModifiers property cliPropertyOrEventName declarator { cliAccessorSpecification }");
    assertThat(p).matches("cliAttributes cliPropertyModifiers property nestedNameSpecifier cliPropertyOrEventName declarator { cliAccessorSpecification }");
    assertThat(p).matches("cliAttributes cliPropertyModifiers property typeSpecifier declarator cliPropertyIndexes { cliAccessorSpecification }");
    assertThat(p).matches("cliAttributes cliPropertyModifiers property nestedNameSpecifier cliPropertyOrEventName declarator cliPropertyIndexes { cliAccessorSpecification }");
  }

  @Test
  public void accessorSpecificationDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.cliAccessorSpecification));

    g.rule(CxxGrammarImpl.cliAccessorDeclaration).mock();
    g.rule(CxxGrammarImpl.accessSpecifier).mock();

    assertThat(p).matches("");
    assertThat(p).matches("cliAccessorDeclaration");
    assertThat(p).matches("cliAccessorDeclaration cliAccessorDeclaration");
    assertThat(p).matches("accessSpecifier : cliAccessorDeclaration");
    assertThat(p).matches("accessSpecifier : cliAccessorDeclaration cliAccessorDeclaration");
  }
  @Test
  public void eventDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.cliEventDefinition));

    g.rule(CxxGrammarImpl.cliAttributes).mock();
    g.rule(CxxGrammarImpl.cliEventModifiers).mock();
    g.rule(CxxGrammarImpl.cliEventType).mock();
    g.rule(CxxGrammarImpl.cliAccessorSpecification).mock();

    
    assertThat(p).matches("event cliEventType myEvent ;");
    assertThat(p).matches("cliEventModifiers event cliEventType myEvent ;");
    assertThat(p).matches("cliAttributes cliEventModifiers event cliEventType myEvent ;");

    assertThat(p).matches("event cliEventType myEvent { cliAccessorSpecification }");
    assertThat(p).matches("cliEventModifiers event cliEventType myEvent { cliAccessorSpecification }");
    assertThat(p).matches("cliAttributes cliEventModifiers event cliEventType myEvent { cliAccessorSpecification }");
  }
  @Test
  public void eventTypeDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.cliEventType));

    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();
    g.rule(CxxGrammarImpl.typeName).mock();
    g.rule(CxxGrammarImpl.templateId).mock();

    assertThat(p).matches("nestedNameSpecifier typeName");
    assertThat(p).matches(":: nestedNameSpecifier typeName");
    assertThat(p).matches("nestedNameSpecifier typeName^");
    assertThat(p).matches(":: nestedNameSpecifier typeName^");

    assertThat(p).matches("nestedNameSpecifier template templateId ^");
    assertThat(p).matches(":: nestedNameSpecifier template templateId ^");
  }
  @Test
  public void cliAttributesDefinition() {
    p.setRootRule(g.rule(CxxGrammarImpl.cliAttributes));
    
    g.rule(CxxGrammarImpl.typeName).mock();
    g.rule(CxxGrammarImpl.cliAttributeTarget).mock();
    g.rule(CxxGrammarImpl.cliPositionArgumentList).mock();

    assertThat(p).matches("[ typeName ]");
    assertThat(p).matches("[ cliAttributeTarget : typeName ]");
    assertThat(p).matches("[ cliAttributeTarget : typeName, typeName ]");
    assertThat(p).matches("[ cliAttributeTarget : typeName, typeName (cliPositionArgumentList) ]");
    
  }
  @Test
  public void cliAttributesDefinition_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.cliAttributes));

    assertThat(p).matches("[Attr]");
    assertThat(p).matches("[assembly:Attr]");
    assertThat(p).matches("[class:Attr]");
    assertThat(p).matches("[constructor:Attr]");
    assertThat(p).matches("[assembly:Attr]");
    assertThat(p).matches("[delegate:Attr]");
    assertThat(p).matches("[enum:Attr]");
    assertThat(p).matches("[event:Attr]");
    assertThat(p).matches("[field:Attr]");
    assertThat(p).matches("[interface:Attr]");
    assertThat(p).matches("[method:Attr]");
    assertThat(p).matches("[parameter:Attr]");
    assertThat(p).matches("[property:Attr]");
    assertThat(p).matches("[returnvalue:Attr]");
    assertThat(p).matches("[struct:Attr]");
    
    assertThat(p).matches("[returnvalue:Attr(x)]");
    assertThat(p).matches("[AttributeUsage( AttributeTargets::All )]");
    assertThat(p).matches("[AttributeUsage(AttributeTargets::Class | AttributeTargets::Method)]");
    assertThat(p).matches("[ AnotherAttr( gcnew array<Object ^> { 3.14159, \"pi\" }, var1 = gcnew array<Object ^> { \"a\", \"b\" } ) ]");
  }
}
