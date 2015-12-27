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

public class ClassesTest extends ParserBaseTest {

  @Test
  public void className_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.className));

    assertThat(p).matches("lala<int>");
  }

  @Test
  public void classSpecifier_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.classSpecifier));

    assertThat(p).matches("class foo final : bar { }");
    assertThat(p).matches("class foo final : bar { ; }");
    assertThat(p).matches("class foo final : bar { int foo(); }");
  }

  @Test
  public void classHead() {
    p.setRootRule(g.rule(CxxGrammarImpl.classHead));

    g.rule(CxxGrammarImpl.classKey).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.classHeadName).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.baseClause).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.classVirtSpecifier).mock(); //@toto deprecated

    assertThat(p).matches("classKey classHeadName");
    assertThat(p).matches("classKey attributeSpecifierSeq classHeadName");
    assertThat(p).matches("classKey attributeSpecifierSeq classHeadName classVirtSpecifier");
    assertThat(p).matches("classKey attributeSpecifierSeq classHeadName classVirtSpecifier baseClause");

    assertThat(p).matches("classKey");
    assertThat(p).matches("classKey attributeSpecifierSeq");
    assertThat(p).matches("classKey attributeSpecifierSeq baseClause");
  }

  @Test
  public void classHeadName() {
    p.setRootRule(g.rule(CxxGrammarImpl.classHeadName));

    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.className).mock(); //@toto deprecated

    assertThat(p).matches("className");
    assertThat(p).matches("nestedNameSpecifier className");
  }

  @Test
  public void memberSpecification() {
    p.setRootRule(g.rule(CxxGrammarImpl.memberSpecification));

    g.rule(CxxGrammarImpl.memberDeclaration).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.accessSpecifier).mock(); //@toto deprecated

    assertThat(p).matches("memberDeclaration");
    assertThat(p).matches("memberDeclaration accessSpecifier :");

    assertThat(p).matches("accessSpecifier :");
    assertThat(p).matches("accessSpecifier : memberDeclaration");
  }

  @Test
  public void memberSpecification_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.memberSpecification));

    assertThat(p).matches("int foo();");
    assertThat(p).matches("protected:");
    assertThat(p).matches("Result (*ptr)();");
    assertThat(p).matches("protected: Result (*ptr)();");
  }

  @Test
  public void memberDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.memberDeclaration));

    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.memberDeclSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.memberDeclaratorList).mock();
    g.rule(CxxGrammarImpl.functionDefinition).mock();
    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();
    g.rule(CxxGrammarImpl.unqualifiedId).mock();
    g.rule(CxxGrammarImpl.usingDeclaration).mock();
    g.rule(CxxGrammarImpl.staticAssertDeclaration).mock();
    g.rule(CxxGrammarImpl.templateDeclaration).mock();
    g.rule(CxxGrammarImpl.aliasDeclaration).mock();
    g.rule(CxxGrammarImpl.cliPropertyDefinition).mock();
    g.rule(CxxGrammarImpl.cliEventDefinition).mock();
    g.rule(CxxGrammarImpl.cliDelegateSpecifier).mock();
    g.rule(CxxGrammarImpl.cliGenericDeclaration).mock(); //@toto deprecated

    assertThat(p).matches(";");
    assertThat(p).matches("attributeSpecifierSeq memberDeclSpecifierSeq memberDeclaratorList ;");

    assertThat(p).matches("functionDefinition");
    assertThat(p).matches("functionDefinition ;");

    assertThat(p).matches("nestedNameSpecifier unqualifiedId ;");
    assertThat(p).matches(":: nestedNameSpecifier template unqualifiedId ;");

    assertThat(p).matches("usingDeclaration");
    assertThat(p).matches("staticAssertDeclaration");
    assertThat(p).matches("templateDeclaration");
    assertThat(p).matches("aliasDeclaration");

    assertThat(p).matches("cliPropertyDefinition");
    assertThat(p).matches("cliEventDefinition");
    assertThat(p).matches("cliDelegateSpecifier");
    assertThat(p).matches("cliGenericDeclaration");
  }

  @Test
  public void memberDeclaration_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.memberDeclaration));

    assertThat(p).matches("int foo();");
    assertThat(p).matches("int foo(){}");

    assertThat(p).matches("char tword[20];");
    assertThat(p).matches("int count;");
    assertThat(p).matches("tnode *left;");
    assertThat(p).matches("tnode *right;");
    assertThat(p).matches("Result (*ptr)();");
    assertThat(p).matches("A(const ::P& c) : m_value(c){};");
    assertThat(p).matches("void foo(::P& c) {};");

    assertThat(p).matches("property int Property_Block {int get();}");
    assertThat(p).matches("property int Property_Block {int get() {return MyInt;}}");
    assertThat(p).matches("property int^ Caption {void set(int ^value) {caption=value;}}");
    assertThat(p).matches("property int^ Caption {int ^get() {return caption;}}");
    assertThat(p).matches("property String^ Caption {int ^String() {return caption;}}");
    assertThat(p).matches("property System::String^ Caption {int ^String() {return caption;}}");

    assertThat(p).matches("event ClickEventHandler^ OnClick;");
    assertThat(p).matches("event MyDel^ E {raise() {pE->Invoke();}}");

    assertThat(p).matches("generic<typename T> ref class List {};");
    assertThat(p).matches("generic<typename T> ref class Queue : public List<T> {};");
    assertThat(p).matches("generic <typename ItemType> ref class Stack { void Add(ItemType item) {}};");
    assertThat(p).matches("generic <typename ItemType> ref struct Stack { void Add(ItemType item) {}};");

    assertThat(p).matches("delegate void MyDel();");
    assertThat(p).matches("public delegate void MyDel();");
    assertThat(p).matches("delegate void ClickEventHandler(int, double);");
    assertThat(p).matches("delegate void Del(int i);");
    assertThat(p).matches("public delegate void DblClickEventHandler(String^);");
  }

  ;

  @Test
  public void memberDeclaratorList() {
    p.setRootRule(g.rule(CxxGrammarImpl.memberDeclaratorList));

    g.rule(CxxGrammarImpl.memberDeclarator).mock();

    assertThat(p).matches("memberDeclarator");
    assertThat(p).matches("memberDeclarator , memberDeclarator");
  }

  @Test
  public void memberDeclaratorList_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.memberDeclaratorList));

    assertThat(p).matches("tword[20]");
  }

  @Test
  public void memberDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.memberDeclarator));

    g.rule(CxxGrammarImpl.declarator).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.pureSpecifier).mock();
    g.rule(CxxGrammarImpl.braceOrEqualInitializer).mock();
    g.rule(CxxGrammarImpl.constantExpression).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.virtSpecifierSeq).mock(); //@toto deprecated

    assertThat(p).matches("declarator");
    assertThat(p).matches("declarator virtSpecifierSeq");
    assertThat(p).matches("declarator virtSpecifierSeq pureSpecifier");

    assertThat(p).matches("declarator braceOrEqualInitializer");

    assertThat(p).matches(": constantExpression");
    assertThat(p).matches("foo : constantExpression");
    assertThat(p).matches("foo attributeSpecifierSeq : constantExpression");
  }

  @Test
  public void memberDeclarator_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.memberDeclarator));

    assertThat(p).matches("tword[20]");
    assertThat(p).matches("ThisAllocated : 1");
  }

  @Test
  public void virtSpecifierSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.virtSpecifierSeq));

    g.rule(CxxGrammarImpl.virtSpecifier).mock(); //@toto deprecated

    assertThat(p).matches("virtSpecifier");
    assertThat(p).matches("virtSpecifier virtSpecifier");
  }

  @Test
  public void virtSpecifierSeq_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.virtSpecifierSeq));

    assertThat(p).matches("override");
    assertThat(p).matches("final");
    assertThat(p).matches("override final");
    assertThat(p).matches("final override");
  }

  @Test
  public void cliFunctionModifier_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.cliFunctionModifier));

    assertThat(p).matches("abstract");
    assertThat(p).matches("new");
    assertThat(p).matches("sealed");
  }

  @Test
  public void virtSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.virtSpecifier));

    assertThat(p).matches("override");
    assertThat(p).matches("final");
  }

  @Test
  public void baseSpecifierList() {
    p.setRootRule(g.rule(CxxGrammarImpl.baseSpecifierList));

    g.rule(CxxGrammarImpl.baseSpecifier).mock(); //@toto deprecated

    assertThat(p).matches("baseSpecifier");
    assertThat(p).matches("baseSpecifier ...");

    assertThat(p).matches("baseSpecifier , baseSpecifier");
    assertThat(p).matches("baseSpecifier , baseSpecifier ...");
    assertThat(p).matches("baseSpecifier ..., baseSpecifier ...");
  }

  @Test
  public void baseSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.baseSpecifier));

    g.rule(CxxGrammarImpl.baseTypeSpecifier).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.accessSpecifier).mock(); //@toto deprecated

    assertThat(p).matches("baseTypeSpecifier");
    assertThat(p).matches("attributeSpecifierSeq baseTypeSpecifier");

    assertThat(p).matches("virtual baseTypeSpecifier");
    assertThat(p).matches("attributeSpecifierSeq virtual accessSpecifier baseTypeSpecifier");

    assertThat(p).matches("accessSpecifier baseTypeSpecifier");
    assertThat(p).matches("attributeSpecifierSeq accessSpecifier virtual baseTypeSpecifier");
  }

  @Test
  public void classOrDecltype() {
    p.setRootRule(g.rule(CxxGrammarImpl.classOrDecltype));

    g.rule(CxxGrammarImpl.className).mock(); //@toto deprecated
    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();
    g.rule(CxxGrammarImpl.decltypeSpecifier).mock(); //@toto deprecated

    assertThat(p).matches("className");
    assertThat(p).matches("nestedNameSpecifier className");
    assertThat(p).matches("decltypeSpecifier");
  }
}
