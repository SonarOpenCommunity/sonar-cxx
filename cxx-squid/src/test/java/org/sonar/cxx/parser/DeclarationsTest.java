/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx.parser;

import org.junit.Test;
import static org.sonar.sslr.tests.Assertions.assertThat;

public class DeclarationsTest extends ParserBaseTestHelper {

  @Test
  public void declarationSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.declarationSeq));

    mockRule(CxxGrammarImpl.declaration);

    assertThat(p).matches("declaration");
    assertThat(p).matches("declaration declaration");
  }

  @Test
  public void declaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.declaration));

    mockRule(CxxGrammarImpl.blockDeclaration);
    mockRule(CxxGrammarImpl.nodeclspecFunctionDeclaration);
    mockRule(CxxGrammarImpl.functionDefinition);
    mockRule(CxxGrammarImpl.templateDeclaration);
    mockRule(CxxGrammarImpl.deductionGuide);
    mockRule(CxxGrammarImpl.cliGenericDeclaration);
    mockRule(CxxGrammarImpl.explicitInstantiation);
    mockRule(CxxGrammarImpl.explicitSpecialization);
    mockRule(CxxGrammarImpl.linkageSpecification);
    mockRule(CxxGrammarImpl.namespaceDefinition);
    mockRule(CxxGrammarImpl.emptyDeclaration);
    mockRule(CxxGrammarImpl.attributeDeclaration);
    mockRule(CxxGrammarImpl.vcAtlDeclaration);

    assertThat(p).matches("blockDeclaration");
    assertThat(p).matches("nodeclspecFunctionDeclaration");
    assertThat(p).matches("functionDefinition");
    assertThat(p).matches("templateDeclaration");
    assertThat(p).matches("deductionGuide");
    assertThat(p).matches("cliGenericDeclaration");
    assertThat(p).matches("explicitInstantiation");
    assertThat(p).matches("explicitSpecialization");
    assertThat(p).matches("linkageSpecification");
    assertThat(p).matches("namespaceDefinition");
    assertThat(p).matches("emptyDeclaration");
    assertThat(p).matches("attributeDeclaration");
    assertThat(p).matches("vcAtlDeclaration");
  }

  @Test
  public void declaration_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.declaration));

    assertThat(p).matches(";");
    assertThat(p).matches("extern int a;");
    assertThat(p).matches("extern const int c;");
    assertThat(p).matches("int f(int);");
    assertThat(p).matches("struct S;");
    assertThat(p).matches("typedef int Int;");
    assertThat(p).matches("extern X anotherX;");
    assertThat(p).matches("[[noreturn]] void f [[noreturn]] ();");
    assertThat(p).matches("using std::string;");
    assertThat(p).matches("using namespace D;");
    assertThat(p).matches("enum Color { red, green, blue };");
    assertThat(p).matches("enum byte : unsigned char {};");
    assertThat(p).matches("static_assert(std::is_copy_constructible<T>::value, \"Swap requires copying\");");
    assertThat(p).matches("t* pt;");
    assertThat(p).matches("t* pt = nullptr;");
    assertThat(p).matches("t* pt {nullptr};");
    assertThat(p).matches("int i = 0;");
    assertThat(p).matches("sometype& somefunc();");
    assertThat(p).matches("sometype foo();");
    assertThat(p).matches("sometype (*foo)(void);");
    assertThat(p).matches("aligned_storage<sizeof(result_type)> cache;");
    assertThat(p).matches("mpl<N/M>();");
    assertThat(p).matches("bool operator==<B>(A const&, A const&);");
    assertThat(p).matches("sometype foo(int& var1);");
    assertThat(p).matches("auto foo(int& var1) -> int;");
    assertThat(p).matches("auto fp11() -> void(*)(const std::string&);");
    assertThat(p).matches("t^ pt;");
    assertThat(p).matches("t% pt;");
    assertThat(p).matches("t^% pt;");
    assertThat(p).matches("int property;");
    assertThat(p).matches("int property = 0;");
  }

  @Test
  public void aliasDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.aliasDeclaration));

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.definingTypeId);

    assertThat(p).matches("using foo = definingTypeId ;");
    assertThat(p).matches("using foo attributeSpecifierSeq = definingTypeId ;");
  }

  @Test
  public void simpleDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.simpleDeclaration));

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.declSpecifierSeq);
    mockRule(CxxGrammarImpl.initDeclaratorList);
    mockRule(CxxGrammarImpl.refQualifier);
    mockRule(CxxGrammarImpl.identifierList);
    mockRule(CxxGrammarImpl.initializer);

    assertThat(p).matches(";");
    assertThat(p).matches("initDeclaratorList ;");
    assertThat(p).matches("declSpecifierSeq ;");
    assertThat(p).matches("declSpecifierSeq initDeclaratorList ;");

    assertThat(p).matches("attributeSpecifierSeq initDeclaratorList ;");
    assertThat(p).matches("attributeSpecifierSeq declSpecifierSeq initDeclaratorList ;");

    assertThat(p).matches("declSpecifierSeq [ identifierList ] initializer ;");
    assertThat(p).matches("attributeSpecifierSeq declSpecifierSeq [ identifierList ] initializer ;");
    assertThat(p).matches("declSpecifierSeq refQualifier [ identifierList ] initializer ;");
    assertThat(p).matches("attributeSpecifierSeq declSpecifierSeq refQualifier [ identifierList ] initializer ;");
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

    assertThat(p).matches("auto[a, b] = f();");
  }

  @Test
  public void staticAssertDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.staticAssertDeclaration));

    mockRule(CxxGrammarImpl.constantExpression);

    assertThat(p).matches("static_assert ( constantExpression , \"foo\" ) ;");
  }

  @Test
  public void declSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.declSpecifier));

    mockRule(CxxGrammarImpl.storageClassSpecifier);
    mockRule(CxxGrammarImpl.definingTypeSpecifier);
    mockRule(CxxGrammarImpl.functionSpecifier);

    assertThat(p).matches("storageClassSpecifier");
    assertThat(p).matches("definingTypeSpecifier");
    assertThat(p).matches("functionSpecifier");
    assertThat(p).matches("friend");
    assertThat(p).matches("typedef");
    assertThat(p).matches("constexpr");
    assertThat(p).matches("inline");
  }

  @Test
  public void declSpecifier_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.declSpecifier));

    assertThat(p).matches("register"); // a storage class
    assertThat(p).matches("friend"); // a function specifier
    assertThat(p).matches("void"); // a built-in type

    // declSpecifier
    assertThat(p).matches("friend");
    assertThat(p).matches("typedef");
    assertThat(p).matches("constexpr");
    assertThat(p).matches("inline");

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

    mockRule(CxxGrammarImpl.typeSpecifier);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

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
  public void definingTypeSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.definingTypeSpecifier));

    mockRule(CxxGrammarImpl.typeSpecifier);
    mockRule(CxxGrammarImpl.classSpecifier);
    mockRule(CxxGrammarImpl.enumSpecifier);

    assertThat(p).matches("typeSpecifier");
    assertThat(p).matches("classSpecifier");
    assertThat(p).matches("enumSpecifier");
  }

  @Test
  public void definingTypeSpecifierSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.definingTypeSpecifierSeq));

    mockRule(CxxGrammarImpl.definingTypeSpecifier);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThat(p).matches("definingTypeSpecifier");
    assertThat(p).matches("definingTypeSpecifier attributeSpecifierSeq");
    assertThat(p).matches("definingTypeSpecifier definingTypeSpecifier");
    assertThat(p).matches("definingTypeSpecifier attributeSpecifierSeq definingTypeSpecifier");
    assertThat(p).matches("definingTypeSpecifier attributeSpecifierSeq definingTypeSpecifier attributeSpecifierSeq");
  }

  @Test
  public void simpleTypeSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.simpleTypeSpecifier));

    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.simpleTemplateId);
    mockRule(CxxGrammarImpl.templateName);
    mockRule(CxxGrammarImpl.decltypeSpecifier);

    assertThat(p).matches("typeName");
    assertThat(p).matches("nestedNameSpecifier typeName");
    assertThat(p).matches("nestedNameSpecifier template simpleTemplateId");
    assertThat(p).matches("nestedNameSpecifier template simpleTemplateId");
    assertThat(p).matches("templateName");
    assertThat(p).matches("nestedNameSpecifier templateName");

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

    mockRule(CxxGrammarImpl.className);
    mockRule(CxxGrammarImpl.enumName);
    mockRule(CxxGrammarImpl.typedefName);
    mockRule(CxxGrammarImpl.simpleTemplateId);

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

    mockRule(CxxGrammarImpl.classKey);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.simpleTemplateId);

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

    mockRule(CxxGrammarImpl.enumHead);
    mockRule(CxxGrammarImpl.enumeratorList);
    mockRule(CxxGrammarImpl.cliAttribute);

    assertThat(p).matches("enumHead { }");
    assertThat(p).matches("enumHead { enumeratorList }");
    assertThat(p).matches("enumHead { enumeratorList , }");
  }

  @Test
  public void enumSpecifier_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumSpecifier));

    assertThat(p).matches("enum Color { red, green, blue }");
    assertThat(p).matches("enum Suit { Diamonds, Hearts, Clubs, Spades, }");
    assertThat(p).matches("enum Foo { a, b, c = 10, d, e = 1, f, g = f + c }");
    assertThat(p).matches("enum foo { MONDAY=1 }");
    assertThat(p).matches("enum class Color { red, green = 20, blue }");
    assertThat(p).matches("enum struct Color { red, green = 20, blue }");
    assertThat(p).matches("enum byte : unsigned char {}");

    assertThat(p).matches("enum altitude { high, low }");
    assertThat(p).matches("enum altitude { high, low, }");
    assertThat(p).matches("enum altitude { high='h', low='l' }");
    assertThat(p).matches("enum class altitude { high = 'h', low = 'l' }");
    assertThat(p).matches("enum struct altitude { high = 'h', low = 'l' }");
    assertThat(p).matches("enum altitude : char { high = 'h', low = 'l' }");
    assertThat(p).matches("enum class altitude : char { high = 'h', low = 'l' }");
    assertThat(p).matches("enum class altitude : char { high = 'h', low = 'l', }");
  }

  @Test
  public void enumHead() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumHead));

    mockRule(CxxGrammarImpl.enumKey);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.enumHeadName);
    mockRule(CxxGrammarImpl.enumBase);

    assertThat(p).matches("enumKey");
    assertThat(p).matches("enumKey attributeSpecifierSeq");
    assertThat(p).matches("enumKey enumHeadName");
    assertThat(p).matches("enumKey enumBase");
    assertThat(p).matches("enumKey attributeSpecifierSeq enumHeadName");
    assertThat(p).matches("enumKey enumHeadName enumBase");
    assertThat(p).matches("enumKey attributeSpecifierSeq enumBase");
    assertThat(p).matches("enumKey attributeSpecifierSeq enumHeadName enumBase");
  }

  @Test
  public void enumHeadName() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumHeadName));

    mockRule(CxxGrammarImpl.nestedNameSpecifier);

    assertThat(p).matches("IDENTIFIER");
    assertThat(p).matches("nestedNameSpecifier IDENTIFIER");
  }

  @Test
  public void opaqueEnumDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.opaqueEnumDeclaration));

    mockRule(CxxGrammarImpl.enumKey);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.enumBase);

    assertThat(p).matches("enumKey IDENTIFIER ;");
    assertThat(p).matches("enumKey attributeSpecifierSeq IDENTIFIER ;");
    assertThat(p).matches("enumKey attributeSpecifierSeq nestedNameSpecifier IDENTIFIER ;");
    assertThat(p).matches("enumKey attributeSpecifierSeq nestedNameSpecifier IDENTIFIER enumBase ;");
  }

  @Test
  public void enumKey() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumKey));

    assertThat(p).matches("enum");
    assertThat(p).matches("enum class");
    assertThat(p).matches("enum struct");
  }

  @Test
  public void enumBase() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumBase));
    mockRule(CxxGrammarImpl.typeSpecifierSeq);

    assertThat(p).matches(": typeSpecifierSeq");
  }

  @Test
  public void enumeratorList() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumeratorList));

    mockRule(CxxGrammarImpl.enumeratorDefinition);

    assertThat(p).matches("enumeratorDefinition");
    assertThat(p).matches("enumeratorDefinition , enumeratorDefinition");
  }

  @Test
  public void enumeratorDefinition() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumeratorDefinition));

    mockRule(CxxGrammarImpl.enumerator);
    mockRule(CxxGrammarImpl.constantExpression);

    assertThat(p).matches("enumerator");
    assertThat(p).matches("enumerator = constantExpression");
  }

  @Test
  public void enumerator() {
    p.setRootRule(g.rule(CxxGrammarImpl.enumerator));
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThat(p).matches("IDENTIFIER");
    assertThat(p).matches("IDENTIFIER attributeSpecifierSeq");
  }

  @Test
  public void namespaceDefinition() {
    p.setRootRule(g.rule(CxxGrammarImpl.namespaceDefinition));

    mockRule(CxxGrammarImpl.namedNamespaceDefinition);
    mockRule(CxxGrammarImpl.unnamedNamespaceDefinition);
    mockRule(CxxGrammarImpl.nestedNamespaceDefinition);

    assertThat(p)
      .matches("namedNamespaceDefinition")
      .matches("unnamedNamespaceDefinition")
      .matches("nestedNamespaceDefinition");
  }

  @Test
  public void enclosingNamespaceSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.enclosingNamespaceSpecifier));

    assertThat(p)
      .matches("IDENTIFIER")
      .matches("IDENTIFIER :: IDENTIFIER")
      .matches("IDENTIFIER :: IDENTIFIER :: IDENTIFIER");
  }

  @Test
  public void namespaceDefinition_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.namespaceDefinition));

    assertThat(p).matches("namespace MyLib { double readAndProcessSum (std::istream&); }");
    assertThat(p).matches("namespace A::B::C { int i; }");
  }

  @Test
  public void usingDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.usingDeclaration));
    mockRule(CxxGrammarImpl.usingDeclaratorList);

    assertThat(p).matches("using usingDeclaratorList ;");
  }

  @Test
  public void usingDeclaratorList() {
    p.setRootRule(g.rule(CxxGrammarImpl.usingDeclaratorList));
    mockRule(CxxGrammarImpl.usingDeclarator);

    assertThat(p).matches("usingDeclarator");
    assertThat(p).matches("usingDeclarator ...");
    assertThat(p).matches("usingDeclarator , usingDeclarator");
    assertThat(p).matches("usingDeclarator ... , usingDeclarator");
    assertThat(p).matches("usingDeclarator ... , usingDeclarator ...");
  }

  @Test
  public void usingDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.usingDeclarator));
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.unqualifiedId);

    assertThat(p).matches("nestedNameSpecifier unqualifiedId");
    assertThat(p).matches("typename nestedNameSpecifier unqualifiedId");
  }

  @Test
  public void usingDirective() {
    p.setRootRule(g.rule(CxxGrammarImpl.usingDirective));

    assertThat(p).matches("using namespace std;");
  }

  @Test
  public void linkageSpecification() {
    p.setRootRule(g.rule(CxxGrammarImpl.linkageSpecification));

    mockRule(CxxGrammarImpl.declaration);
    mockRule(CxxGrammarImpl.declarationSeq);

    assertThat(p).matches("extern \"foo\" { declarationSeq }");
    assertThat(p).matches("extern \"foo\" declaration");
  }

  @Test
  public void propertyDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.cliPropertyDefinition));

    mockRule(CxxGrammarImpl.cliAttributes);
    mockRule(CxxGrammarImpl.cliPropertyModifiers);
    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.cliPropertyIndexes);
    mockRule(CxxGrammarImpl.cliAccessorSpecification);

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

    mockRule(CxxGrammarImpl.cliAccessorDeclaration);
    mockRule(CxxGrammarImpl.accessSpecifier);

    assertThat(p).matches("");
    assertThat(p).matches("cliAccessorDeclaration");
    assertThat(p).matches("cliAccessorDeclaration cliAccessorDeclaration");
    assertThat(p).matches("accessSpecifier : cliAccessorDeclaration");
    assertThat(p).matches("accessSpecifier : cliAccessorDeclaration cliAccessorDeclaration");
  }

  @Test
  public void eventDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.cliEventDefinition));

    mockRule(CxxGrammarImpl.cliAttributes);
    mockRule(CxxGrammarImpl.cliEventModifiers);
    mockRule(CxxGrammarImpl.cliEventType);
    mockRule(CxxGrammarImpl.cliAccessorSpecification);

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

    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.templateId);

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

    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.cliAttributeTarget);
    mockRule(CxxGrammarImpl.cliPositionArgumentList);

    assertThat(p).matches("[ typeName ]");
    assertThat(p).matches("[ nestedNameSpecifier typeName ]");
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
    assertThat(p).matches("[System::Diagnostics::DebuggerNonUserCodeAttribute]");
    assertThat(p).matches("[System::CodeDom::Compiler::GeneratedCodeAttribute(L\"System.Data.Design.TypedDataSetGenerator\", L\"4.0.0.0\")]");
  }
}
