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

public class ExpressionTest extends ParserBaseTestHelper {

  @Test
  public void primaryExpression() {
    setRootRule(CxxGrammarImpl.primaryExpression);

    mockRule(CxxGrammarImpl.LITERAL);
    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.compoundStatement);
    mockRule(CxxGrammarImpl.idExpression);
    mockRule(CxxGrammarImpl.lambdaExpression);
    mockRule(CxxGrammarImpl.foldExpression);
    mockRule(CxxGrammarImpl.requiresExpression);

    assertThatParser()
      .matches("LITERAL")
      .matches("this")
      .matches("( expression )")
      .matches("idExpression")
      .matches("lambdaExpression")
      .matches("foldExpression")
      .matches("requiresExpression");
  }

  @Test
  public void primaryExpression_reallife() {
    setRootRule(CxxGrammarImpl.primaryExpression);

    assertThatParser()
      .matches("(istream_iterator<string>(cin))")
      // GCCs extension: statement expression
      .matches("({ int i = 0; a = i++; })");
  }

  @Test
  public void foldExpression() {
    setRootRule(CxxGrammarImpl.foldExpression);

    mockRule(CxxGrammarImpl.castExpression);
    mockRule(CxxGrammarImpl.foldOperator);

    assertThatParser()
      .matches("( castExpression foldOperator ... )")
      .matches("( ... foldOperator castExpression )")
      .matches("( castExpression foldOperator ... foldOperator castExpression )");
  }

  @Test
  public void requiresExpression() {
    setRootRule(CxxGrammarImpl.requiresExpression);

    mockRule(CxxGrammarImpl.requirementParameterList);
    mockRule(CxxGrammarImpl.requirementBody);

    assertThatParser()
      .matches("requires requirementBody")
      .matches("requires requirementParameterList requirementBody");
  }

  @Test
  public void requirementParameterList() {
    setRootRule(CxxGrammarImpl.requirementParameterList);

    mockRule(CxxGrammarImpl.parameterDeclarationClause);

    assertThatParser()
      .matches("( )")
      .matches("( parameterDeclarationClause )");
  }

  @Test
  public void requirementBody() {
    setRootRule(CxxGrammarImpl.requirementBody);

    mockRule(CxxGrammarImpl.requirementSeq);

    assertThatParser()
      .matches("{ requirementSeq }");
  }

  @Test
  public void requirementSeq() {
    setRootRule(CxxGrammarImpl.requirementSeq);

    mockRule(CxxGrammarImpl.requirement);

    assertThatParser()
      .matches("requirement")
      .matches("requirement requirement");

  }

  @Test
  public void requirement() {
    setRootRule(CxxGrammarImpl.requirement);

    mockRule(CxxGrammarImpl.simpleRequirement);
    mockRule(CxxGrammarImpl.typeRequirement);
    mockRule(CxxGrammarImpl.compoundRequirement);
    mockRule(CxxGrammarImpl.nestedRequirement);

    assertThatParser()
      .matches("simpleRequirement")
      .matches("typeRequirement")
      .matches("compoundRequirement")
      .matches("nestedRequirement");
  }

  @Test
  public void simpleRequirement() {
    setRootRule(CxxGrammarImpl.simpleRequirement);

    mockRule(CxxGrammarImpl.expression);

    assertThatParser()
      .matches("expression ;");
  }

  @Test
  public void typeRequirement() {
    setRootRule(CxxGrammarImpl.typeRequirement);

    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.typeName);

    assertThatParser()
      .matches("typename typeName ;")
      .matches("typename nestedNameSpecifier typeName ;");
  }

  @Test
  public void compoundRequirement() {
    setRootRule(CxxGrammarImpl.compoundRequirement);

    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.returnTypeRequirement);

    assertThatParser()
      .matches("{ expression } ;")
      .matches("{ expression } noexcept ;")
      .matches("{ expression } returnTypeRequirement ;")
      .matches("{ expression } noexcept returnTypeRequirement ;");
  }

  public void returnTypeRequirement() {
    setRootRule(CxxGrammarImpl.returnTypeRequirement);

    mockRule(CxxGrammarImpl.typeConstraint);

    assertThatParser()
      .matches("-> typeConstraint");
  }

  @Test
  public void nestedRequirement() {
    setRootRule(CxxGrammarImpl.nestedRequirement);

    mockRule(CxxGrammarImpl.constraintExpression);

    assertThatParser()
      .matches("requires constraintExpression ;");
  }

  @Test
  public void idExpression_reallife() {
    setRootRule(CxxGrammarImpl.idExpression);

    assertThatParser()
      .matches("numeric_limits<char>::is_signed")
      .matches("foo<int>")
      .matches("operator==<B>");
  }

  @Test
  public void unqualifiedId() {
    setRootRule(CxxGrammarImpl.unqualifiedId);

    mockRule(CxxGrammarImpl.operatorFunctionId);
    mockRule(CxxGrammarImpl.conversionFunctionId);
    mockRule(CxxGrammarImpl.literalOperatorId);
    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.decltypeSpecifier);
    mockRule(CxxGrammarImpl.templateId);

    assertThatParser()
      .matches("foo")
      .matches("operatorFunctionId")
      .matches("conversionFunctionId")
      .matches("literalOperatorId")
      .matches("~ typeName")
      .matches("~ decltypeSpecifier")
      .matches("templateId");
  }

  @Test
  public void unqualifiedId_reallife() {
    setRootRule(CxxGrammarImpl.unqualifiedId);

    assertThatParser()
      .matches("foo<int>")
      .matches("operator==<B>");
  }

  @Test
  public void qualifiedId() {
    setRootRule(CxxGrammarImpl.qualifiedId);

    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.unqualifiedId);

    assertThatParser()
      .matches("nestedNameSpecifier unqualifiedId")
      .matches("nestedNameSpecifier template unqualifiedId");
  }

  @Test
  public void qualifiedId_reallife() {
    setRootRule(CxxGrammarImpl.qualifiedId);

    assertThatParser()
      .matches("numeric_limits<char>::is_signed");
  }

  @Test
  public void nestedNameSpecifier() {
    setRootRule(CxxGrammarImpl.nestedNameSpecifier);

    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.namespaceName);
    mockRule(CxxGrammarImpl.decltypeSpecifier);
    mockRule(CxxGrammarImpl.simpleTemplateId);

    assertThatParser()
      // basic
      .matches("::")
      .matches("typeName ::")
      .matches("namespaceName ::")
      .matches("decltypeSpecifier ::")
      // nested-name-specifier identifier ::
      .matches(":: foo ::")
      .matches("typeName :: foo ::")
      .matches("namespaceName :: foo ::")
      .matches("decltypeSpecifier :: foo ::")
      // nested-name-specifier simple-template-id ::
      .matches(":: simpleTemplateId ::")
      .matches("typeName :: simpleTemplateId ::")
      .matches("namespaceName :: simpleTemplateId ::")
      .matches("decltypeSpecifier :: simpleTemplateId ::")
      // nested-name-specifier template simple-template-id ::
      .matches(":: template simpleTemplateId ::")
      .matches("typeName :: template simpleTemplateId ::")
      .matches("namespaceName :: template simpleTemplateId ::")
      .matches("decltypeSpecifier :: template simpleTemplateId ::")
      // some deeper nested tests
      .matches(":: foo1 :: foo2 :: foo3 :: foo4 ::")
      .matches("typeName :: foo2 :: foo3 :: foo4 ::")
      .matches("namespaceName :: foo2 :: foo3 :: foo4 ::")
      .matches("decltypeSpecifier :: foo2 :: foo3 :: foo4 ::")
      .matches(":: foo1 :: simpleTemplateId :: foo2 :: simpleTemplateId ::")
      .matches(":: foo1 :: template simpleTemplateId :: foo2 :: template simpleTemplateId ::");
  }

  @Test
  public void postfixExpression() {
    setRootRule(CxxGrammarImpl.postfixExpression);

    mockRule(CxxGrammarImpl.primaryExpression);
    mockRule(CxxGrammarImpl.simpleTypeSpecifier);
    mockRule(CxxGrammarImpl.expressionList);
    mockRule(CxxGrammarImpl.typenameSpecifier);
    mockRule(CxxGrammarImpl.bracedInitList);
    mockRule(CxxGrammarImpl.primaryExpression);
    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.idExpression);
    mockRule(CxxGrammarImpl.typeId);
    mockRule(CxxGrammarImpl.exprOrBracedInitList);
    mockRule(CxxGrammarImpl.cudaKernel);

    assertThatParser()
      .matches("primaryExpression")
      .matches("simpleTypeSpecifier ( )")
      .matches("simpleTypeSpecifier ( expressionList )")
      .matches("typenameSpecifier ( )")
      .matches("typenameSpecifier ( expressionList )")
      .matches("simpleTypeSpecifier bracedInitList")
      .matches("typenameSpecifier bracedInitList")
      .matches("dynamic_cast < typeId > ( expression )")
      .matches("static_cast < typeId > ( expression )")
      .matches("reinterpret_cast < typeId > ( expression )")
      .matches("const_cast < typeId > ( expression )")
      .matches("typeid ( expression )")
      .matches("typeid ( typeId )")
      .matches("primaryExpression [ exprOrBracedInitList ]")
      .matches("primaryExpression ( )")
      .matches("primaryExpression ( expressionList )")
      .matches("primaryExpression . idExpression")
      .matches("primaryExpression . template idExpression")
      .matches("primaryExpression -> idExpression")
      .matches("primaryExpression -> template idExpression")
      .matches("primaryExpression ++")
      .matches("primaryExpression --")
      // CUDA
      .matches("simpleTypeSpecifier cudaKernel ( expressionList )");
  }

  @Test
  public void postfixExpression_reallife() {
    setRootRule(CxxGrammarImpl.postfixExpression);

    assertThatParser()
      .matches("usedColors[(Color)c]")
      .matches("foo()->i")
      .matches("dynamic_cast<Type*>(myop)->op()")
      .matches("::foo()")
      .matches("obj.foo<int>()")
      .matches("typeid(int)")
      // C++/CLI
      .matches("G::typeid")
      .matches("int::typeid")
      // CUDA
      .matches("kernel<<<gridDim,blockDim,0>>>(d_data, height, width)");
  }

  @Test
  public void expressionList_reallife() {
    setRootRule(CxxGrammarImpl.expressionList);

    assertThatParser()
      .matches("(istream_iterator<string>(cin)), istream_iterator<string>()");
  }

  @Test
  public void unaryExpression() {
    setRootRule(CxxGrammarImpl.unaryExpression);

    mockRule(CxxGrammarImpl.postfixExpression);
    mockRule(CxxGrammarImpl.castExpression);
    mockRule(CxxGrammarImpl.unaryOperator);
    mockRule(CxxGrammarImpl.typeId);
    mockRule(CxxGrammarImpl.noexceptExpression);
    mockRule(CxxGrammarImpl.newExpression);
    mockRule(CxxGrammarImpl.deleteExpression);
    mockRule(CxxGrammarImpl.awaitExpression);

    assertThatParser()
      .matches("postfixExpression")
      .matches("sizeof postfixExpression")
      .matches("awaitExpression")
      .matches("noexceptExpression")
      .matches("newExpression")
      .matches("deleteExpression");
  }

  @Test
  public void unaryExpression_reallife() {
    setRootRule(CxxGrammarImpl.unaryExpression);

    assertThatParser()
      .matches("(istream_iterator<string>(cin))")
      .matches("~CDB::mask");
  }

  @Test
  public void newExpression() {
    setRootRule(CxxGrammarImpl.newExpression);

    mockRule(CxxGrammarImpl.newPlacement);
    mockRule(CxxGrammarImpl.newTypeId);
    mockRule(CxxGrammarImpl.newInitializer);
    mockRule(CxxGrammarImpl.typeId);

    assertThatParser()
      .matches(":: new newPlacement newTypeId newInitializer")
      .matches(":: new newPlacement ( typeId ) newInitializer");
  }

  @Test
  public void newExpression_reallife() {
    setRootRule(CxxGrammarImpl.newExpression);

    assertThatParser()
      .matches("new Table()")
      .matches("new Table")
      .matches("new(Table)")
      .matches("new double[n][5]")
      .matches("new (int (*[10])())")
      .matches("new (std::nothrow) char[8]")
      .matches("new A{1, 2}")
      .matches("new auto('a')")
      .matches("new double[]{1,2,3}")
      .matches("new std::integral auto(1)")
      .matches("new(buf) T")
      .matches("::new(p)A(1,2,3)");
  }

  @Test
  public void newDeclarator() {
    setRootRule(CxxGrammarImpl.newDeclarator);

    mockRule(CxxGrammarImpl.ptrOperator);
    mockRule(CxxGrammarImpl.noptrNewDeclarator);

    assertThatParser()
      .matches("ptrOperator ptrOperator noptrNewDeclarator")
      .matches("ptrOperator ptrOperator")
      .matches("ptrOperator")
      .matches("ptrOperator noptrNewDeclarator")
      .matches("noptrNewDeclarator");
  }

  @Test
  public void noptrNewDeclarator() {
    setRootRule(CxxGrammarImpl.noptrNewDeclarator);

    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.constantExpression);

    assertThatParser()
      .matches("[ expression ]")
      .matches("[ expression ] attributeSpecifierSeq")
      .matches("[ expression ] attributeSpecifierSeq [ constantExpression ]")
      .matches("[ expression ] attributeSpecifierSeq [ constantExpression ] attributeSpecifierSeq");
  }

  @Test
  public void newInitializer() {
    setRootRule(CxxGrammarImpl.newInitializer);

    mockRule(CxxGrammarImpl.expressionList);
    mockRule(CxxGrammarImpl.bracedInitList);

    assertThatParser()
      .matches("(  )")
      .matches("( expressionList )")
      .matches("bracedInitList");
  }

  @Test
  public void deleteExpression() {
    setRootRule(CxxGrammarImpl.deleteExpression);

    mockRule(CxxGrammarImpl.castExpression);

    assertThatParser()
      .matches(":: delete castExpression")
      .matches(":: delete [ ] castExpression");
  }

  @Test
  public void expression() {
    setRootRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.assignmentExpression);

    assertThatParser()
      .matches("assignmentExpression")
      .matches("assignmentExpression, assignmentExpression")
      .matches("assignmentExpression, assignmentExpression, assignmentExpression");
  }

  @Test
  public void expression_reallife() {
    setRootRule(CxxGrammarImpl.expression);

    assertThatParser()
      .matches("1 + 1")
      .matches("(1 + 1) * 2")
      .matches("arr[i]")
      .matches("( y > 4)")
      .matches("( x== 8) && (c=='U')")
      .matches("(a > b) ? a : b")
      .matches("a ? : b")
      .matches("m = 1")
      .matches("cout << endl")
      .matches("numeric_limits<char>::is_signed")
      .matches("cout << numeric_limits<char>::is_signed << endl")
      .matches("usedColors[(Color)c]")
      .matches("(Color)c")
      .matches("foo()->i")
      .matches("which ^= 1u")
      .matches("p = nullptr");
  }

  @Test
  public void assignmentExpression() {
    setRootRule(CxxGrammarImpl.assignmentExpression);

    mockRule(CxxGrammarImpl.conditionalExpression);
    mockRule(CxxGrammarImpl.logicalOrExpression);
    mockRule(CxxGrammarImpl.assignmentOperator);
    mockRule(CxxGrammarImpl.initializerClause);
    mockRule(CxxGrammarImpl.yieldExpression);
    mockRule(CxxGrammarImpl.throwExpression);

    assertThatParser()
      .matches("conditionalExpression")
      .matches("logicalOrExpression assignmentOperator initializerClause")
      .matches("yieldExpression")
      .matches("throwExpression");
  }

  @Test
  public void assignmentExpression_reallife() {
    setRootRule(CxxGrammarImpl.assignmentExpression);

    assertThatParser()
      .matches("i=0")
      .matches("(istream_iterator<string>(cin))")
      .matches("which ^= 1u")
      .matches("p = nullptr");
  }

  @Test
  public void logicalOrExpression() {
    setRootRule(CxxGrammarImpl.logicalOrExpression);
    mockRule(CxxGrammarImpl.logicalAndExpression);

    assertThatParser()
      .matches("logicalAndExpression")
      .matches("logicalAndExpression || logicalAndExpression")
      .matches("logicalAndExpression or logicalAndExpression");
  }

  @Test
  public void logicalOrExpression_reallife() {
    setRootRule(CxxGrammarImpl.logicalOrExpression);

    assertThatParser()
      .matches("(istream_iterator<string>(cin))");
  }

  @Test
  public void conditionalExpression() {
    setRootRule(CxxGrammarImpl.conditionalExpression);

    mockRule(CxxGrammarImpl.logicalOrExpression);
    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.assignmentExpression);

    assertThatParser()
      .matches("logicalOrExpression")
      .matches("logicalOrExpression ? expression : assignmentExpression");
  }

  @Test
  public void constantExpression() {
    setRootRule(CxxGrammarImpl.constantExpression);

    mockRule(CxxGrammarImpl.conditionalExpression);

    assertThatParser()
      .matches("conditionalExpression");
  }

  @Test
  public void constantExpression_reallife() {
    setRootRule(CxxGrammarImpl.constantExpression);

    assertThatParser()
      .matches("__cplusplus");
  }

  @Test
  public void logicalAndExpression() {
    setRootRule(CxxGrammarImpl.logicalAndExpression);
    mockRule(CxxGrammarImpl.inclusiveOrExpression);

    assertThatParser()
      .matches("inclusiveOrExpression")
      .matches("inclusiveOrExpression && inclusiveOrExpression")
      .matches("inclusiveOrExpression and inclusiveOrExpression");
  }

  @Test
  public void inclusiveOrExpression() {
    setRootRule(CxxGrammarImpl.inclusiveOrExpression);
    mockRule(CxxGrammarImpl.exclusiveOrExpression);

    assertThatParser()
      .matches("exclusiveOrExpression")
      .matches("exclusiveOrExpression | exclusiveOrExpression")
      .matches("exclusiveOrExpression bitor exclusiveOrExpression");
  }

  @Test
  public void exclusiveOrExpression() {
    setRootRule(CxxGrammarImpl.exclusiveOrExpression);
    mockRule(CxxGrammarImpl.andExpression);

    assertThatParser()
      .matches("andExpression")
      .matches("andExpression ^ andExpression")
      .matches("andExpression xor andExpression");
  }

  @Test
  public void andExpression() {
    setRootRule(CxxGrammarImpl.andExpression);
    mockRule(CxxGrammarImpl.equalityExpression);

    assertThatParser()
      .matches("equalityExpression")
      .matches("equalityExpression & equalityExpression")
      .matches("equalityExpression bitand equalityExpression");
  }

  @Test
  public void equalityExpression() {
    setRootRule(CxxGrammarImpl.equalityExpression);
    mockRule(CxxGrammarImpl.relationalExpression);

    assertThatParser()
      .matches("relationalExpression")
      .matches("relationalExpression == relationalExpression")
      .matches("relationalExpression != relationalExpression")
      .matches("relationalExpression not_eq relationalExpression");
  }

  @Test
  public void relationalExpression() {
    setRootRule(CxxGrammarImpl.relationalExpression);
    mockRule(CxxGrammarImpl.compareExpression);

    assertThatParser()
      .matches("compareExpression")
      .matches("compareExpression < compareExpression")
      .matches("compareExpression > compareExpression")
      .matches("compareExpression <= compareExpression")
      .matches("compareExpression >= compareExpression");
  }

  @Test
  public void shiftExpression() {
    setRootRule(CxxGrammarImpl.shiftExpression);
    mockRule(CxxGrammarImpl.additiveExpression);

    assertThatParser()
      .matches("additiveExpression")
      .matches("additiveExpression << additiveExpression")
      .matches("additiveExpression >> additiveExpression");
  }

  @Test
  public void compareExpression() {
    setRootRule(CxxGrammarImpl.compareExpression);
    mockRule(CxxGrammarImpl.shiftExpression);

    assertThatParser()
      .matches("shiftExpression")
      .matches("shiftExpression <=> shiftExpression");
  }

  @Test
  public void additiveExpression() {
    setRootRule(CxxGrammarImpl.additiveExpression);
    mockRule(CxxGrammarImpl.multiplicativeExpression);

    assertThatParser()
      .matches("multiplicativeExpression")
      .matches("multiplicativeExpression + multiplicativeExpression")
      .matches("multiplicativeExpression - multiplicativeExpression");
  }

  @Test
  public void multiplicativeExpression() {
    setRootRule(CxxGrammarImpl.multiplicativeExpression);
    mockRule(CxxGrammarImpl.pmExpression);

    assertThatParser()
      .matches("pmExpression")
      .matches("pmExpression * pmExpression")
      .matches("pmExpression / pmExpression")
      .matches("pmExpression % pmExpression");
  }

  @Test
  public void multiplicativeExpression_reallive() {
    setRootRule(CxxGrammarImpl.multiplicativeExpression);

    assertThatParser()
      .matches("N / 1");
  }

  @Test
  public void pmExpression() {
    setRootRule(CxxGrammarImpl.pmExpression);
    mockRule(CxxGrammarImpl.castExpression);

    assertThatParser()
      .matches("castExpression")
      .matches("castExpression .* castExpression")
      .matches("castExpression ->* castExpression");
  }

  @Test
  public void castExpression() {
    setRootRule(CxxGrammarImpl.pmExpression);

    mockRule(CxxGrammarImpl.unaryExpression);
    mockRule(CxxGrammarImpl.typeId);
    mockRule(CxxGrammarImpl.bracedInitList);

    assertThatParser()
      .matches("unaryExpression")
      .matches("(typeId) unaryExpression")
      .matches("(typeId)(typeId) unaryExpression")
      // C-COMPATIBILITY: C99 compound literals
      .matches("(typeId) bracedInitList");
  }

  @Test
  public void castExpression_reallife() {
    setRootRule(CxxGrammarImpl.castExpression);

    assertThatParser()
      .matches("(int)c")
      .matches("(unsigned int)c")
      .matches("(const char*)c")
      .matches("(Color)c")
      .matches("CDB::mask")
      .matches("(istream_iterator<string>(cin))")
      .matches("(int (*const [])(unsigned int, ...))f")
      // C-COMPATIBILITY: C99 compound literals
      .matches("(Point){ 400, 200 }")
      .matches("(struct Point){ 400, 200 }")
      .matches("(struct foo) {x + y, 'a', 0}")
      .matches("(int []){ 1, 2, 4, 8 }")
      .matches("(int [3]) {1}")
      .matches("(const float []){1e0, 1e1, 1e2}");
  }

  @Test
  public void yieldExpression() {
    setRootRule(CxxGrammarImpl.yieldExpression);

    mockRule(CxxGrammarImpl.assignmentExpression);
    mockRule(CxxGrammarImpl.bracedInitList);

    assertThatParser()
      .matches("co_yield assignmentExpression")
      .matches("co_yield bracedInitList");
  }

}
