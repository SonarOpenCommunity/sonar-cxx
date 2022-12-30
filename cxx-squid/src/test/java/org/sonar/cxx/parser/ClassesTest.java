/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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

class ClassesTest extends ParserBaseTestHelper {

  @Test
  void className_reallife() {
    setRootRule(CxxGrammarImpl.className);

    assertThatParser()
      .matches("lala<int>");
  }

  @Test
  void classSpecifier_reallife() {
    setRootRule(CxxGrammarImpl.classSpecifier);

    assertThatParser()
      .matches("class foo final : bar { }")
      .matches("class foo final : bar { ; }")
      .matches("class foo final : bar { int foo(); }");
  }

  @Test
  void classHead() {
    setRootRule(CxxGrammarImpl.classHead);

    mockRule(CxxGrammarImpl.classKey);
    mockRule(CxxGrammarImpl.classHeadName);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.baseClause);
    mockRule(CxxGrammarImpl.classVirtSpecifier);

    assertThatParser()
      .matches("classKey classHeadName")
      .matches("classKey attributeSpecifierSeq classHeadName")
      .matches("classKey attributeSpecifierSeq classHeadName classVirtSpecifier")
      .matches("classKey attributeSpecifierSeq classHeadName classVirtSpecifier baseClause")
      .matches("classKey")
      .matches("classKey attributeSpecifierSeq")
      .matches("classKey attributeSpecifierSeq baseClause");
  }

  @Test
  void classHeadName() {
    setRootRule(CxxGrammarImpl.classHeadName);

    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.className);

    assertThatParser()
      .matches("className")
      .matches("nestedNameSpecifier className");
  }

  @Test
  void memberSpecification() {
    setRootRule(CxxGrammarImpl.memberSpecification);

    mockRule(CxxGrammarImpl.memberDeclaration);
    mockRule(CxxGrammarImpl.accessSpecifier);

    assertThatParser()
      .matches("memberDeclaration")
      .matches("memberDeclaration accessSpecifier :")
      .matches("accessSpecifier :")
      .matches("accessSpecifier : memberDeclaration");
  }

  @Test
  void memberSpecification_reallife() {
    setRootRule(CxxGrammarImpl.memberSpecification);

    assertThatParser()
      .matches("int foo();")
      .matches("protected:")
      .matches("Result (*ptr)();")
      .matches("protected: Result (*ptr)();");
  }

  @Test
  void memberDeclaration() {
    setRootRule(CxxGrammarImpl.memberDeclaration);

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.memberDeclSpecifierSeq);
    mockRule(CxxGrammarImpl.memberDeclaratorList);
    mockRule(CxxGrammarImpl.functionDefinition);
    mockRule(CxxGrammarImpl.usingDeclaration);
    mockRule(CxxGrammarImpl.usingEnumDeclaration);
    mockRule(CxxGrammarImpl.staticAssertDeclaration);
    mockRule(CxxGrammarImpl.templateDeclaration);
    mockRule(CxxGrammarImpl.explicitSpecialization);
    mockRule(CxxGrammarImpl.deductionGuide);
    mockRule(CxxGrammarImpl.aliasDeclaration);
    mockRule(CxxGrammarImpl.opaqueEnumDeclaration);
    mockRule(CxxGrammarImpl.emptyDeclaration);
    //----
    mockRule(CxxGrammarImpl.cliPropertyDefinition);
    mockRule(CxxGrammarImpl.cliEventDefinition);
    mockRule(CxxGrammarImpl.cliDelegateSpecifier);
    mockRule(CxxGrammarImpl.cliGenericDeclaration);

    assertThatParser()
      .matches(";")
      .matches("attributeSpecifierSeq ;")
      .matches("memberDeclSpecifierSeq ;")
      .matches("memberDeclaratorList ;")
      .matches("attributeSpecifierSeq memberDeclSpecifierSeq memberDeclaratorList ;")
      .matches("functionDefinition")
      .matches("usingDeclaration")
      .matches("usingEnumDeclaration")
      .matches("staticAssertDeclaration")
      .matches("templateDeclaration")
      .matches("explicitSpecialization")
      .matches("deductionGuide")
      .matches("aliasDeclaration")
      .matches("opaqueEnumDeclaration")
      .matches("emptyDeclaration")
      .matches("cliPropertyDefinition")
      .matches("cliEventDefinition")
      .matches("cliDelegateSpecifier")
      .matches("cliGenericDeclaration");
  }

  @Test
  void memberDeclaration_reallife() {
    setRootRule(CxxGrammarImpl.memberDeclaration);

    assertThatParser()
      .matches("int foo();")
      .matches("int foo(){}")
      .matches("char tword[20];")
      .matches("int count;")
      .matches("tnode *left;")
      .matches("tnode *right;")
      .matches("Result (*ptr)();")
      .matches("A(const ::P& c) : m_value(c){}")
      .matches("void foo(::P& c) {}")
      .matches("property int Property_Block {int get();}")
      .matches("property int Property_Block {int get() {return MyInt;}}")
      .matches("property int^ Caption {void set(int ^value) {caption=value;}}")
      .matches("property int^ Caption {int ^get() {return caption;}}")
      .matches("property String^ Caption {int ^String() {return caption;}}")
      .matches("property System::String^ Caption {int ^String() {return caption;}}")
      .matches("event ClickEventHandler^ OnClick;")
      .matches("event MyDel^ E {raise() {pE->Invoke();}}")
      .matches("generic<typename T> ref class List {};")
      .matches("generic<typename T> ref class Queue : public List<T> {};")
      .matches("generic <typename ItemType> ref class Stack { void Add(ItemType item) {}};")
      .matches("generic <typename ItemType> ref struct Stack { void Add(ItemType item) {}};")
      .matches("delegate void MyDel();")
      .matches("public delegate void MyDel();")
      .matches("delegate void ClickEventHandler(int, double);")
      .matches("delegate void Del(int i);")
      .matches("public delegate void DblClickEventHandler(String^);");
  }

  ;

  @Test
  void memberDeclaratorList() {
    setRootRule(CxxGrammarImpl.memberDeclaratorList);

    mockRule(CxxGrammarImpl.memberDeclarator);

    assertThatParser()
      .matches("memberDeclarator")
      .matches("memberDeclarator , memberDeclarator");
  }

  @Test
  void memberDeclaratorList_reallife() {
    setRootRule(CxxGrammarImpl.memberDeclaratorList);

    assertThatParser()
      .matches("tword[20]");
  }

  @Test
  void memberDeclarator() {
    setRootRule(CxxGrammarImpl.memberDeclarator);

    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.pureSpecifier);
    mockRule(CxxGrammarImpl.braceOrEqualInitializer);
    mockRule(CxxGrammarImpl.constantExpression);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.virtSpecifierSeq);
    mockRule(CxxGrammarImpl.requiresClause);

    assertThatParser()
      .matches("declarator")
      .matches("declarator virtSpecifierSeq")
      .matches("declarator virtSpecifierSeq pureSpecifier")
      .matches("declarator pureSpecifier")
      .matches("declarator requiresClause")
      .matches("declarator braceOrEqualInitializer")
      .matches(": constantExpression")
      .matches("foo : constantExpression")
      .matches("foo attributeSpecifierSeq : constantExpression")
      .matches(": constantExpression braceOrEqualInitializer")
      .matches("foo : constantExpression braceOrEqualInitializer")
      .matches("foo attributeSpecifierSeq : constantExpression braceOrEqualInitializer");
  }

  @Test
  void memberDeclarator_reallife() {
    setRootRule(CxxGrammarImpl.memberDeclarator);

    assertThatParser()
      .matches("tword[20]")
      .matches("ThisAllocated : 1");
  }

  @Test
  void virtSpecifierSeq() {
    setRootRule(CxxGrammarImpl.virtSpecifierSeq);

    mockRule(CxxGrammarImpl.virtSpecifier);

    assertThatParser()
      .matches("virtSpecifier")
      .matches("virtSpecifier virtSpecifier");
  }

  @Test
  void virtSpecifierSeq_reallife() {
    setRootRule(CxxGrammarImpl.virtSpecifierSeq);

    assertThatParser()
      .matches("override")
      .matches("final")
      .matches("override final")
      .matches("final override");
  }

  @Test
  void cliFunctionModifier_reallife() {
    setRootRule(CxxGrammarImpl.cliFunctionModifier);

    assertThatParser()
      .matches("abstract")
      .matches("new")
      .matches("sealed");
  }

  @Test
  void virtSpecifier() {
    setRootRule(CxxGrammarImpl.virtSpecifier);

    assertThatParser()
      .matches("override")
      .matches("final");
  }

  @Test
  void baseSpecifierList() {
    setRootRule(CxxGrammarImpl.baseSpecifierList);

    mockRule(CxxGrammarImpl.baseSpecifier);

    assertThatParser()
      .matches("baseSpecifier")
      .matches("baseSpecifier ...")
      .matches("baseSpecifier , baseSpecifier")
      .matches("baseSpecifier , baseSpecifier ...")
      .matches("baseSpecifier ..., baseSpecifier ...");
  }

  @Test
  void baseSpecifier() {
    setRootRule(CxxGrammarImpl.baseSpecifier);

    mockRule(CxxGrammarImpl.classOrDecltype);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.accessSpecifier);

    assertThatParser()
      .matches("classOrDecltype")
      .matches("attributeSpecifierSeq classOrDecltype")
      .matches("virtual classOrDecltype")
      .matches("attributeSpecifierSeq virtual accessSpecifier classOrDecltype")
      .matches("accessSpecifier classOrDecltype")
      .matches("attributeSpecifierSeq accessSpecifier virtual classOrDecltype");
  }

  @Test
  void classOrDecltype() {
    setRootRule(CxxGrammarImpl.classOrDecltype);

    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.simpleTemplateId);
    mockRule(CxxGrammarImpl.decltypeSpecifier);

    assertThatParser()
      .matches("typeName")
      .matches("nestedNameSpecifier typeName")
      .matches("template simpleTemplateId")
      .matches("nestedNameSpecifier template simpleTemplateId")
      .matches("decltypeSpecifier");
  }

}
