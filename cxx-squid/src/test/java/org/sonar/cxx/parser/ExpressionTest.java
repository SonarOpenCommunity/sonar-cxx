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

public class ExpressionTest extends ParserBaseTestHelper {

  @Test
  public void primaryExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.primaryExpression));

    mockRule(CxxGrammarImpl.LITERAL);
    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.compoundStatement);
    mockRule(CxxGrammarImpl.idExpression);
    mockRule(CxxGrammarImpl.lambdaExpression);
    mockRule(CxxGrammarImpl.foldExpression);

    assertThat(p)
      .matches("LITERAL")
      .matches("this")
      .matches("( expression )")
      .matches("idExpression")
      .matches("lambdaExpression")
      .matches("foldExpression");
  }

  @Test
  public void foldExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.foldExpression));

    mockRule(CxxGrammarImpl.castExpression);
    mockRule(CxxGrammarImpl.foldOperator);

    assertThat(p)
      .matches("( castExpression foldOperator ... )")
      .matches("( ... foldOperator castExpression )")
      .matches("( castExpression foldOperator ... foldOperator castExpression )");
  }

  @Test
  public void primaryExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.primaryExpression));

    assertThat(p).matches("(istream_iterator<string>(cin))");

    // GCCs extension: statement expression
    assertThat(p).matches("({ int i = 0; a = i++; })");
  }

  @Test
  public void idExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.idExpression));

    assertThat(p).matches("numeric_limits<char>::is_signed");
    assertThat(p).matches("foo<int>");
    assertThat(p).matches("operator==<B>");
  }

  @Test
  public void unqualifiedId() {
    p.setRootRule(g.rule(CxxGrammarImpl.unqualifiedId));

    mockRule(CxxGrammarImpl.operatorFunctionId);
    mockRule(CxxGrammarImpl.conversionFunctionId);
    mockRule(CxxGrammarImpl.literalOperatorId);
    mockRule(CxxGrammarImpl.className);
    mockRule(CxxGrammarImpl.decltypeSpecifier);
    mockRule(CxxGrammarImpl.templateId);

    assertThat(p).matches("foo");
    assertThat(p).matches("operatorFunctionId");
    assertThat(p).matches("conversionFunctionId");
    assertThat(p).matches("literalOperatorId");
    assertThat(p).matches("~ className");
    assertThat(p).matches("~ decltypeSpecifier");
    assertThat(p).matches("templateId");
  }

  @Test
  public void unqualifiedId_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.unqualifiedId));
    assertThat(p).matches("foo<int>");
    assertThat(p).matches("operator==<B>");
  }

  @Test
  public void qualifiedId() {
    p.setRootRule(g.rule(CxxGrammarImpl.qualifiedId));

    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.unqualifiedId);

    assertThat(p).matches("nestedNameSpecifier unqualifiedId");
    assertThat(p).matches("nestedNameSpecifier template unqualifiedId");
  }

  @Test
  public void qualifiedId_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.qualifiedId));

    assertThat(p).matches("numeric_limits<char>::is_signed");
  }

  @Test
  public void nestedNameSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.nestedNameSpecifier));

    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.namespaceName);
    mockRule(CxxGrammarImpl.decltypeSpecifier);
    mockRule(CxxGrammarImpl.simpleTemplateId);

    assertThat(p).matches(":: typeName ::");
    assertThat(p).matches("typeName ::");
    assertThat(p).matches(":: namespaceName ::");
    assertThat(p).matches("namespaceName ::");
    assertThat(p).matches("decltypeSpecifier ::");
    assertThat(p).matches("typeName :: foo ::");
    assertThat(p).matches("namespaceName :: simpleTemplateId ::");
  }

  @Test
  public void postfixExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.postfixExpression));

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
    mockRule(CxxGrammarImpl.pseudoDestructorName);
    mockRule(CxxGrammarImpl.cudaKernel);

    assertThat(p).matches("primaryExpression");
    assertThat(p).matches("simpleTypeSpecifier ( )");
    assertThat(p).matches("simpleTypeSpecifier ( expressionList )");
    assertThat(p).matches("typenameSpecifier ( )");
    assertThat(p).matches("typenameSpecifier ( expressionList )");
    assertThat(p).matches("simpleTypeSpecifier bracedInitList");
    assertThat(p).matches("typenameSpecifier bracedInitList");
    assertThat(p).matches("dynamic_cast < typeId > ( expression )");
    assertThat(p).matches("static_cast < typeId > ( expression )");
    assertThat(p).matches("reinterpret_cast < typeId > ( expression )");
    assertThat(p).matches("const_cast < typeId > ( expression )");
    assertThat(p).matches("typeid ( expression )");
    assertThat(p).matches("typeid ( typeId )");

    assertThat(p).matches("primaryExpression [ exprOrBracedInitList ]");
    assertThat(p).matches("primaryExpression ( )");
    assertThat(p).matches("primaryExpression ( expressionList )");
    assertThat(p).matches("primaryExpression . idExpression");
    assertThat(p).matches("primaryExpression . template idExpression");
    assertThat(p).matches("primaryExpression . pseudoDestructorName");
    assertThat(p).matches("primaryExpression -> idExpression");
    assertThat(p).matches("primaryExpression -> template idExpression");
    assertThat(p).matches("primaryExpression -> pseudoDestructorName");
    assertThat(p).matches("primaryExpression ++");
    assertThat(p).matches("primaryExpression --");

    // CUDA
    assertThat(p).matches("simpleTypeSpecifier cudaKernel ( expressionList )");
  }

  @Test
  public void postfixExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.postfixExpression));

    assertThat(p).matches("usedColors[(Color)c]");
    assertThat(p).matches("foo()->i");
    assertThat(p).matches("dynamic_cast<Type*>(myop)->op()");
    assertThat(p).matches("::foo()");
    assertThat(p).matches("obj.foo<int>()");
    assertThat(p).matches("typeid(int)");

    // C++/CLI
    assertThat(p).matches("G::typeid");
    assertThat(p).matches("int::typeid");

    // CUDA
    assertThat(p).matches("kernel<<<gridDim,blockDim,0>>>(d_data, height, width)");
  }

  @Test
  public void expressionList_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.expressionList));

    assertThat(p).matches("(istream_iterator<string>(cin)), istream_iterator<string>()");
  }

  @Test
  public void pseudoDestructorName() {
    p.setRootRule(g.rule(CxxGrammarImpl.pseudoDestructorName));

    mockRule(CxxGrammarImpl.typeName);
    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.simpleTemplateId);
    mockRule(CxxGrammarImpl.decltypeSpecifier);

    assertThat(p).matches("typeName :: ~ typeName");
    assertThat(p).matches("nestedNameSpecifier typeName :: ~ typeName");
    assertThat(p).matches("nestedNameSpecifier template simpleTemplateId :: ~ typeName");
    assertThat(p).matches("~ typeName");
    assertThat(p).matches("~ decltypeSpecifier");
  }

  @Test
  public void unaryExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.unaryExpression));

    mockRule(CxxGrammarImpl.postfixExpression);
    mockRule(CxxGrammarImpl.castExpression);
    mockRule(CxxGrammarImpl.unaryOperator);
    mockRule(CxxGrammarImpl.typeId);
    mockRule(CxxGrammarImpl.noexceptExpression);
    mockRule(CxxGrammarImpl.newExpression);
    mockRule(CxxGrammarImpl.deleteExpression);

    assertThat(p).matches("postfixExpression");
    assertThat(p).matches("sizeof postfixExpression");
  }

  @Test
  public void unaryExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.unaryExpression));

    assertThat(p).matches("(istream_iterator<string>(cin))");
    assertThat(p).matches("~CDB::mask");
  }

  @Test
  public void newExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.newExpression));

    mockRule(CxxGrammarImpl.newPlacement);
    mockRule(CxxGrammarImpl.newTypeId);
    mockRule(CxxGrammarImpl.newInitializer);
    mockRule(CxxGrammarImpl.typeId);

    assertThat(p).matches(":: new newPlacement newTypeId newInitializer");
    assertThat(p).matches(":: new newPlacement ( typeId ) newInitializer");
  }

  @Test
  public void newExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.newExpression));

    assertThat(p).matches("new Table()");
    assertThat(p).matches("new Table");
    assertThat(p).matches("new(Table)");
  }

  @Test
  public void newDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.newDeclarator));

    mockRule(CxxGrammarImpl.ptrOperator);
    mockRule(CxxGrammarImpl.noptrNewDeclarator);

    assertThat(p).matches("ptrOperator ptrOperator noptrNewDeclarator");
    assertThat(p).matches("ptrOperator ptrOperator");
    assertThat(p).matches("ptrOperator");
    assertThat(p).matches("ptrOperator noptrNewDeclarator");
    assertThat(p).matches("noptrNewDeclarator");
  }

  @Test
  public void noptrNewDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.noptrNewDeclarator));

    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);
    mockRule(CxxGrammarImpl.constantExpression);

    assertThat(p).matches("[ expression ]");
    assertThat(p).matches("[ expression ] attributeSpecifierSeq");
    assertThat(p).matches("[ expression ] attributeSpecifierSeq [ constantExpression ]");
    assertThat(p).matches("[ expression ] attributeSpecifierSeq [ constantExpression ] attributeSpecifierSeq");
  }

  @Test
  public void newInitializer() {
    p.setRootRule(g.rule(CxxGrammarImpl.newInitializer));

    mockRule(CxxGrammarImpl.expressionList);
    mockRule(CxxGrammarImpl.bracedInitList);

    assertThat(p).matches("(  )");
    assertThat(p).matches("( expressionList )");
    assertThat(p).matches("bracedInitList");
  }

  @Test
  public void deleteExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.deleteExpression));

    mockRule(CxxGrammarImpl.castExpression);

    assertThat(p).matches(":: delete castExpression");
    assertThat(p).matches(":: delete [ ] castExpression");
  }

  @Test
  public void expression() {
    p.setRootRule(g.rule(CxxGrammarImpl.expression));
    mockRule(CxxGrammarImpl.assignmentExpression);

    assertThat(p).matches("assignmentExpression");
    assertThat(p).matches("assignmentExpression, assignmentExpression");
    assertThat(p).matches("assignmentExpression, assignmentExpression, assignmentExpression");
  }

  @Test
  public void expression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.expression));

    assertThat(p).matches("1 + 1");
    assertThat(p).matches("(1 + 1) * 2");
    assertThat(p).matches("arr[i]");
    assertThat(p).matches("( y > 4)");
    assertThat(p).matches("( x== 8) && (c=='U')");
    assertThat(p).matches("(a > b) ? a : b");
    assertThat(p).matches("a ? : b");
    assertThat(p).matches("m = 1");
    assertThat(p).matches("cout << endl");
    assertThat(p).matches("numeric_limits<char>::is_signed");
    assertThat(p).matches("cout << numeric_limits<char>::is_signed << endl");
    assertThat(p).matches("usedColors[(Color)c]");
    assertThat(p).matches("(Color)c");
    assertThat(p).matches("foo()->i");
    assertThat(p).matches("which ^= 1u");
    assertThat(p).matches("p = nullptr");
  }

  @Test
  public void assignmentExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.assignmentExpression));
    mockRule(CxxGrammarImpl.conditionalExpression);
    mockRule(CxxGrammarImpl.logicalOrExpression);
    mockRule(CxxGrammarImpl.assignmentOperator);
    mockRule(CxxGrammarImpl.initializerClause);
    mockRule(CxxGrammarImpl.throwExpression);

    assertThat(p).matches("conditionalExpression");
    assertThat(p).matches("logicalOrExpression assignmentOperator initializerClause");
    assertThat(p).matches("throwExpression");
  }

  @Test
  public void assignmentExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.assignmentExpression));

    assertThat(p).matches("i=0");
    assertThat(p).matches("(istream_iterator<string>(cin))");
    assertThat(p).matches("which ^= 1u");
    assertThat(p).matches("p = nullptr");
  }

  @Test
  public void logicalOrExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.logicalOrExpression));
    mockRule(CxxGrammarImpl.logicalAndExpression);

    assertThat(p).matches("logicalAndExpression");
    assertThat(p).matches("logicalAndExpression || logicalAndExpression");
  }

  @Test
  public void logicalOrExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.logicalOrExpression));

    assertThat(p).matches("(istream_iterator<string>(cin))");
  }

  @Test
  public void conditionalExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.conditionalExpression));

    mockRule(CxxGrammarImpl.logicalOrExpression);
    mockRule(CxxGrammarImpl.expression);
    mockRule(CxxGrammarImpl.assignmentExpression);

    assertThat(p).matches("logicalOrExpression");
    assertThat(p).matches("logicalOrExpression ? expression : assignmentExpression");
  }

  @Test
  public void constantExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.constantExpression));

    mockRule(CxxGrammarImpl.conditionalExpression);

    assertThat(p).matches("conditionalExpression");
  }

  @Test
  public void constantExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.constantExpression));

    assertThat(p).matches("__cplusplus");
  }

  @Test
  public void logicalAndExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.logicalAndExpression));
    mockRule(CxxGrammarImpl.inclusiveOrExpression);

    assertThat(p).matches("inclusiveOrExpression");
    assertThat(p).matches("inclusiveOrExpression && inclusiveOrExpression");
  }

  @Test
  public void inclusiveOrExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.inclusiveOrExpression));
    mockRule(CxxGrammarImpl.exclusiveOrExpression);

    assertThat(p).matches("exclusiveOrExpression");
    assertThat(p).matches("exclusiveOrExpression | exclusiveOrExpression");
  }

  @Test
  public void exclusiveOrExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.exclusiveOrExpression));
    mockRule(CxxGrammarImpl.andExpression);

    assertThat(p).matches("andExpression");
    assertThat(p).matches("andExpression ^ andExpression");
  }

  @Test
  public void andExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.andExpression));
    mockRule(CxxGrammarImpl.equalityExpression);

    assertThat(p).matches("equalityExpression");
    assertThat(p).matches("equalityExpression & equalityExpression");
  }

  @Test
  public void equalityExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.equalityExpression));
    mockRule(CxxGrammarImpl.relationalExpression);

    assertThat(p).matches("relationalExpression");
    assertThat(p).matches("relationalExpression == relationalExpression");
    assertThat(p).matches("relationalExpression != relationalExpression");
  }

  @Test
  public void relationalExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.relationalExpression));
    mockRule(CxxGrammarImpl.shiftExpression);

    assertThat(p).matches("shiftExpression");
    assertThat(p).matches("shiftExpression < shiftExpression");
    assertThat(p).matches("shiftExpression > shiftExpression");
    assertThat(p).matches("shiftExpression <= shiftExpression");
    assertThat(p).matches("shiftExpression >= shiftExpression");
  }

  @Test
  public void shiftExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.shiftExpression));
    mockRule(CxxGrammarImpl.additiveExpression);

    assertThat(p).matches("additiveExpression");
    assertThat(p).matches("additiveExpression << additiveExpression");
    assertThat(p).matches("additiveExpression >> additiveExpression");
  }

  @Test
  public void additiveExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.additiveExpression));
    mockRule(CxxGrammarImpl.multiplicativeExpression);

    assertThat(p).matches("multiplicativeExpression");
    assertThat(p).matches("multiplicativeExpression + multiplicativeExpression");
    assertThat(p).matches("multiplicativeExpression - multiplicativeExpression");
  }

  @Test
  public void multiplicativeExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.multiplicativeExpression));
    mockRule(CxxGrammarImpl.pmExpression);

    assertThat(p).matches("pmExpression");
    assertThat(p).matches("pmExpression * pmExpression");
    assertThat(p).matches("pmExpression / pmExpression");
    assertThat(p).matches("pmExpression % pmExpression");
  }

  @Test
  public void multiplicativeExpression_reallive() {
    p.setRootRule(g.rule(CxxGrammarImpl.multiplicativeExpression));

    assertThat(p).matches("N / 1");
  }

  @Test
  public void pmExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.pmExpression));
    mockRule(CxxGrammarImpl.castExpression);

    assertThat(p).matches("castExpression");
    assertThat(p).matches("castExpression .* castExpression");
    assertThat(p).matches("castExpression ->* castExpression");
  }

  @Test
  public void castExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.pmExpression));
    mockRule(CxxGrammarImpl.unaryExpression);
    mockRule(CxxGrammarImpl.typeId);
    mockRule(CxxGrammarImpl.bracedInitList);

    assertThat(p).matches("unaryExpression");
    assertThat(p).matches("(typeId) unaryExpression");
    assertThat(p).matches("(typeId)(typeId) unaryExpression");

    // C-COMPATIBILITY: C99 compound literals
    assertThat(p).matches("(typeId) bracedInitList");
  }

  @Test
  public void castExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.castExpression));

    assertThat(p).matches("(int)c");
    assertThat(p).matches("(unsigned int)c");
    assertThat(p).matches("(const char*)c");
    assertThat(p).matches("(Color)c");
    assertThat(p).matches("CDB::mask");
    assertThat(p).matches("(istream_iterator<string>(cin))");
    assertThat(p).matches("(int (*const [])(unsigned int, ...))f");

    // C-COMPATIBILITY: C99 compound literals
    assertThat(p).matches("(Point){ 400, 200 }");
    assertThat(p).matches("(struct Point){ 400, 200 }");
    assertThat(p).matches("(struct foo) {x + y, 'a', 0}");
    assertThat(p).matches("(int []){ 1, 2, 4, 8 }");
    assertThat(p).matches("(int [3]) {1}");
    assertThat(p).matches("(const float []){1e0, 1e1, 1e2}");
  }
}
