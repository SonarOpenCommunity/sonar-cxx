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

public class DeclaratorsTest extends ParserBaseTestHelper {

  @Test
  public void initDeclaratorList() {
    p.setRootRule(g.rule(CxxGrammarImpl.initDeclaratorList));

    mockRule(CxxGrammarImpl.initDeclarator);

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

    mockRule(CxxGrammarImpl.ptrDeclarator);
    mockRule(CxxGrammarImpl.noptrDeclarator);
    mockRule(CxxGrammarImpl.parametersAndQualifiers);
    mockRule(CxxGrammarImpl.trailingReturnType);

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
    assertThat(p).matches("foo(const string p1, bool p2)");
    assertThat(p).matches("foo(const string &p1, bool p2)");
  }

  @Test
  public void noptrDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.noptrDeclarator));

    mockRule(CxxGrammarImpl.declaratorId);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.parametersAndQualifiers);
    mockRule(CxxGrammarImpl.constantExpression);
    mockRule(CxxGrammarImpl.ptrDeclarator);

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

    mockRule(CxxGrammarImpl.parameterDeclarationClause);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.cvQualifierSeq);
    mockRule(CxxGrammarImpl.refQualifier);
    mockRule(CxxGrammarImpl.noexceptSpecifier);

    assertThat(p).matches("( parameterDeclarationClause )");
    assertThat(p).matches("( parameterDeclarationClause ) attributeSpecifierSeq");
    assertThat(p).matches("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq");
    assertThat(p).matches("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq refQualifier");
    assertThat(p).matches("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq refQualifier noexceptSpecifier");
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

    mockRule(CxxGrammarImpl.noptrDeclarator);
    mockRule(CxxGrammarImpl.ptrOperator);

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

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.cvQualifierSeq);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);

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

    mockRule(CxxGrammarImpl.cvQualifier);

    assertThat(p).matches("cvQualifier");
    assertThat(p).matches("cvQualifier cvQualifier");
  }

  @Test
  public void declaratorId() {
    p.setRootRule(g.rule(CxxGrammarImpl.declaratorId));

    mockRule(CxxGrammarImpl.idExpression);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.className);

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

    mockRule(CxxGrammarImpl.typeSpecifierSeq);
    mockRule(CxxGrammarImpl.abstractDeclarator);

    assertThat(p).matches("typeSpecifierSeq");
    assertThat(p).matches("typeSpecifierSeq abstractDeclarator");
  }

  @Test
  public void typeId_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.typeId));

    assertThat(p).matches("int");
    assertThat(p).matches("int *");
    assertThat(p).matches("int *[3]");
    assertThat(p).matches("int (*)[3]");
    assertThat(p).matches("int *()");
    assertThat(p).matches("int (*)(double)");
  }

  @Test
  public void definingTypeId() {
    p.setRootRule(g.rule(CxxGrammarImpl.definingTypeId));

    mockRule(CxxGrammarImpl.definingTypeSpecifierSeq);
    mockRule(CxxGrammarImpl.abstractDeclarator);

    assertThat(p).matches("definingTypeSpecifierSeq");
    assertThat(p).matches("definingTypeSpecifierSeq abstractDeclarator");
  }

  @Test
  public void abstractDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.abstractDeclarator));

    mockRule(CxxGrammarImpl.ptrAbstractDeclarator);
    mockRule(CxxGrammarImpl.noptrAbstractDeclarator);
    mockRule(CxxGrammarImpl.parametersAndQualifiers);
    mockRule(CxxGrammarImpl.trailingReturnType);
    mockRule(CxxGrammarImpl.abstractPackDeclarator);

    assertThat(p).matches("ptrAbstractDeclarator");
    assertThat(p).matches("parametersAndQualifiers trailingReturnType");
    assertThat(p).matches("noptrAbstractDeclarator parametersAndQualifiers trailingReturnType");
    assertThat(p).matches("abstractPackDeclarator");
  }

  @Test
  public void ptrAbstractDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.ptrAbstractDeclarator));

    mockRule(CxxGrammarImpl.noptrAbstractDeclarator);
    mockRule(CxxGrammarImpl.ptrOperator);

    assertThat(p).matches("ptrOperator");
    assertThat(p).matches("ptrOperator ptrOperator");
    assertThat(p).matches("ptrOperator noptrAbstractDeclarator");
    assertThat(p).matches("ptrOperator ptrOperator noptrAbstractDeclarator");
    assertThat(p).matches("noptrAbstractDeclarator");
  }

  @Test
  public void noptrAbstractDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.noptrAbstractDeclarator));

    mockRule(CxxGrammarImpl.parametersAndQualifiers);
    mockRule(CxxGrammarImpl.constantExpression);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.ptrAbstractDeclarator);

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

    mockRule(CxxGrammarImpl.noptrAbstractPackDeclarator);
    mockRule(CxxGrammarImpl.ptrOperator);

    assertThat(p).matches("noptrAbstractPackDeclarator");
    assertThat(p).matches("ptrOperator noptrAbstractPackDeclarator");
    assertThat(p).matches("ptrOperator ptrOperator noptrAbstractPackDeclarator");
  }

  @Test
  public void noptrAbstractPackDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.noptrAbstractPackDeclarator));

    mockRule(CxxGrammarImpl.parametersAndQualifiers);
    mockRule(CxxGrammarImpl.constantExpression);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThat(p).matches("...");
    assertThat(p).matches("... parametersAndQualifiers");
    assertThat(p).matches("... [ ] ");
    assertThat(p).matches("... [ constantExpression ] ");
    assertThat(p).matches("... [ constantExpression ] attributeSpecifierSeq");
  }

  @Test
  public void parameterDeclarationList() {
    p.setRootRule(g.rule(CxxGrammarImpl.parameterDeclarationList));

    mockRule(CxxGrammarImpl.parameterDeclaration);

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

    mockRule(CxxGrammarImpl.parameterDeclarationList);

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

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.parameterDeclSpecifierSeq);
    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.initializerClause);
    mockRule(CxxGrammarImpl.abstractDeclarator);

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
    // CLI extension
    assertThat(p).matches("const int^ i");
    assertThat(p).matches("const int% i");
    assertThat(p).matches("const int^% i");
  }

  @Test
  public void functionDefinition() {
    p.setRootRule(g.rule(CxxGrammarImpl.functionDefinition));

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.functionDeclSpecifierSeq);
    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.virtSpecifierSeq);
    mockRule(CxxGrammarImpl.functionBody);

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
    assertThat(p).matches("auto equal_range(ForwardIterator first, ForwardIterator last, const Type& value) -> std::pair<ForwardIterator, ForwardIterator> { return pair; }");
    assertThat(p).matches("auto to_string(int value) -> std::string { return \"\"; }");
    assertThat(p).matches("auto size() const -> std::size_t { return 0; }");
//  ToDo : make this work
//    assertThat(p).matches("auto str() const -> const char* { return nullptr; }");
//    assertThat(p).matches("auto std::map::at(const key_type& key) -> mapped_type& { return value; }");
  }

  @Test
  public void functionBody() {
    p.setRootRule(g.rule(CxxGrammarImpl.functionBody));

    mockRule(CxxGrammarImpl.compoundStatement);
    mockRule(CxxGrammarImpl.ctorInitializer);
    mockRule(CxxGrammarImpl.functionTryBlock);

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

    mockRule(CxxGrammarImpl.assignmentExpression);
    mockRule(CxxGrammarImpl.initializerList);
    mockRule(CxxGrammarImpl.constantExpression);

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
    assertThat(p).matches(".a.b = {}");
    assertThat(p).matches("[constantExpression][constantExpression] = {}");
    assertThat(p).matches("[constantExpression][constantExpression].a = {}");
    assertThat(p).matches("[constantExpression][constantExpression].a.b = {}");

    // C-COMPATIBILITY: EXTENSION: gcc's designated initializers range
    assertThat(p).matches("[constantExpression ... constantExpression] = {}");
    assertThat(p).matches("[constantExpression ... constantExpression] = { initializerList }");
    assertThat(p).matches("[constantExpression ... constantExpression] = assignmentExpression");
  }

  @Test
  public void initializerList() {
    p.setRootRule(g.rule(CxxGrammarImpl.initializerList));

    mockRule(CxxGrammarImpl.initializerClause);

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

    mockRule(CxxGrammarImpl.initializerList);

    assertThat(p).matches("{}");
    assertThat(p).matches("{ initializerList }");
    assertThat(p).matches("{ initializerList , }");
  }
}
