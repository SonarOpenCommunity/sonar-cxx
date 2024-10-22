/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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

import org.junit.jupiter.api.Test;

class DeclarationsTest extends ParserBaseTestHelper {

  @Test
  void declarationSeq() {
    setRootRule(CxxGrammarImpl.declarationSeq);

    mockRule(CxxGrammarImpl.declaration);

    assertThatParser()
      .matches("declaration")
      .matches("declaration declaration");
  }

  @Test
  void declaration() {
    setRootRule(CxxGrammarImpl.declaration);

    mockRule(CxxGrammarImpl.nameDeclaration);
    mockRule(CxxGrammarImpl.specialDeclaration);

    assertThatParser()
      .matches("nameDeclaration")
      .matches("specialDeclaration");
  }

  @Test
  void nameDeclaration() {
    setRootRule(CxxGrammarImpl.nameDeclaration);

    mockRule(CxxGrammarImpl.blockDeclaration);
    mockRule(CxxGrammarImpl.nodeclspecFunctionDeclaration);
    mockRule(CxxGrammarImpl.functionDefinition);
    mockRule(CxxGrammarImpl.templateDeclaration);
    mockRule(CxxGrammarImpl.deductionGuide);
    mockRule(CxxGrammarImpl.linkageSpecification);
    mockRule(CxxGrammarImpl.namespaceDefinition);
    mockRule(CxxGrammarImpl.emptyDeclaration);
    mockRule(CxxGrammarImpl.attributeDeclaration);

    assertThatParser()
      .matches("blockDeclaration")
      .matches("nodeclspecFunctionDeclaration")
      .matches("functionDefinition")
      .matches("templateDeclaration")
      .matches("deductionGuide")
      .matches("linkageSpecification")
      .matches("namespaceDefinition")
      .matches("emptyDeclaration")
      .matches("attributeDeclaration");
  }

  @Test
  void specialDeclaration() {
    setRootRule(CxxGrammarImpl.specialDeclaration);

    mockRule(CxxGrammarImpl.cliGenericDeclaration);
    mockRule(CxxGrammarImpl.explicitInstantiation);
    mockRule(CxxGrammarImpl.explicitSpecialization);
    mockRule(CxxGrammarImpl.exportDeclaration);
    mockRule(CxxGrammarImpl.vcAtlDeclaration);

    assertThatParser()
      .matches("cliGenericDeclaration")
      .matches("explicitInstantiation")
      .matches("explicitSpecialization")
      .matches("exportDeclaration")
      .matches("vcAtlDeclaration");
  }

  @Test
  void declarationReallife() {
    setRootRule(CxxGrammarImpl.declaration);

    assertThatParser()
      .matches(";")
      .matches("extern const int c;")
      .matches("extern const int c;")
      .matches("int f(int);")
      .matches("struct S;")
      .matches("typedef int Int;")
      .matches("extern X anotherX;")
      .matches("[[noreturn]] void f [[noreturn]] ();")
      .matches("using std::string;")
      .matches("using namespace D;")
      .matches("enum Color { red, green, blue };")
      .matches("static_assert(std::is_copy_constructible<T>::value, \"Swap requires copying\");")
      .matches("t* pt = nullptr;")
      .matches("t* pt {nullptr};")
      .matches("t* pt {nullptr};")
      .matches("int i = 0;")
      .matches("sometype foo();")
      .matches("sometype (*foo)(void);")
      .matches("sometype (*foo)(void);")
      .matches("aligned_storage<sizeof(result_type)> cache;")
      .matches("mpl<N/M>();")
      .matches("bool operator==<B>(A const&, A const&);")
      .matches("auto foo(int& var1) -> int;")
      .matches("auto foo(int& var1) -> int;")
      .matches("auto fp11() -> void(*)(const std::string&);")
      .matches("t% pt;")
      .matches("t% pt;")
      .matches("int property;")
      .matches("int property = 0;")
      .matches("int property = 0;");
  }

  @Test
  void aliasDeclaration() {
    setRootRule(CxxGrammarImpl.aliasDeclaration);

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.definingTypeId);

    assertThatParser()
      .matches("using foo = definingTypeId ;")
      .matches("using foo attributeSpecifierSeq = definingTypeId ;");
  }

  @Test
  void simpleDeclaration() {
    setRootRule(CxxGrammarImpl.simpleDeclaration);

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.declSpecifierSeq);
    mockRule(CxxGrammarImpl.initDeclaratorList);
    mockRule(CxxGrammarImpl.refQualifier);
    mockRule(CxxGrammarImpl.identifierList);
    mockRule(CxxGrammarImpl.initializer);

    assertThatParser()
      .matches("initDeclaratorList ;")
      .matches("declSpecifierSeq ;")
      .matches("declSpecifierSeq initDeclaratorList ;")
      .matches("attributeSpecifierSeq initDeclaratorList ;")
      .matches("attributeSpecifierSeq declSpecifierSeq initDeclaratorList ;")
      .matches("declSpecifierSeq [ identifierList ] initializer ;")
      .matches("attributeSpecifierSeq declSpecifierSeq [ identifierList ] initializer ;")
      .matches("declSpecifierSeq refQualifier [ identifierList ] initializer ;")
      .matches("attributeSpecifierSeq declSpecifierSeq refQualifier [ identifierList ] initializer ;");
  }

  @Test
  void simpleDeclarationReallife() {
    setRootRule(CxxGrammarImpl.simpleDeclaration);

    assertThatParser()
      .matches("sometype foo();")
      .matches("const auto_ptr<int> p(new int(42));")
      .matches("list<string>::iterator pos1, pos2;")
      .matches("vector<string> coll((istream_iterator<string>(cin)), istream_iterator<string>());")
      .matches("carray<int,10> a;")
      .matches("void foo(string, bool);")
      .matches("friend class ::SMLCGroupHierarchyImpl;")
      .matches("void foo(int, type[]);")
      .matches("bool operator==<B>(A const&, A const&);")
      .matches("int foo();")
      .matches("const int foo();")
      .matches("int* foo();")
      .matches("const int* foo();")
      .matches("int& foo();")
      .matches("const int& foo();")
      .matches("long long foo();")
      .matches("const long long foo();")
      .matches("long long* foo();")
      .matches("const long long* foo();")
      .matches("long long& foo();")
      .matches("const long long& foo();")
      .matches("MyClass foo();")
      .matches("const MyClass foo();")
      .matches("MyClass* foo();")
      .matches("const MyClass* foo();")
      .matches("MyClass* foo();")
      .matches("const MyClass* foo();")
      .matches("extern int foo();")
      .matches("auto to_string(int value) -> int;")
      .matches("auto to_string(int value) -> long long;")
      .matches("auto to_string(int value) -> int;")
      .matches("auto size() const -> std::size_t;")
      .matches("auto str() const;")
      .matches(
        "auto equal_range(ForwardIterator first, ForwardIterator last, const Type& value) -> std::pair<ForwardIterator, ForwardIterator>;")
      .matches("auto str() const -> const char*;")
      .matches("auto std::map::at(const key_type& key) -> mapped_type&;")
      .matches("auto f() -> int(*)[4];")
      .matches("auto fpif(int) -> int(*)(int);")
      .matches("auto[a, b] = f();");
  }

  @Test
  void staticAssertDeclaration() {
    setRootRule(CxxGrammarImpl.staticAssertDeclaration);

    mockRule(CxxGrammarImpl.constantExpression);

    assertThatParser()
      .matches("static_assert ( constantExpression , \"foo\" ) ;");
  }

  @Test
  void declSpecifier() {
    setRootRule(CxxGrammarImpl.declSpecifier);

    mockRule(CxxGrammarImpl.storageClassSpecifier);
    mockRule(CxxGrammarImpl.definingTypeSpecifier);
    mockRule(CxxGrammarImpl.functionSpecifier);

    assertThatParser()
      .matches("storageClassSpecifier")
      .matches("definingTypeSpecifier")
      .matches("functionSpecifier")
      .matches("friend")
      .matches("typedef")
      .matches("constexpr")
      .matches("consteval")
      .matches("constinit")
      .matches("inline");
  }

  @Test
  void declSpecifierReallife() {
    setRootRule(CxxGrammarImpl.declSpecifier);

    assertThatParser()
      .matches("friend") // a function specifier
      .matches("void") // a built-in type
      // declSpecifier
      .matches("friend")
      .matches("typedef")
      .matches("constexpr")
      .matches("consteval")
      .matches("constinit")
      .matches("inline")
      // enum specifier
      .matches("enum foo { MONDAY=1 }")
      // class specifier
      .matches("class foo final : bar { }")
      .matches("class foo final : bar { int foo(); }")
      .matches("class foo final : public ::bar { int foo(); }")
      // CLI extension
      .matches("class foo sealed : bar { }")
      .matches("ref class foo : bar { }")
      .matches("public ref class foo : bar { }")
      .matches("private ref class foo : bar { }")
      // type names
      .matches("class_foo") // className->identifier
      .matches("class_foo<bar>") // className->simpleTemplateId
      .matches("enum_foo") // enumName->identifier
      .matches("typedef_foo") // typedefName->identifier
      .matches("foo<bar>")
      .matches("paramtype<T>")
      .matches("carray<int,10>")
      .matches("::P");
  }

  @Test
  void typeSpecifierReallife() {
    setRootRule(CxxGrammarImpl.typeSpecifier);

    assertThatParser()
      .matches("enum foo { MONDAY=1 }")
      .matches("carray<int,10>");
  }

  @Test
  void typeSpecifierSeq() {
    setRootRule(CxxGrammarImpl.typeSpecifierSeq);

    mockRule(CxxGrammarImpl.typeSpecifier);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThatParser()
      .matches("typeSpecifier")
      .matches("typeSpecifier attributeSpecifierSeq")
      .matches("typeSpecifier typeSpecifier")
      .matches("typeSpecifier typeSpecifier attributeSpecifierSeq");
  }

  @Test
  void typeSpecifierSeqReallife() {
    setRootRule(CxxGrammarImpl.typeSpecifierSeq);

    assertThatParser()
      .matches("templatetype<T>")
      .matches("templatetype<T> int");
  }

  @Test
  void definingTypeSpecifier() {
    setRootRule(CxxGrammarImpl.definingTypeSpecifier);

    mockRule(CxxGrammarImpl.typeSpecifier);
    mockRule(CxxGrammarImpl.classSpecifier);
    mockRule(CxxGrammarImpl.enumSpecifier);

    assertThatParser()
      .matches("typeSpecifier")
      .matches("classSpecifier")
      .matches("enumSpecifier");
  }

  @Test
  void definingTypeSpecifierSeq() {
    setRootRule(CxxGrammarImpl.definingTypeSpecifierSeq);

    mockRule(CxxGrammarImpl.definingTypeSpecifier);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThatParser()
      .matches("definingTypeSpecifier")
      .matches("definingTypeSpecifier attributeSpecifierSeq")
      .matches("definingTypeSpecifier definingTypeSpecifier")
      .matches("definingTypeSpecifier definingTypeSpecifier attributeSpecifierSeq");
  }

  @Test
  void simpleTypeSpecifier() {
    setRootRule(CxxGrammarImpl.simpleTypeSpecifier);

    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.simpleTemplateId);
    mockRule(CxxGrammarImpl.placeholderTypeSpecifier);
    mockRule(CxxGrammarImpl.templateName);
    mockRule(CxxGrammarImpl.decltypeSpecifier);

    assertThatParser()
      .matches("typeName")
      .matches("nestedNameSpecifier typeName")
      .matches("nestedNameSpecifier template simpleTemplateId")
      .matches("nestedNameSpecifier template simpleTemplateId")
      .matches("placeholderTypeSpecifier")
      .matches("templateName")
      .matches("nestedNameSpecifier templateName")
      .matches("char")
      .matches("char8_t")
      .matches("char16_t")
      .matches("char32_t")
      .matches("wchar_t")
      .matches("bool")
      .matches("short")
      .matches("int")
      .matches("long")
      .matches("signed")
      .matches("unsigned")
      .matches("float")
      .matches("double")
      .matches("void")
      .matches("decltypeSpecifier");
  }

  @Test
  void simpleTypeSpecifierReallife() {
    setRootRule(CxxGrammarImpl.simpleTypeSpecifier);

    assertThatParser()
      .matches("::P");
  }

  @Test
  void typeName() {
    setRootRule(CxxGrammarImpl.typeName);

    mockRule(CxxGrammarImpl.className);
    mockRule(CxxGrammarImpl.enumName);
    mockRule(CxxGrammarImpl.typedefName);

    assertThatParser()
      .matches("className")
      .matches("enumName")
      .matches("typedefName");
  }

  @Test
  void typeNameReallife() {
    setRootRule(CxxGrammarImpl.typeName);

    assertThatParser()
      .matches("sometype<int>");
  }

  @Test
  void decltypeSpecifier() {
    setRootRule(CxxGrammarImpl.decltypeSpecifier);

    mockRule(CxxGrammarImpl.expression);

    assertThatParser()
      .matches("decltype ( expression )")
      .matches("decltype ( auto )");
  }

  @Test
  void placeholderTypeSpecifier() {
    setRootRule(CxxGrammarImpl.placeholderTypeSpecifier);

    mockRule(CxxGrammarImpl.className);

    assertThatParser()
      .matches("auto")
      .matches("typeConstraint auto")
      .matches("decltype ( auto )")
      .matches("typeConstraint decltype ( auto )");
  }

  @Test
  void elaboratedTypeSpecifier() {
    setRootRule(CxxGrammarImpl.elaboratedTypeSpecifier);

    mockRule(CxxGrammarImpl.classKey);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.simpleTemplateId);

    assertThatParser()
      .matches("classKey foo")
      .matches("classKey attributeSpecifierSeq foo")
      .matches("classKey nestedNameSpecifier foo")
      .matches("classKey attributeSpecifierSeq nestedNameSpecifier foo")
      .matches("classKey simpleTemplateId")
      .matches("classKey nestedNameSpecifier simpleTemplateId")
      .matches("classKey nestedNameSpecifier template simpleTemplateId")
      .matches("enum foo")
      .matches("enum nestedNameSpecifier foo");
  }

  @Test
  void elaboratedTypeSpecifierReallife() {
    setRootRule(CxxGrammarImpl.elaboratedTypeSpecifier);

    assertThatParser()
      .matches("class ::A");
  }

  @Test
  void enumSpecifier() {
    setRootRule(CxxGrammarImpl.enumSpecifier);

    mockRule(CxxGrammarImpl.enumHead);
    mockRule(CxxGrammarImpl.enumeratorList);
    mockRule(CxxGrammarImpl.cliAttribute);

    assertThatParser()
      .matches("enumHead { }")
      .matches("enumHead { enumeratorList }")
      .matches("enumHead { enumeratorList , }");
  }

  @Test
  void enumSpecifierReallife() {
    setRootRule(CxxGrammarImpl.enumSpecifier);

    assertThatParser()
      .matches("enum Color { red, green, blue }")
      .matches("enum Suit { Diamonds, Hearts, Clubs, Spades, }")
      .matches("enum Foo { a, b, c = 10, d, e = 1, f, g = f + c }")
      .matches("enum foo { MONDAY=1 }")
      .matches("enum class Color { red, green = 20, blue }")
      .matches("enum struct Color { red, green = 20, blue }")
      .matches("enum byte : unsigned char {}")
      .matches("enum altitude { high, low }")
      .matches("enum altitude { high, low, }")
      .matches("enum altitude { high='h', low='l' }")
      .matches("enum class altitude { high = 'h', low = 'l' }")
      .matches("enum struct altitude { high = 'h', low = 'l' }")
      .matches("enum altitude : char { high = 'h', low = 'l' }")
      .matches("enum class altitude : char { high = 'h', low = 'l' }")
      .matches("enum class altitude : char { high = 'h', low = 'l', }");
  }

  @Test
  void enumHead() {
    setRootRule(CxxGrammarImpl.enumHead);

    mockRule(CxxGrammarImpl.enumKey);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.enumHeadName);
    mockRule(CxxGrammarImpl.enumBase);

    assertThatParser()
      .matches("enumKey")
      .matches("enumKey attributeSpecifierSeq")
      .matches("enumKey enumHeadName")
      .matches("enumKey enumBase")
      .matches("enumKey attributeSpecifierSeq enumHeadName")
      .matches("enumKey enumHeadName enumBase")
      .matches("enumKey attributeSpecifierSeq enumBase")
      .matches("enumKey attributeSpecifierSeq enumHeadName enumBase");
  }

  @Test
  void enumHeadName() {
    setRootRule(CxxGrammarImpl.enumHeadName);

    mockRule(CxxGrammarImpl.nestedNameSpecifier);

    assertThatParser()
      .matches("IDENTIFIER")
      .matches("nestedNameSpecifier IDENTIFIER");
  }

  @Test
  void opaqueEnumDeclaration() {
    setRootRule(CxxGrammarImpl.opaqueEnumDeclaration);

    mockRule(CxxGrammarImpl.enumKey);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.enumHeadName);
    mockRule(CxxGrammarImpl.enumBase);

    assertThatParser()
      .matches("enumKey enumHeadName ;")
      .matches("enumKey enumHeadName enumBase ;")
      .matches("enumKey attributeSpecifierSeq enumHeadName ;")
      .matches("enumKey attributeSpecifierSeq enumHeadName enumBase ;");
  }

  @Test
  void enumKey() {
    setRootRule(CxxGrammarImpl.enumKey);

    assertThatParser()
      .matches("enum")
      .matches("enum class")
      .matches("enum struct");
  }

  @Test
  void enumBase() {
    setRootRule(CxxGrammarImpl.enumBase);
    mockRule(CxxGrammarImpl.typeSpecifierSeq);

    assertThatParser()
      .matches(": typeSpecifierSeq");
  }

  @Test
  void enumeratorList() {
    setRootRule(CxxGrammarImpl.enumeratorList);

    mockRule(CxxGrammarImpl.enumeratorDefinition);

    assertThatParser()
      .matches("enumeratorDefinition")
      .matches("enumeratorDefinition , enumeratorDefinition");
  }

  @Test
  void enumeratorDefinition() {
    setRootRule(CxxGrammarImpl.enumeratorDefinition);

    mockRule(CxxGrammarImpl.enumerator);
    mockRule(CxxGrammarImpl.constantExpression);

    assertThatParser()
      .matches("enumerator")
      .matches("enumerator = constantExpression");
  }

  @Test
  void enumerator() {
    setRootRule(CxxGrammarImpl.enumerator);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThatParser()
      .matches("IDENTIFIER")
      .matches("IDENTIFIER attributeSpecifierSeq");
  }

  @Test
  void namespaceDefinition() {
    setRootRule(CxxGrammarImpl.namespaceDefinition);

    mockRule(CxxGrammarImpl.namedNamespaceDefinition);
    mockRule(CxxGrammarImpl.unnamedNamespaceDefinition);
    mockRule(CxxGrammarImpl.nestedNamespaceDefinition);

    assertThatParser()
      .matches("namedNamespaceDefinition")
      .matches("unnamedNamespaceDefinition")
      .matches("nestedNamespaceDefinition");
  }

  @Test
  void enclosingNamespaceSpecifier() {
    setRootRule(CxxGrammarImpl.enclosingNamespaceSpecifier);

    assertThatParser()
      .matches("IDENTIFIER")
      .matches("IDENTIFIER :: IDENTIFIER")
      .matches("IDENTIFIER :: IDENTIFIER :: IDENTIFIER")
      .matches("IDENTIFIER :: inline IDENTIFIER")
      .matches("IDENTIFIER :: inline IDENTIFIER :: inline IDENTIFIER");
  }

  @Test
  void namespaceDefinitionReallife() {
    setRootRule(CxxGrammarImpl.namespaceDefinition);

    assertThatParser()
      .matches("namespace MyLib { double readAndProcessSum (std::istream&); }")
      .matches("namespace A::B::C { int i; }")
      .matches("namespace A::B::inline C { /*...*/ }")
      .matches("namespace A::inline B::C {}");
  }

  @Test
  void usingDeclaration() {
    setRootRule(CxxGrammarImpl.usingDeclaration);
    mockRule(CxxGrammarImpl.usingDeclaratorList);

    assertThatParser()
      .matches("using usingDeclaratorList ;");
  }

  @Test
  void usingDeclaratorList() {
    setRootRule(CxxGrammarImpl.usingDeclaratorList);
    mockRule(CxxGrammarImpl.usingDeclarator);

    assertThatParser()
      .matches("usingDeclarator")
      .matches("usingDeclarator ...")
      .matches("usingDeclarator , usingDeclarator")
      .matches("usingDeclarator ... , usingDeclarator")
      .matches("usingDeclarator ... , usingDeclarator ...");
  }

  @Test
  void usingDeclarator() {
    setRootRule(CxxGrammarImpl.usingDeclarator);

    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.unqualifiedId);

    assertThatParser()
      .matches("nestedNameSpecifier unqualifiedId")
      .matches("typename nestedNameSpecifier unqualifiedId");
  }

  @Test
  void usingDirective() {
    setRootRule(CxxGrammarImpl.usingDirective);

    assertThatParser()
      .matches("using namespace std;");
  }

  @Test
  void linkageSpecification() {
    setRootRule(CxxGrammarImpl.linkageSpecification);

    mockRule(CxxGrammarImpl.nameDeclaration);
    mockRule(CxxGrammarImpl.declarationSeq);

    assertThatParser()
      .matches("extern \"foo\" { declarationSeq }")
      .matches("extern \"foo\" nameDeclaration");
  }

  @Test
  void propertyDeclaration() {
    setRootRule(CxxGrammarImpl.cliPropertyDefinition);

    mockRule(CxxGrammarImpl.cliAttributes);
    mockRule(CxxGrammarImpl.cliPropertyModifiers);
    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.cliPropertyIndexes);
    mockRule(CxxGrammarImpl.cliAccessorSpecification);

    assertThatParser()
      .matches("property typeSpecifier declarator ;")
      .matches("cliPropertyModifiers property typeSpecifier declarator ;")
      .matches("cliAttributes cliPropertyModifiers property typeSpecifier declarator ;")
      .matches("cliAttributes cliPropertyModifiers property cliPropertyOrEventName declarator ;")
      .matches(
        "cliAttributes cliPropertyModifiers property nestedNameSpecifier cliPropertyOrEventName declarator ;")
      .matches("property typeSpecifier declarator { cliAccessorSpecification }")
      .matches("cliPropertyModifiers property typeSpecifier declarator { cliAccessorSpecification }")
      .matches(
        "cliPropertyModifiers property typeSpecifier declarator cliPropertyIndexes { cliAccessorSpecification }")
      .matches(
        "cliAttributes cliPropertyModifiers property typeSpecifier declarator { cliAccessorSpecification }")
      .matches(
        "cliAttributes cliPropertyModifiers property cliPropertyOrEventName declarator { cliAccessorSpecification }")
      .matches(
        "cliAttributes cliPropertyModifiers property nestedNameSpecifier cliPropertyOrEventName declarator { cliAccessorSpecification }")
      .matches(
        "cliAttributes cliPropertyModifiers property typeSpecifier declarator cliPropertyIndexes { cliAccessorSpecification }")
      .matches(
        "cliAttributes cliPropertyModifiers property nestedNameSpecifier cliPropertyOrEventName declarator cliPropertyIndexes { cliAccessorSpecification }");
  }

  @Test
  void accessorSpecificationDeclaration() {
    setRootRule(CxxGrammarImpl.cliAccessorSpecification);

    mockRule(CxxGrammarImpl.cliAccessorDeclaration);
    mockRule(CxxGrammarImpl.accessSpecifier);

    assertThatParser()
      .matches("")
      .matches("cliAccessorDeclaration")
      .matches("cliAccessorDeclaration cliAccessorDeclaration")
      .matches("accessSpecifier : cliAccessorDeclaration")
      .matches("accessSpecifier : cliAccessorDeclaration cliAccessorDeclaration");
  }

  @Test
  void eventDeclaration() {
    setRootRule(CxxGrammarImpl.cliEventDefinition);

    mockRule(CxxGrammarImpl.cliAttributes);
    mockRule(CxxGrammarImpl.cliEventModifiers);
    mockRule(CxxGrammarImpl.cliEventType);
    mockRule(CxxGrammarImpl.cliAccessorSpecification);

    assertThatParser()
      .matches("event cliEventType myEvent ;")
      .matches("cliEventModifiers event cliEventType myEvent ;")
      .matches("cliAttributes cliEventModifiers event cliEventType myEvent ;")
      .matches("event cliEventType myEvent { cliAccessorSpecification }")
      .matches("cliEventModifiers event cliEventType myEvent { cliAccessorSpecification }")
      .matches("cliAttributes cliEventModifiers event cliEventType myEvent { cliAccessorSpecification }");
  }

  @Test
  void eventTypeDeclaration() {
    setRootRule(CxxGrammarImpl.cliEventType);

    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.templateId);

    assertThatParser()
      .matches("nestedNameSpecifier typeName")
      .matches(":: nestedNameSpecifier typeName")
      .matches("nestedNameSpecifier typeName^")
      .matches(":: nestedNameSpecifier typeName^")
      .matches("nestedNameSpecifier template templateId ^")
      .matches(":: nestedNameSpecifier template templateId ^");
  }

  @Test
  void cliAttributesDefinition() {
    setRootRule(CxxGrammarImpl.cliAttributes);

    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.cliAttributeTarget);
    mockRule(CxxGrammarImpl.cliPositionArgumentList);

    assertThatParser()
      .matches("[ typeName ]")
      .matches("[ nestedNameSpecifier typeName ]")
      .matches("[ cliAttributeTarget : typeName ]")
      .matches("[ cliAttributeTarget : typeName, typeName ]")
      .matches("[ cliAttributeTarget : typeName, typeName (cliPositionArgumentList) ]");

  }

  @Test
  void cliAttributesDefinitionReallife() {
    setRootRule(CxxGrammarImpl.cliAttributes);

    assertThatParser()
      .matches("[Attr]")
      .matches("[assembly:Attr]")
      .matches("[class:Attr]")
      .matches("[constructor:Attr]")
      .matches("[assembly:Attr]")
      .matches("[delegate:Attr]")
      .matches("[enum:Attr]")
      .matches("[event:Attr]")
      .matches("[field:Attr]")
      .matches("[interface:Attr]")
      .matches("[method:Attr]")
      .matches("[parameter:Attr]")
      .matches("[property:Attr]")
      .matches("[returnvalue:Attr]")
      .matches("[struct:Attr]")
      .matches("[returnvalue:Attr(x)]")
      .matches("[AttributeUsage( AttributeTargets::All )]")
      .matches("[AttributeUsage(AttributeTargets::Class | AttributeTargets::Method)]")
      .matches(
        "[ AnotherAttr( gcnew array<Object ^> { 3.14159, \"pi\" }, var1 = gcnew array<Object ^> { \"a\", \"b\" } ) ]")
      .matches("[System::Diagnostics::DebuggerNonUserCodeAttribute]")
      .matches(
        "[System::CodeDom::Compiler::GeneratedCodeAttribute(L\"System.Data.Design.TypedDataSetGenerator\", L\"4.0.0.0\")]");
  }

}
