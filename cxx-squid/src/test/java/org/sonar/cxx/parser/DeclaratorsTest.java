/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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

public class DeclaratorsTest extends ParserBaseTestHelper {

  @Test
  public void initDeclarator() {
    setRootRule(CxxGrammarImpl.initDeclarator);

    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.asmLabel);
    mockRule(CxxGrammarImpl.initializer);
    mockRule(CxxGrammarImpl.requiresClause);

    assertThatParser()
      .matches("declarator")
      .matches("declarator requiresClause")
      .matches("declarator initializer")
      .matches("declarator asmLabel")
      .matches("declarator asmLabel initializer");
  }

  @Test
  public void initDeclaratorList() {
    setRootRule(CxxGrammarImpl.initDeclaratorList);

    mockRule(CxxGrammarImpl.initDeclarator);

    assertThatParser()
      .matches("initDeclarator")
      .matches("initDeclarator , initDeclarator");
  }

  @Test
  public void initDeclaratorList_reallife() {
    setRootRule(CxxGrammarImpl.initDeclaratorList);

    assertThatParser()
      .matches("a")
      .matches("foo(string, bool)");
  }

  @Test
  public void initDeclarator_reallife() {
    setRootRule(CxxGrammarImpl.initDeclarator);

    assertThatParser()
      .matches("coll((istream_iterator<string>(cin)), istream_iterator<string>())")
      .matches("a");
  }

  @Test
  public void declarator() {
    setRootRule(CxxGrammarImpl.declarator);

    mockRule(CxxGrammarImpl.ptrDeclarator);
    mockRule(CxxGrammarImpl.noptrDeclarator);
    mockRule(CxxGrammarImpl.parametersAndQualifiers);
    mockRule(CxxGrammarImpl.trailingReturnType);

    assertThatParser()
      .matches("ptrDeclarator")
      .matches("noptrDeclarator parametersAndQualifiers trailingReturnType");
  }

  @Test
  public void declarator_reallife() {
    setRootRule(CxxGrammarImpl.declarator);

    assertThatParser()
      .matches("a")
      .matches("foo()")
      .matches("max(int a, int b, int c)")
      .matches("tword[20]")
      .matches("*what() throw()")
      .matches("foo(string, bool)")
      .matches("foo(const string p1, bool p2)")
      .matches("foo(const string &p1, bool p2)");
  }

  @Test
  public void noptrDeclarator() {
    setRootRule(CxxGrammarImpl.noptrDeclarator);

    mockRule(CxxGrammarImpl.declaratorId);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.parametersAndQualifiers);
    mockRule(CxxGrammarImpl.constantExpression);
    mockRule(CxxGrammarImpl.ptrDeclarator);

    assertThatParser()
      .matches("declaratorId")
      .matches("declaratorId attributeSpecifierSeq")
      .matches("declaratorId parametersAndQualifiers")
      .matches("declaratorId [ ]")
      .matches("declaratorId [ constantExpression ]")
      .matches("declaratorId [ ] attributeSpecifierSeq")
      .matches("declaratorId [ constantExpression ] attributeSpecifierSeq")
      .matches("declaratorId [ ] attributeSpecifierSeq")
      .matches("( ptrDeclarator )");
  }

  @Test
  public void noptrDeclarator_reallife() {
    setRootRule(CxxGrammarImpl.noptrDeclarator);

    assertThatParser()
      .matches("coll");
  }

  @Test
  public void parametersAndQualifiers() {
    setRootRule(CxxGrammarImpl.parametersAndQualifiers);

    mockRule(CxxGrammarImpl.parameterDeclarationClause);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.cvQualifierSeq);
    mockRule(CxxGrammarImpl.refQualifier);
    mockRule(CxxGrammarImpl.noexceptSpecifier);

    assertThatParser()
      .matches("( parameterDeclarationClause )")
      .matches("( parameterDeclarationClause ) attributeSpecifierSeq")
      .matches("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq")
      .matches("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq refQualifier")
      .matches("( parameterDeclarationClause ) attributeSpecifierSeq cvQualifierSeq refQualifier noexceptSpecifier");
  }

  @Test
  public void parametersAndQualifiers_reallife() {
    setRootRule(CxxGrammarImpl.parametersAndQualifiers);

    assertThatParser()
      .matches("(ostream& strm, const int& i)")
      .matches("(string, bool)");
  }

  @Test
  public void ptrDeclarator() {
    setRootRule(CxxGrammarImpl.ptrDeclarator);

    mockRule(CxxGrammarImpl.noptrDeclarator);
    mockRule(CxxGrammarImpl.ptrOperator);

    assertThatParser()
      .matches("noptrDeclarator")
      .matches("ptrOperator noptrDeclarator")
      .matches("ptrOperator ptrOperator noptrDeclarator");

  }

  @Test
  public void ptrDeclarator_reallife() {
    setRootRule(CxxGrammarImpl.ptrDeclarator);

    assertThatParser()
      .matches("A::*foo");
  }

  @Test
  public void ptrOperator() {
    setRootRule(CxxGrammarImpl.ptrOperator);

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.cvQualifierSeq);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);

    assertThatParser()
      .matches("*")
      .matches("* attributeSpecifierSeq")
      .matches("* attributeSpecifierSeq cvQualifierSeq")
      .matches("&")
      .matches("& attributeSpecifierSeq")
      .matches("&&")
      .matches("&& attributeSpecifierSeq")
      .matches("nestedNameSpecifier *")
      .matches("nestedNameSpecifier * cvQualifierSeq")
      .matches("nestedNameSpecifier * attributeSpecifierSeq cvQualifierSeq");
  }

  @Test
  public void ptrOperator_reallife() {
    setRootRule(CxxGrammarImpl.ptrOperator);

    assertThatParser()
      .matches("A::*");
  }

  @Test
  public void cvQualifierSeq() {
    setRootRule(CxxGrammarImpl.cvQualifierSeq);

    mockRule(CxxGrammarImpl.cvQualifier);

    assertThatParser()
      .matches("cvQualifier")
      .matches("cvQualifier cvQualifier");
  }

  @Test
  public void declaratorId() {
    setRootRule(CxxGrammarImpl.declaratorId);

    mockRule(CxxGrammarImpl.idExpression);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.className);

    assertThatParser()
      .matches("idExpression")
      .matches("... idExpression")
      .matches("className")
      .matches("nestedNameSpecifier className");
  }

  @Test
  public void declaratorId_reallife() {
    setRootRule(CxxGrammarImpl.declaratorId);

    assertThatParser()
      .matches("lala<int>")
      .matches("operator==<B>");
  }

  @Test
  public void typeId() {
    setRootRule(CxxGrammarImpl.typeId);

    mockRule(CxxGrammarImpl.typeSpecifierSeq);
    mockRule(CxxGrammarImpl.abstractDeclarator);

    assertThatParser()
      .matches("typeSpecifierSeq")
      .matches("typeSpecifierSeq abstractDeclarator");
  }

  @Test
  public void typeId_reallife() {
    setRootRule(CxxGrammarImpl.typeId);

    assertThatParser()
      .matches("int")
      .matches("int *")
      .matches("int *[3]")
      .matches("int (*)[3]")
      .matches("int *()")
      .matches("int (*)(double)");
  }

  @Test
  public void definingTypeId() {
    setRootRule(CxxGrammarImpl.definingTypeId);

    mockRule(CxxGrammarImpl.definingTypeSpecifierSeq);
    mockRule(CxxGrammarImpl.abstractDeclarator);

    assertThatParser()
      .matches("definingTypeSpecifierSeq")
      .matches("definingTypeSpecifierSeq abstractDeclarator");
  }

  @Test
  public void abstractDeclarator() {
    setRootRule(CxxGrammarImpl.abstractDeclarator);

    mockRule(CxxGrammarImpl.ptrAbstractDeclarator);
    mockRule(CxxGrammarImpl.noptrAbstractDeclarator);
    mockRule(CxxGrammarImpl.parametersAndQualifiers);
    mockRule(CxxGrammarImpl.trailingReturnType);
    mockRule(CxxGrammarImpl.abstractPackDeclarator);

    assertThatParser()
      .matches("ptrAbstractDeclarator")
      .matches("parametersAndQualifiers trailingReturnType")
      .matches("noptrAbstractDeclarator parametersAndQualifiers trailingReturnType")
      .matches("abstractPackDeclarator");
  }

  @Test
  public void ptrAbstractDeclarator() {
    setRootRule(CxxGrammarImpl.ptrAbstractDeclarator);

    mockRule(CxxGrammarImpl.noptrAbstractDeclarator);
    mockRule(CxxGrammarImpl.ptrOperator);

    assertThatParser()
      .matches("ptrOperator")
      .matches("ptrOperator ptrOperator")
      .matches("ptrOperator noptrAbstractDeclarator")
      .matches("ptrOperator ptrOperator noptrAbstractDeclarator")
      .matches("noptrAbstractDeclarator");
  }

  @Test
  public void noptrAbstractDeclarator() {
    setRootRule(CxxGrammarImpl.noptrAbstractDeclarator);

    mockRule(CxxGrammarImpl.parametersAndQualifiers);
    mockRule(CxxGrammarImpl.constantExpression);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.ptrAbstractDeclarator);

    assertThatParser()
      .matches("parametersAndQualifiers")
      .matches("( ptrAbstractDeclarator ) parametersAndQualifiers")
      .matches("[ ]")
      .matches("[ constantExpression ]")
      .matches("[ constantExpression ] attributeSpecifierSeq")
      .matches("( ptrAbstractDeclarator ) [ constantExpression ] attributeSpecifierSeq")
      .matches("( ptrAbstractDeclarator )");
  }

  @Test
  public void abstractPackDeclarator() {
    setRootRule(CxxGrammarImpl.abstractPackDeclarator);

    mockRule(CxxGrammarImpl.noptrAbstractPackDeclarator);
    mockRule(CxxGrammarImpl.ptrOperator);

    assertThatParser()
      .matches("noptrAbstractPackDeclarator")
      .matches("ptrOperator noptrAbstractPackDeclarator")
      .matches("ptrOperator ptrOperator noptrAbstractPackDeclarator");
  }

  @Test
  public void noptrAbstractPackDeclarator() {
    setRootRule(CxxGrammarImpl.noptrAbstractPackDeclarator);

    mockRule(CxxGrammarImpl.parametersAndQualifiers);
    mockRule(CxxGrammarImpl.constantExpression);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThatParser()
      .matches("...")
      .matches("... parametersAndQualifiers")
      .matches("... [ ] ")
      .matches("... [ constantExpression ] ")
      .matches("... [ constantExpression ] attributeSpecifierSeq");
  }

  @Test
  public void parameterDeclarationList() {
    setRootRule(CxxGrammarImpl.parameterDeclarationList);

    mockRule(CxxGrammarImpl.parameterDeclaration);

    assertThatParser()
      .matches("parameterDeclaration")
      .matches("parameterDeclaration , parameterDeclaration");
  }

  @Test
  public void parameterDeclarationList_reallife() {
    setRootRule(CxxGrammarImpl.parameterDeclarationList);

    assertThatParser()
      .matches("ostream& strm, const int& i")
      .matches("string, bool");
  }

  @Test
  public void parameterDeclarationClause() {
    setRootRule(CxxGrammarImpl.parameterDeclarationClause);

    mockRule(CxxGrammarImpl.parameterDeclarationList);

    assertThatParser()
      .matches("")
      .matches("parameterDeclarationList")
      .matches("...")
      .matches("parameterDeclarationList ...")
      .matches("parameterDeclarationList , ...");
  }

  @Test
  public void parameterDeclarationClause_reallife() {
    setRootRule(CxxGrammarImpl.parameterDeclarationClause);

    assertThatParser()
      .matches("ostream& strm, const int& i")
      .matches("string, bool");
  }

  @Test
  public void parameterDeclaration() {
    setRootRule(CxxGrammarImpl.parameterDeclaration);

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.parameterDeclSpecifierSeq);
    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.initializerClause);
    mockRule(CxxGrammarImpl.abstractDeclarator);

    assertThatParser()
      .matches("parameterDeclSpecifierSeq declarator")
      .matches("attributeSpecifierSeq parameterDeclSpecifierSeq declarator")
      .matches("parameterDeclSpecifierSeq declarator = initializerClause")
      .matches("attributeSpecifierSeq parameterDeclSpecifierSeq declarator = initializerClause")
      .matches("parameterDeclSpecifierSeq")
      .matches("parameterDeclSpecifierSeq abstractDeclarator")
      .matches("attributeSpecifierSeq parameterDeclSpecifierSeq abstractDeclarator")
      .matches("parameterDeclSpecifierSeq = initializerClause")
      .matches("parameterDeclSpecifierSeq abstractDeclarator = initializerClause")
      .matches("attributeSpecifierSeq parameterDeclSpecifierSeq abstractDeclarator = initializerClause");
  }

  @Test
  public void parameterDeclaration_reallife() {
    setRootRule(CxxGrammarImpl.parameterDeclaration);

    assertThatParser()
      .matches("ostream& strm")
      .matches("const int& i")
      .matches("const paramtype<T> param")
      .matches("const auto_ptr<T>& p")
      .matches("string")
      .matches("::P& c")
      .matches("bool (A::*bar)(void)")
      // CLI extension
      .matches("const int^ i")
      .matches("const int% i")
      .matches("const int^% i");
  }

  @Test
  public void functionDefinition() {
    setRootRule(CxxGrammarImpl.functionDefinition);

    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.functionDeclSpecifierSeq);
    mockRule(CxxGrammarImpl.declarator);
    mockRule(CxxGrammarImpl.virtSpecifierSeq);
    mockRule(CxxGrammarImpl.requiresClause);
    mockRule(CxxGrammarImpl.functionBody);

    assertThatParser()
      .matches("declarator functionBody")
      .matches("attributeSpecifierSeq declarator functionBody")
      .matches("attributeSpecifierSeq functionDeclSpecifierSeq declarator functionBody")
      .matches("attributeSpecifierSeq functionDeclSpecifierSeq declarator virtSpecifierSeq functionBody")
      .matches("declarator requiresClause functionBody")
      .matches("attributeSpecifierSeq declarator requiresClause functionBody")
      .matches("attributeSpecifierSeq functionDeclSpecifierSeq declarator requiresClause functionBody");
  }

  @Test
  public void functionDefinition_reallife() {
    setRootRule(CxxGrammarImpl.functionDefinition);

    assertThatParser()
      .matches("int foo(){}")
      .matches("int A::foo(){}")
      .matches("static int foo(){}")
      .matches("main(){}")
      .matches("int max(int a, int b, int c) { int m = (a > b) ? a : b; return (m > c) ? m : c; }")
      .matches("AddValue (const T& v) : theValue(v) {}")
      .matches("void operator[] () {}")
      .matches("void operator() (T& elem) const {elem += theValue;}")
      .matches("int main(){}")
      .matches("virtual const char* what() const throw() { return \"read empty stack\"; }")
      .matches("void foo() override {}")
      .matches("void foo(::P& c) {}")
      .matches(
        "auto equal_range(ForwardIterator first, ForwardIterator last, const Type& value) -> std::pair<ForwardIterator, ForwardIterator> { return pair; }")
      .matches("auto to_string(int value) -> std::string { return \"\"; }")
      .matches("auto size() const -> std::size_t { return 0; }");
//  ToDo : make this work
//    .matches("auto str() const -> const char* { return nullptr; }");
//    .matches("auto std::map::at(const key_type& key) -> mapped_type& { return value; }");
  }

  @Test
  public void functionBody() {
    setRootRule(CxxGrammarImpl.functionBody);

    mockRule(CxxGrammarImpl.compoundStatement);
    mockRule(CxxGrammarImpl.ctorInitializer);
    mockRule(CxxGrammarImpl.functionTryBlock);

    assertThatParser()
      .matches("compoundStatement")
      .matches("ctorInitializer compoundStatement")
      .matches("functionTryBlock")
      .matches("= default ;")
      .matches("= delete ;");
  }

  @Test
  public void functionBody_reallife() {
    setRootRule(CxxGrammarImpl.functionBody);

    assertThatParser()
      .matches("{ /* ... */ }")
      .matches(": lala(0) {}")
      .matches("{ return \"read empty stack\"; }");
  }

  @Test
  public void initializer_reallife() {
    setRootRule(CxxGrammarImpl.initializer);

    assertThatParser()
      .matches("(new int(42))")
      .matches("((istream_iterator<string>(cin)), istream_iterator<string>())");
  }

  @Test
  public void initializerClause_reallife() {
    setRootRule(CxxGrammarImpl.initializerClause);

    assertThatParser()
      .matches("(istream_iterator<string>(cin))")
      .matches("istream_iterator<string>()");
  }

  @Test
  public void initializerClause() {
    setRootRule(CxxGrammarImpl.initializerClause);

    mockRule(CxxGrammarImpl.assignmentExpression);
    mockRule(CxxGrammarImpl.initializerList);
    mockRule(CxxGrammarImpl.constantExpression);

    assertThatParser()
      .matches("{}")
      .matches("{ initializerList }")
      .matches("assignmentExpression");
  }

  @Test
  public void initializerList() {
    setRootRule(CxxGrammarImpl.initializerList);

    mockRule(CxxGrammarImpl.initializerClause);

    assertThatParser()
      .matches("initializerClause")
      .matches("initializerClause ...")
      .matches("initializerClause , initializerClause")
      .matches("initializerClause , initializerClause ...");
  }

  @Test
  public void initializerList_reallife() {
    setRootRule(CxxGrammarImpl.initializerList);

    assertThatParser()
      .matches("(istream_iterator<string>(cin)), istream_iterator<string>()");
  }

  @Test
  public void designatedInitializerList() {
    setRootRule(CxxGrammarImpl.designatedInitializerList);

    mockRule(CxxGrammarImpl.designatedInitializerClause);

    assertThatParser()
      .matches("designatedInitializerClause")
      .matches("designatedInitializerClause , designatedInitializerClause");
  }

  @Test
  public void designatedInitializerClause_reallife() {
    setRootRule(CxxGrammarImpl.designatedInitializerClause);

    assertThatParser()
      .matches(".name = string(\"Something\")")
      .matches(".name = {1}")
      .matches(".name{2}")
      // C-COMPATIBILITY: C99 designated initializers
      .matches("[5] = {}")
      // EXTENSION: gcc's designated initializers range
      .matches(".values = { [4] = 5, [5 ... 7] = 1, [2] = 0 }");
  }

  @Test
  public void designator() {
    setRootRule(CxxGrammarImpl.designator);

    mockRule(CxxGrammarImpl.constantExpression);

    assertThatParser()
      .matches(". IDENTIFIER")
      .matches(". IDENTIFIER . IDENTIFIER")
      // C99 designated initializers
      .matches("[ constantExpression ]")
      .matches("[ constantExpression ] . IDENTIFIER")
      .matches("[ constantExpression ] . IDENTIFIER . IDENTIFIER")
      // EXTENSION: gcc's designated initializers range
      .matches("[ constantExpression ... constantExpression ]");
  }

  @Test
  public void bracedInitList() {
    setRootRule(CxxGrammarImpl.bracedInitList);

    mockRule(CxxGrammarImpl.initializerList);
    mockRule(CxxGrammarImpl.designatedInitializerList);

    assertThatParser()
      .matches("{}")
      .matches("{ initializerList }")
      .matches("{ initializerList , }")
      .matches("{ designatedInitializerList }")
      .matches("{ designatedInitializerList , }");
  }

}
