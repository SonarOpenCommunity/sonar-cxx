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
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ClassesTest {

  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();

  @Test
  public void className_reallife() {
    p.setRootRule(g.className);

    assertThat(p, parse("lala<int>"));
  }

  @Test
  public void classSpecifier_reallife() {
    p.setRootRule(g.classSpecifier);

    assertThat(p, parse("class foo final : bar { }"));
    assertThat(p, parse("class foo final : bar { ; }"));
    assertThat(p, parse("class foo final : bar { int foo(); }"));
  }

  @Test
  public void classHead() {
    p.setRootRule(g.classHead);

    g.classKey.mock();
    g.classHeadName.mock();
    g.attributeSpecifierSeq.mock();
    g.baseClause.mock();
    g.classVirtSpecifier.mock();

    assertThat(p, parse("classKey classHeadName"));
    assertThat(p, parse("classKey attributeSpecifierSeq classHeadName"));
    assertThat(p, parse("classKey attributeSpecifierSeq classHeadName classVirtSpecifier"));
    assertThat(p, parse("classKey attributeSpecifierSeq classHeadName classVirtSpecifier baseClause"));

    assertThat(p, parse("classKey"));
    assertThat(p, parse("classKey attributeSpecifierSeq"));
    assertThat(p, parse("classKey attributeSpecifierSeq baseClause"));
  }

  @Test
  public void classHeadName() {
    p.setRootRule(g.classHeadName);

    g.nestedNameSpecifier.mock();
    g.className.mock();

    assertThat(p, parse("className"));
    assertThat(p, parse("nestedNameSpecifier className"));
  }

  @Test
  public void memberSpecification() {
    p.setRootRule(g.memberSpecification);

    g.memberDeclaration.mock();
    g.accessSpecifier.mock();

    assertThat(p, parse("memberDeclaration"));
    assertThat(p, parse("memberDeclaration accessSpecifier :"));

    assertThat(p, parse("accessSpecifier :"));
    assertThat(p, parse("accessSpecifier : memberDeclaration"));
  }

  @Test
  public void memberSpecification_reallife() {
    p.setRootRule(g.memberSpecification);

    assertThat(p, parse("int foo();"));
    assertThat(p, parse("protected:"));
    assertThat(p, parse("Result (*ptr)();"));
    assertThat(p, parse("protected: Result (*ptr)();"));
  }

  @Test
  public void memberDeclaration() {
    p.setRootRule(g.memberDeclaration);

    g.attributeSpecifierSeq.mock();
    g.memberDeclSpecifierSeq.mock();
    g.memberDeclaratorList.mock();
    g.functionDefinition.mock();
    g.nestedNameSpecifier.mock();
    g.unqualifiedId.mock();
    g.usingDeclaration.mock();
    g.staticAssertDeclaration.mock();
    g.templateDeclaration.mock();
    g.aliasDeclaration.mock();

    assertThat(p, parse(";"));
    assertThat(p, parse("attributeSpecifierSeq memberDeclSpecifierSeq memberDeclaratorList ;"));

    assertThat(p, parse("functionDefinition"));
    assertThat(p, parse("functionDefinition ;"));

    assertThat(p, parse("nestedNameSpecifier unqualifiedId ;"));
    assertThat(p, parse(":: nestedNameSpecifier template unqualifiedId ;"));

    assertThat(p, parse("usingDeclaration"));
    assertThat(p, parse("staticAssertDeclaration"));
    assertThat(p, parse("templateDeclaration"));
    assertThat(p, parse("aliasDeclaration"));
  }

  @Test
  public void memberDeclaration_reallife() {
    p.setRootRule(g.memberDeclaration);

    assertThat(p, parse("int foo();"));
    assertThat(p, parse("int foo(){}"));

    assertThat(p, parse("char tword[20];"));
    assertThat(p, parse("int count;"));
    assertThat(p, parse("tnode *left;"));
    assertThat(p, parse("tnode *right;"));
    assertThat(p, parse("Result (*ptr)();"));
    assertThat(p, parse("A(const ::P& c) : m_value(c){};"));
    assertThat(p, parse("void foo(::P& c) {};"));
  }

  @Test
  public void memberDeclaratorList() {
    p.setRootRule(g.memberDeclaratorList);

    g.memberDeclarator.mock();

    assertThat(p, parse("memberDeclarator"));
    assertThat(p, parse("memberDeclarator , memberDeclarator"));
  }

  @Test
  public void memberDeclaratorList_reallife() {
    p.setRootRule(g.memberDeclaratorList);

    assertThat(p, parse("tword[20]"));
  }

  @Test
  public void memberDeclarator() {
    p.setRootRule(g.memberDeclarator);

    g.declarator.mock();
    g.pureSpecifier.mock();
    g.braceOrEqualInitializer.mock();
    g.constantExpression.mock();
    g.attributeSpecifierSeq.mock();
    g.virtSpecifierSeq.mock();

    assertThat(p, parse("declarator"));
    assertThat(p, parse("declarator virtSpecifierSeq"));
    assertThat(p, parse("declarator virtSpecifierSeq pureSpecifier"));

    assertThat(p, parse("declarator braceOrEqualInitializer"));

    assertThat(p, parse(": constantExpression"));
    assertThat(p, parse("foo : constantExpression"));
    assertThat(p, parse("foo attributeSpecifierSeq : constantExpression"));
  }

  @Test
  public void memberDeclarator_reallife() {
    p.setRootRule(g.memberDeclarator);

    assertThat(p, parse("tword[20]"));
    assertThat(p, parse("ThisAllocated : 1"));
  }

  @Test
  public void virtSpecifierSeq() {
    p.setRootRule(g.virtSpecifierSeq);

    g.virtSpecifier.mock();

    assertThat(p, parse("virtSpecifier"));
    assertThat(p, parse("virtSpecifier virtSpecifier"));
  }

  @Test
  public void virtSpecifierSeq_reallife() {
    p.setRootRule(g.virtSpecifierSeq);

    assertThat(p, parse("override"));
  }

  @Test
  public void virtSpecifier() {
    p.setRootRule(g.virtSpecifier);

    assertThat(p, parse("override"));
    assertThat(p, parse("final"));
  }

  @Test
  public void baseSpecifierList() {
    p.setRootRule(g.baseSpecifierList);

    g.baseSpecifier.mock();

    assertThat(p, parse("baseSpecifier"));
    assertThat(p, parse("baseSpecifier ..."));

    assertThat(p, parse("baseSpecifier , baseSpecifier"));
    assertThat(p, parse("baseSpecifier , baseSpecifier ..."));
    assertThat(p, parse("baseSpecifier ..., baseSpecifier ..."));
  }

  @Test
  public void baseSpecifier() {
    p.setRootRule(g.baseSpecifier);

    g.baseTypeSpecifier.mock();
    g.attributeSpecifierSeq.mock();
    g.accessSpecifier.mock();

    assertThat(p, parse("baseTypeSpecifier"));
    assertThat(p, parse("attributeSpecifierSeq baseTypeSpecifier"));

    assertThat(p, parse("virtual baseTypeSpecifier"));
    assertThat(p, parse("attributeSpecifierSeq virtual accessSpecifier baseTypeSpecifier"));

    assertThat(p, parse("accessSpecifier baseTypeSpecifier"));
    assertThat(p, parse("attributeSpecifierSeq accessSpecifier virtual baseTypeSpecifier"));
  }

  @Test
  public void classOrDecltype() {
    p.setRootRule(g.classOrDecltype);

    g.className.mock();
    g.nestedNameSpecifier.mock();
    g.decltypeSpecifier.mock();

    assertThat(p, parse("className"));
    assertThat(p, parse("nestedNameSpecifier className"));
    assertThat(p, parse("decltypeSpecifier"));
  }
}
