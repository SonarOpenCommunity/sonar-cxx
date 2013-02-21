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

public class DeclaratorsTest {

  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();

  @Test
  public void initDeclaratorList() {
    p.setRootRule(g.initDeclaratorList);

    g.initDeclarator.mock();

    assertThat(p, parse("initDeclarator"));
    assertThat(p, parse("initDeclarator , initDeclarator"));
  }

  @Test
  public void initDeclaratorList_reallife() {
    p.setRootRule(g.initDeclaratorList);

    assertThat(p, parse("a"));
    assertThat(p, parse("foo(string, bool)"));
  }

  @Test
  public void initDeclarator_reallife() {
    p.setRootRule(g.initDeclarator);

    assertThat(p, parse("coll((istream_iterator<string>(cin)), istream_iterator<string>())"));
    assertThat(p, parse("a"));
  }

  @Test
  public void declarator() {
    p.setRootRule(g.declarator);

    g.ptrDeclarator.mock();
    g.noptrDeclarator.mock();
    g.parametersAndQualifiers.mock();
    g.trailingReturnType.mock();

    assertThat(p, parse("ptrDeclarator"));
    assertThat(p, parse("noptrDeclarator parametersAndQualifiers trailingReturnType"));
  }

  @Test
  public void declarator_reallife() {
    p.setRootRule(g.declarator);

    assertThat(p, parse("a"));
    assertThat(p, parse("foo()"));
    assertThat(p, parse("max(int a, int b, int c)"));
    assertThat(p, parse("tword[20]"));
    assertThat(p, parse("*what() throw()"));
    assertThat(p, parse("foo(string, bool)"));
  }

  @Test
  public void noptrDeclarator() {
    p.setRootRule(g.noptrDeclarator);

    g.declaratorId.mock();
    g.attributeSpecifierSeq.mock();
    g.parametersAndQualifiers.mock();
    g.constantExpression.mock();
    g.ptrDeclarator.mock();

    assertThat(p, parse("declaratorId"));
    assertThat(p, parse("declaratorId attributeSpecifierSeq"));
    assertThat(p, parse("declaratorId parametersAndQualifiers"));
    assertThat(p, parse("declaratorId [ ]"));
    assertThat(p, parse("declaratorId [ constantExpression ]"));
    assertThat(p, parse("declaratorId [ ] attributeSpecifierSeq"));
    assertThat(p, parse("declaratorId [ constantExpression ] attributeSpecifierSeq"));
    assertThat(p, parse("declaratorId [ ] attributeSpecifierSeq"));
    assertThat(p, parse("( ptrDeclarator )"));
  }

  @Test
  public void noptrDeclarator_reallife() {
    p.setRootRule(g.noptrDeclarator);

    assertThat(p, parse("coll"));
  }

  @Test
  public void parametersAndQualifiers() {
    p.setRootRule(g.parametersAndQualifiers);

    g.parameterDeclarationClause.mock();
    g.attributeSpecifierSeq.mock();
    g.cvQualifierSeq.mock();
    g.refQualifier.mock();
    g.exceptionSpecification.mock();

    assertThat(p, parse("( parameterDeclarationClause )"));
    assertThat(p, parse("( parameterDeclarationClause ) attributeSpecifierSeq"));
    assertThat(p, parse("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq"));
    assertThat(p, parse("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq refQualifier"));
    assertThat(p, parse("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq refQualifier exceptionSpecification"));
  }

  @Test
  public void parametersAndQualifiers_reallife() {
    p.setRootRule(g.parametersAndQualifiers);

    assertThat(p, parse("(ostream& strm, const int& i)"));
    assertThat(p, parse("(string, bool)"));
  }

  @Test
  public void ptrDeclarator() {
    p.setRootRule(g.ptrDeclarator);

    g.noptrDeclarator.mock();
    g.ptrOperator.mock();

    assertThat(p, parse("noptrDeclarator"));
    assertThat(p, parse("ptrOperator noptrDeclarator"));
    assertThat(p, parse("ptrOperator ptrOperator noptrDeclarator"));
    
  }

  @Test
  public void ptrDeclarator_reallife() {
    p.setRootRule(g.ptrDeclarator);

    assertThat(p, parse("A::*foo"));
  }
  
  @Test
  public void ptrOperator() {
    p.setRootRule(g.ptrOperator);

    g.attributeSpecifierSeq.mock();
    g.cvQualifierSeq.mock();
    g.nestedNameSpecifier.mock();

    assertThat(p, parse("*"));
    assertThat(p, parse("* attributeSpecifierSeq"));
    assertThat(p, parse("* attributeSpecifierSeq cvQualifierSeq"));
    assertThat(p, parse("&"));
    assertThat(p, parse("& attributeSpecifierSeq"));
    assertThat(p, parse("&&"));
    assertThat(p, parse("&& attributeSpecifierSeq"));
    assertThat(p, parse("nestedNameSpecifier *"));
    assertThat(p, parse("nestedNameSpecifier * cvQualifierSeq"));
    assertThat(p, parse("nestedNameSpecifier * attributeSpecifierSeq cvQualifierSeq"));
  }

  @Test
  public void ptrOperator_reallife() {
    p.setRootRule(g.ptrOperator);
    
    assertThat(p, parse("A::*"));
  }
  
  @Test
  public void cvQualifierSeq() {
    p.setRootRule(g.cvQualifierSeq);

    g.cvQualifier.mock();

    assertThat(p, parse("cvQualifier"));
    assertThat(p, parse("cvQualifier cvQualifier"));
  }

  @Test
  public void declaratorId() {
    p.setRootRule(g.declaratorId);

    g.idExpression.mock();
    g.nestedNameSpecifier.mock();
    g.className.mock();

    assertThat(p, parse("idExpression"));
    assertThat(p, parse("... idExpression"));
    assertThat(p, parse("className"));
    assertThat(p, parse("nestedNameSpecifier className"));
  }

  @Test
  public void declaratorId_reallife() {
    p.setRootRule(g.declaratorId);

    assertThat(p, parse("lala<int>"));
    assertThat(p, parse("operator==<B>"));
  }

  @Test
  public void typeId() {
    p.setRootRule(g.typeId);

    assertThat(p, parse("int"));
    assertThat(p, parse("int *"));
    assertThat(p, parse("int *[3]"));
    assertThat(p, parse("int (*)[3]"));
    assertThat(p, parse("int *()"));
    assertThat(p, parse("int (*)(double)"));
  }

  @Test
  public void abstractDeclarator() {
    p.setRootRule(g.abstractDeclarator);

    g.ptrAbstractDeclarator.mock();
    g.noptrAbstractDeclarator.mock();
    g.parametersAndQualifiers.mock();
    g.trailingReturnType.mock();
    g.abstractPackDeclarator.mock();

    assertThat(p, parse("ptrAbstractDeclarator"));
    assertThat(p, parse("parametersAndQualifiers trailingReturnType"));
    assertThat(p, parse("noptrAbstractDeclarator parametersAndQualifiers trailingReturnType"));
    assertThat(p, parse("abstractPackDeclarator"));
  }

  @Test
  public void ptrAbstractDeclarator() {
    p.setRootRule(g.ptrAbstractDeclarator);

    g.noptrAbstractDeclarator.mock();
    g.ptrOperator.mock();

    assertThat(p, parse("ptrOperator"));
    assertThat(p, parse("ptrOperator ptrOperator"));
    assertThat(p, parse("ptrOperator noptrAbstractDeclarator"));
    assertThat(p, parse("ptrOperator ptrOperator noptrAbstractDeclarator"));
    assertThat(p, parse("noptrAbstractDeclarator"));
  }

  @Test
  public void noptrAbstractDeclarator() {
    p.setRootRule(g.noptrAbstractDeclarator);

    g.parametersAndQualifiers.mock();
    g.constantExpression.mock();
    g.attributeSpecifierSeq.mock();
    g.ptrAbstractDeclarator.mock();

    assertThat(p, parse("parametersAndQualifiers"));
    assertThat(p, parse("( ptrAbstractDeclarator ) parametersAndQualifiers"));

    assertThat(p, parse("[ ]"));
    assertThat(p, parse("[ constantExpression ]"));
    assertThat(p, parse("[ constantExpression ] attributeSpecifierSeq"));
    assertThat(p, parse("( ptrAbstractDeclarator ) [ constantExpression ] attributeSpecifierSeq"));

    assertThat(p, parse("( ptrAbstractDeclarator )"));
  }

  @Test
  public void abstractPackDeclarator() {
    p.setRootRule(g.abstractPackDeclarator);

    g.noptrAbstractPackDeclarator.mock();
    g.ptrOperator.mock();

    assertThat(p, parse("noptrAbstractPackDeclarator"));
    assertThat(p, parse("ptrOperator noptrAbstractPackDeclarator"));
    assertThat(p, parse("ptrOperator ptrOperator noptrAbstractPackDeclarator"));
  }

  @Test
  public void noptrAbstractPackDeclarator() {
    p.setRootRule(g.noptrAbstractPackDeclarator);

    g.parametersAndQualifiers.mock();
    g.constantExpression.mock();
    g.attributeSpecifierSeq.mock();

    assertThat(p, parse("..."));
    assertThat(p, parse("... parametersAndQualifiers"));
    assertThat(p, parse("... [ ] "));
    assertThat(p, parse("... [ constantExpression ] "));
    assertThat(p, parse("... [ constantExpression ] attributeSpecifierSeq"));
  }

  @Test
  public void parameterDeclarationList() {
    p.setRootRule(g.parameterDeclarationList);

    g.parameterDeclaration.mock();

    assertThat(p, parse("parameterDeclaration"));
    assertThat(p, parse("parameterDeclaration , parameterDeclaration"));
  }

  @Test
  public void parameterDeclarationList_reallife() {
    p.setRootRule(g.parameterDeclarationList);

    assertThat(p, parse("ostream& strm, const int& i"));
    assertThat(p, parse("string, bool"));
  }

  @Test
  public void parameterDeclarationClause() {
    p.setRootRule(g.parameterDeclarationClause);

    g.parameterDeclarationList.mock();

    assertThat(p, parse(""));
    assertThat(p, parse("parameterDeclarationList"));
    assertThat(p, parse("..."));
    assertThat(p, parse("parameterDeclarationList ..."));
    assertThat(p, parse("parameterDeclarationList , ..."));
  }

  @Test
  public void parameterDeclarationClause_reallife() {
    p.setRootRule(g.parameterDeclarationClause);

    assertThat(p, parse("ostream& strm, const int& i"));
    assertThat(p, parse("string, bool"));
  }

  @Test
  public void parameterDeclaration() {
    p.setRootRule(g.parameterDeclaration);

    g.attributeSpecifierSeq.mock();
    g.parameterDeclSpecifierSeq.mock();
    g.declarator.mock();
    g.initializerClause.mock();
    g.abstractDeclarator.mock();

    assertThat(p, parse("parameterDeclSpecifierSeq declarator"));
    assertThat(p, parse("attributeSpecifierSeq parameterDeclSpecifierSeq declarator"));

    assertThat(p, parse("parameterDeclSpecifierSeq declarator = initializerClause"));
    assertThat(p, parse("attributeSpecifierSeq parameterDeclSpecifierSeq declarator = initializerClause"));

    assertThat(p, parse("parameterDeclSpecifierSeq"));
    assertThat(p, parse("parameterDeclSpecifierSeq abstractDeclarator"));
    assertThat(p, parse("attributeSpecifierSeq parameterDeclSpecifierSeq abstractDeclarator"));

    assertThat(p, parse("parameterDeclSpecifierSeq = initializerClause"));
    assertThat(p, parse("parameterDeclSpecifierSeq abstractDeclarator = initializerClause"));
    assertThat(p, parse("attributeSpecifierSeq parameterDeclSpecifierSeq abstractDeclarator = initializerClause"));
  }

  @Test
  public void parameterDeclaration_reallife() {
    p.setRootRule(g.parameterDeclaration);

    assertThat(p, parse("ostream& strm"));
    assertThat(p, parse("const int& i"));
    assertThat(p, parse("const paramtype<T> param"));
    assertThat(p, parse("const auto_ptr<T>& p"));
    assertThat(p, parse("string"));
    assertThat(p, parse("::P& c"));
    assertThat(p, parse("bool (A::*bar)(void)"));
  }

  @Test
  public void functionDefinition() {
    p.setRootRule(g.functionDefinition);

    g.attributeSpecifierSeq.mock();
    g.functionDeclSpecifierSeq.mock();
    g.declarator.mock();
    g.virtSpecifierSeq.mock();
    g.functionBody.mock();

    assertThat(p, parse("declarator functionBody"));
    assertThat(p, parse("attributeSpecifierSeq declarator functionBody"));
    assertThat(p, parse("attributeSpecifierSeq functionDeclSpecifierSeq declarator functionBody"));
    assertThat(p, parse("attributeSpecifierSeq functionDeclSpecifierSeq declarator virtSpecifierSeq functionBody"));
  }

  @Test
  public void functionDefinition_reallife() {
    p.setRootRule(g.functionDefinition);

    assertThat(p, parse("int foo(){}"));
    assertThat(p, parse("int A::foo(){}"));
    assertThat(p, parse("static int foo(){}"));
    assertThat(p, parse("main(){}"));
    assertThat(p, parse("int max(int a, int b, int c) { int m = (a > b) ? a : b; return (m > c) ? m : c; }"));
    assertThat(p, parse("AddValue (const T& v) : theValue(v) {}"));
    assertThat(p, parse("void operator[] () {}"));
    assertThat(p, parse("void operator() (T& elem) const {elem += theValue;}"));
    assertThat(p, parse("int main(){}"));
    assertThat(p, parse("virtual const char* what() const throw() { return \"read empty stack\"; }"));
    assertThat(p, parse("void foo() override {}"));
    assertThat(p, parse("void foo(::P& c) {}"));
  }

  @Test
  public void functionBody() {
    p.setRootRule(g.functionBody);

    g.compoundStatement.mock();
    g.ctorInitializer.mock();
    g.functionTryBlock.mock();

    assertThat(p, parse("compoundStatement"));
    assertThat(p, parse("ctorInitializer compoundStatement"));

    assertThat(p, parse("functionTryBlock"));
    assertThat(p, parse("= default ;"));
    assertThat(p, parse("= delete ;"));
  }

  @Test
  public void functionBody_reallife() {
    p.setRootRule(g.functionBody);

    assertThat(p, parse("{ /* ... */ }"));
    assertThat(p, parse(": lala(0) {}"));
    assertThat(p, parse("{ return \"read empty stack\"; }"));
  }

  @Test
  public void initializer_reallife() {
    p.setRootRule(g.initializer);

    assertThat(p, parse("(new int(42))"));
    assertThat(p, parse("((istream_iterator<string>(cin)), istream_iterator<string>())"));
  }

  @Test
  public void initializerClause_reallife() {
    p.setRootRule(g.initializerClause);

    assertThat(p, parse("(istream_iterator<string>(cin))"));
    assertThat(p, parse("istream_iterator<string>()"));
  }

  @Test
  public void initializerList() {
    p.setRootRule(g.initializerList);

    g.initializerClause.mock();

    assertThat(p, parse("initializerClause"));
    assertThat(p, parse("initializerClause ..."));
    assertThat(p, parse("initializerClause , initializerClause"));
    assertThat(p, parse("initializerClause , initializerClause ..."));
  }

  @Test
  public void initializerList_reallife() {
    p.setRootRule(g.initializerList);

    assertThat(p, parse("(istream_iterator<string>(cin)), istream_iterator<string>()"));
  }

  @Test
  public void bracedInitList() {
    p.setRootRule(g.bracedInitList);

    g.initializerList.mock();

    assertThat(p, parse("{}"));
    assertThat(p, parse("{ initializerList }"));
    assertThat(p, parse("{ initializerList , }"));
  }
}
