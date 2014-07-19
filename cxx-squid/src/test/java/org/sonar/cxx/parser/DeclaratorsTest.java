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

public class DeclaratorsTest extends ParserBaseTest {

  @Test
  public void initDeclaratorList() {
    p.setRootRule(g.rule(CxxGrammarImpl.initDeclaratorList));

    g.rule(CxxGrammarImpl.initDeclarator).mock();

    assertThat(p).matches("initDeclarator");
    assertThat(p).matches("initDeclarator , initDeclarator");
  }

  @Test
  public void initDeclaratorList_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.initDeclaratorList));

    assertThat(p).matches("a");
    assertThat(p).matches("foo(string, bool)");
  }

  @Test
  public void initDeclarator_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.initDeclarator));

    assertThat(p).matches("coll((istream_iterator<string>(cin)), istream_iterator<string>())");
    assertThat(p).matches("a");
  }

  @Test
  public void declarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.declarator));

    g.rule(CxxGrammarImpl.ptrDeclarator).mock();
    g.rule(CxxGrammarImpl.noptrDeclarator).mock();
    g.rule(CxxGrammarImpl.parametersAndQualifiers).mock();
    g.rule(CxxGrammarImpl.trailingReturnType).mock();

    assertThat(p).matches("ptrDeclarator");
    assertThat(p).matches("noptrDeclarator parametersAndQualifiers trailingReturnType");
  }

  @Test
  public void declarator_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.declarator));

    assertThat(p).matches("a");
    assertThat(p).matches("foo()");
    assertThat(p).matches("max(int a, int b, int c)");
    assertThat(p).matches("tword[20]");
    assertThat(p).matches("*what() throw()");
    assertThat(p).matches("foo(string, bool)");
  }

  @Test
  public void noptrDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.noptrDeclarator));

    g.rule(CxxGrammarImpl.declaratorId).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.parametersAndQualifiers).mock();
    g.rule(CxxGrammarImpl.constantExpression).mock();
    g.rule(CxxGrammarImpl.ptrDeclarator).mock();

    assertThat(p).matches("declaratorId");
    assertThat(p).matches("declaratorId attributeSpecifierSeq");
    assertThat(p).matches("declaratorId parametersAndQualifiers");
    assertThat(p).matches("declaratorId [ ]");
    assertThat(p).matches("declaratorId [ constantExpression ]");
    assertThat(p).matches("declaratorId [ ] attributeSpecifierSeq");
    assertThat(p).matches("declaratorId [ constantExpression ] attributeSpecifierSeq");
    assertThat(p).matches("declaratorId [ ] attributeSpecifierSeq");
    assertThat(p).matches("( ptrDeclarator )");
  }

  @Test
  public void noptrDeclarator_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.noptrDeclarator));

    assertThat(p).matches("coll");
  }

  @Test
  public void parametersAndQualifiers() {
    p.setRootRule(g.rule(CxxGrammarImpl.parametersAndQualifiers));

    g.rule(CxxGrammarImpl.parameterDeclarationClause).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.cvQualifierSeq).mock();
    g.rule(CxxGrammarImpl.refQualifier).mock();
    g.rule(CxxGrammarImpl.exceptionSpecification).mock();

    assertThat(p).matches("( parameterDeclarationClause )");
    assertThat(p).matches("( parameterDeclarationClause ) attributeSpecifierSeq");
    assertThat(p).matches("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq");
    assertThat(p).matches("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq refQualifier");
    assertThat(p).matches("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq refQualifier exceptionSpecification");
  }

  @Test
  public void parametersAndQualifiers_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.parametersAndQualifiers));

    assertThat(p).matches("(ostream& strm, const int& i)");
    assertThat(p).matches("(string, bool)");
  }

  @Test
  public void ptrDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.ptrDeclarator));

    g.rule(CxxGrammarImpl.noptrDeclarator).mock();
    g.rule(CxxGrammarImpl.ptrOperator).mock();

    assertThat(p).matches("noptrDeclarator");
    assertThat(p).matches("ptrOperator noptrDeclarator");
    assertThat(p).matches("ptrOperator ptrOperator noptrDeclarator");

  }

  @Test
  public void ptrDeclarator_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.ptrDeclarator));

    assertThat(p).matches("A::*foo");
  }

  @Test
  public void ptrOperator() {
    p.setRootRule(g.rule(CxxGrammarImpl.ptrOperator));

    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.cvQualifierSeq).mock();
    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();

    assertThat(p).matches("*");
    assertThat(p).matches("* attributeSpecifierSeq");
    assertThat(p).matches("* attributeSpecifierSeq cvQualifierSeq");
    assertThat(p).matches("&");
    assertThat(p).matches("& attributeSpecifierSeq");
    assertThat(p).matches("&&");
    assertThat(p).matches("&& attributeSpecifierSeq");
    assertThat(p).matches("nestedNameSpecifier *");
    assertThat(p).matches("nestedNameSpecifier * cvQualifierSeq");
    assertThat(p).matches("nestedNameSpecifier * attributeSpecifierSeq cvQualifierSeq");
  }

  @Test
  public void ptrOperator_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.ptrOperator));

    assertThat(p).matches("A::*");
  }

  @Test
  public void cvQualifierSeq() {
    p.setRootRule(g.rule(CxxGrammarImpl.cvQualifierSeq));

    g.rule(CxxGrammarImpl.cvQualifier).mock();

    assertThat(p).matches("cvQualifier");
    assertThat(p).matches("cvQualifier cvQualifier");
  }

  @Test
  public void declaratorId() {
    p.setRootRule(g.rule(CxxGrammarImpl.declaratorId));

    g.rule(CxxGrammarImpl.idExpression).mock();
    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();
    g.rule(CxxGrammarImpl.className).mock();

    assertThat(p).matches("idExpression");
    assertThat(p).matches("... idExpression");
    assertThat(p).matches("className");
    assertThat(p).matches("nestedNameSpecifier className");
  }

  @Test
  public void declaratorId_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.declaratorId));

    assertThat(p).matches("lala<int>");
    assertThat(p).matches("operator==<B>");
  }

  @Test
  public void typeId() {
    p.setRootRule(g.rule(CxxGrammarImpl.typeId));

    assertThat(p).matches("int");
    assertThat(p).matches("int *");
    assertThat(p).matches("int *[3]");
    assertThat(p).matches("int (*)[3]");
    assertThat(p).matches("int *()");
    assertThat(p).matches("int (*)(double)");
  }

  @Test
  public void abstractDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.abstractDeclarator));

    g.rule(CxxGrammarImpl.ptrAbstractDeclarator).mock();
    g.rule(CxxGrammarImpl.noptrAbstractDeclarator).mock();
    g.rule(CxxGrammarImpl.parametersAndQualifiers).mock();
    g.rule(CxxGrammarImpl.trailingReturnType).mock();
    g.rule(CxxGrammarImpl.abstractPackDeclarator).mock();

    assertThat(p).matches("ptrAbstractDeclarator");
    assertThat(p).matches("parametersAndQualifiers trailingReturnType");
    assertThat(p).matches("noptrAbstractDeclarator parametersAndQualifiers trailingReturnType");
    assertThat(p).matches("abstractPackDeclarator");
  }

  @Test
  public void ptrAbstractDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.ptrAbstractDeclarator));

    g.rule(CxxGrammarImpl.noptrAbstractDeclarator).mock();
    g.rule(CxxGrammarImpl.ptrOperator).mock();

    assertThat(p).matches("ptrOperator");
    assertThat(p).matches("ptrOperator ptrOperator");
    assertThat(p).matches("ptrOperator noptrAbstractDeclarator");
    assertThat(p).matches("ptrOperator ptrOperator noptrAbstractDeclarator");
    assertThat(p).matches("noptrAbstractDeclarator");
  }

  @Test
  public void noptrAbstractDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.noptrAbstractDeclarator));

    g.rule(CxxGrammarImpl.parametersAndQualifiers).mock();
    g.rule(CxxGrammarImpl.constantExpression).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.ptrAbstractDeclarator).mock();

    assertThat(p).matches("parametersAndQualifiers");
    assertThat(p).matches("( ptrAbstractDeclarator ) parametersAndQualifiers");

    assertThat(p).matches("[ ]");
    assertThat(p).matches("[ constantExpression ]");
    assertThat(p).matches("[ constantExpression ] attributeSpecifierSeq");
    assertThat(p).matches("( ptrAbstractDeclarator ) [ constantExpression ] attributeSpecifierSeq");

    assertThat(p).matches("( ptrAbstractDeclarator )");
  }

  @Test
  public void abstractPackDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.abstractPackDeclarator));

    g.rule(CxxGrammarImpl.noptrAbstractPackDeclarator).mock();
    g.rule(CxxGrammarImpl.ptrOperator).mock();

    assertThat(p).matches("noptrAbstractPackDeclarator");
    assertThat(p).matches("ptrOperator noptrAbstractPackDeclarator");
    assertThat(p).matches("ptrOperator ptrOperator noptrAbstractPackDeclarator");
  }

  @Test
  public void noptrAbstractPackDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.noptrAbstractPackDeclarator));

    g.rule(CxxGrammarImpl.parametersAndQualifiers).mock();
    g.rule(CxxGrammarImpl.constantExpression).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();

    assertThat(p).matches("...");
    assertThat(p).matches("... parametersAndQualifiers");
    assertThat(p).matches("... [ ] ");
    assertThat(p).matches("... [ constantExpression ] ");
    assertThat(p).matches("... [ constantExpression ] attributeSpecifierSeq");
  }

  @Test
  public void parameterDeclarationList() {
    p.setRootRule(g.rule(CxxGrammarImpl.parameterDeclarationList));

    g.rule(CxxGrammarImpl.parameterDeclaration).mock();

    assertThat(p).matches("parameterDeclaration");
    assertThat(p).matches("parameterDeclaration , parameterDeclaration");
  }

  @Test
  public void parameterDeclarationList_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.parameterDeclarationList));

    assertThat(p).matches("ostream& strm, const int& i");
    assertThat(p).matches("string, bool");
  }

  @Test
  public void parameterDeclarationClause() {
    p.setRootRule(g.rule(CxxGrammarImpl.parameterDeclarationClause));

    g.rule(CxxGrammarImpl.parameterDeclarationList).mock();

    assertThat(p).matches("");
    assertThat(p).matches("parameterDeclarationList");
    assertThat(p).matches("...");
    assertThat(p).matches("parameterDeclarationList ...");
    assertThat(p).matches("parameterDeclarationList , ...");
  }

  @Test
  public void parameterDeclarationClause_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.parameterDeclarationClause));

    assertThat(p).matches("ostream& strm, const int& i");
    assertThat(p).matches("string, bool");
  }

  @Test
  public void parameterDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.parameterDeclaration));

    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.parameterDeclSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.declarator).mock();
    g.rule(CxxGrammarImpl.initializerClause).mock();
    g.rule(CxxGrammarImpl.abstractDeclarator).mock();

    assertThat(p).matches("parameterDeclSpecifierSeq declarator");
    assertThat(p).matches("attributeSpecifierSeq parameterDeclSpecifierSeq declarator");

    assertThat(p).matches("parameterDeclSpecifierSeq declarator = initializerClause");
    assertThat(p).matches("attributeSpecifierSeq parameterDeclSpecifierSeq declarator = initializerClause");

    assertThat(p).matches("parameterDeclSpecifierSeq");
    assertThat(p).matches("parameterDeclSpecifierSeq abstractDeclarator");
    assertThat(p).matches("attributeSpecifierSeq parameterDeclSpecifierSeq abstractDeclarator");

    assertThat(p).matches("parameterDeclSpecifierSeq = initializerClause");
    assertThat(p).matches("parameterDeclSpecifierSeq abstractDeclarator = initializerClause");
    assertThat(p).matches("attributeSpecifierSeq parameterDeclSpecifierSeq abstractDeclarator = initializerClause");
  }

  @Test
  public void parameterDeclaration_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.parameterDeclaration));

    assertThat(p).matches("ostream& strm");
    assertThat(p).matches("const int& i");
    assertThat(p).matches("const paramtype<T> param");
    assertThat(p).matches("const auto_ptr<T>& p");
    assertThat(p).matches("string");
    assertThat(p).matches("::P& c");
    assertThat(p).matches("bool (A::*bar)(void)");
  }

  @Test
  public void functionDefinition() {
    p.setRootRule(g.rule(CxxGrammarImpl.functionDefinition));

    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.functionDeclSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.declarator).mock();
    g.rule(CxxGrammarImpl.virtSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.functionBody).mock();

    assertThat(p).matches("declarator functionBody");
    assertThat(p).matches("attributeSpecifierSeq declarator functionBody");
    assertThat(p).matches("attributeSpecifierSeq functionDeclSpecifierSeq declarator functionBody");
    assertThat(p).matches("attributeSpecifierSeq functionDeclSpecifierSeq declarator virtSpecifierSeq functionBody");
  }

  @Test
  public void functionDefinition_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.functionDefinition));

    assertThat(p).matches("int foo(){}");
    assertThat(p).matches("int A::foo(){}");
    assertThat(p).matches("static int foo(){}");
    assertThat(p).matches("main(){}");
    assertThat(p).matches("int max(int a, int b, int c) { int m = (a > b) ? a : b; return (m > c) ? m : c; }");
    assertThat(p).matches("AddValue (const T& v) : theValue(v) {}");
    assertThat(p).matches("void operator[] () {}");
    assertThat(p).matches("void operator() (T& elem) const {elem += theValue;}");
    assertThat(p).matches("int main(){}");
    assertThat(p).matches("virtual const char* what() const throw() { return \"read empty stack\"; }");
    assertThat(p).matches("void foo() override {}");
    assertThat(p).matches("void foo(::P& c) {}");
  }

  @Test
  public void functionBody() {
    p.setRootRule(g.rule(CxxGrammarImpl.functionBody));

    g.rule(CxxGrammarImpl.compoundStatement).mock();
    g.rule(CxxGrammarImpl.ctorInitializer).mock();
    g.rule(CxxGrammarImpl.functionTryBlock).mock();

    assertThat(p).matches("compoundStatement");
    assertThat(p).matches("ctorInitializer compoundStatement");

    assertThat(p).matches("functionTryBlock");
    assertThat(p).matches("= default ;");
    assertThat(p).matches("= delete ;");
  }

  @Test
  public void functionBody_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.functionBody));

    assertThat(p).matches("{ /* ... */ }");
    assertThat(p).matches(": lala(0) {}");
    assertThat(p).matches("{ return \"read empty stack\"; }");
  }

  @Test
  public void initializer_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.initializer));

    assertThat(p).matches("(new int(42))");
    assertThat(p).matches("((istream_iterator<string>(cin)), istream_iterator<string>())");
  }

  @Test
  public void initializerClause_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.initializerClause));

    assertThat(p).matches("(istream_iterator<string>(cin))");
    assertThat(p).matches("istream_iterator<string>()");

    // C-COMPATIBILITY: C99 designated initializers
    assertThat(p).matches(".name = string(\"Something\")");
    assertThat(p).matches("[5] = {}");
    assertThat(p).matches(".values = { [4] = 5, [5 ... 7] = 1, [2] = 0 }");
  }

  @Test
  public void initializerClause() {
    p.setRootRule(g.rule(CxxGrammarImpl.initializerClause));

    g.rule(CxxGrammarImpl.assignmentExpression).mock();
    g.rule(CxxGrammarImpl.initializerList).mock();
    g.rule(CxxGrammarImpl.constantExpression).mock();

    assertThat(p).matches("{}");
    assertThat(p).matches("{ initializerList }");
    assertThat(p).matches("assignmentExpression");

    // C-COMPATIBILITY: C99 designated initializers
    assertThat(p).matches(".fieldName = {}");
    assertThat(p).matches(".fieldName = { initializerList }");
    assertThat(p).matches(".fieldName = assignmentExpression");
    assertThat(p).matches("[constantExpression] = {}");
    assertThat(p).matches("[constantExpression] = { initializerList }");
    assertThat(p).matches("[constantExpression] = assignmentExpression");

    // C-COMPATIBILITY: EXTENSION: gcc's designated initializers range
    assertThat(p).matches("[constantExpression ... constantExpression] = {}");
    assertThat(p).matches("[constantExpression ... constantExpression] = { initializerList }");
    assertThat(p).matches("[constantExpression ... constantExpression] = assignmentExpression");
  }

  @Test
  public void initializerList() {
    p.setRootRule(g.rule(CxxGrammarImpl.initializerList));

    g.rule(CxxGrammarImpl.initializerClause).mock();

    assertThat(p).matches("initializerClause");
    assertThat(p).matches("initializerClause ...");
    assertThat(p).matches("initializerClause , initializerClause");
    assertThat(p).matches("initializerClause , initializerClause ...");
  }

  @Test
  public void initializerList_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.initializerList));

    assertThat(p).matches("(istream_iterator<string>(cin)), istream_iterator<string>()");
  }

  @Test
  public void bracedInitList() {
    p.setRootRule(g.rule(CxxGrammarImpl.bracedInitList));

    g.rule(CxxGrammarImpl.initializerList).mock();

    assertThat(p).matches("{}");
    assertThat(p).matches("{ initializerList }");
    assertThat(p).matches("{ initializerList , }");
  }
}
