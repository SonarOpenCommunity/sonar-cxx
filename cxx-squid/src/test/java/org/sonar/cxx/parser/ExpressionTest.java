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

public class ExpressionTest {

  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();

  @Test
  public void primaryExpression() {
    p.setRootRule(g.primaryExpression);

    g.literal.mock();
    g.expression.mock();
    g.idExpression.mock();
    g.lambdaExpression.mock();

    assertThat(p, parse("literal"));
    assertThat(p, parse("this"));
    assertThat(p, parse("( expression )"));
    assertThat(p, parse("idExpression"));
    assertThat(p, parse("lambdaExpression"));
  }

  @Test
  public void primaryExpression_reallife() {
    p.setRootRule(g.primaryExpression);

    assertThat(p, parse("(istream_iterator<string>(cin))"));
  }

  @Test
  public void idExpression_reallife() {
    p.setRootRule(g.idExpression);

    assertThat(p, parse("numeric_limits<char>::is_signed"));
    assertThat(p, parse("foo<int>"));
    assertThat(p, parse("operator==<B>"));
  }

  @Test
  public void unqualifiedId() {
    p.setRootRule(g.unqualifiedId);

    g.operatorFunctionId.mock();
    g.conversionFunctionId.mock();
    g.literalOperatorId.mock();
    g.className.mock();
    g.decltypeSpecifier.mock();
    g.templateId.mock();

    assertThat(p, parse("foo"));
    assertThat(p, parse("operatorFunctionId"));
    assertThat(p, parse("conversionFunctionId"));
    assertThat(p, parse("literalOperatorId"));
    assertThat(p, parse("~ className"));
    assertThat(p, parse("~ decltypeSpecifier"));
    assertThat(p, parse("templateId"));
  }

  @Test
  public void unqualifiedId_reallife() {
    p.setRootRule(g.unqualifiedId);
    assertThat(p, parse("foo<int>"));
    assertThat(p, parse("operator==<B>"));
  }

  @Test
  public void qualifiedId() {
    p.setRootRule(g.qualifiedId);

    g.literal.mock();
    g.nestedNameSpecifier.mock();
    g.unqualifiedId.mock();
    g.operatorFunctionId.mock();
    g.literalOperatorId.mock();
    g.templateId.mock();

    assertThat(p, parse("nestedNameSpecifier unqualifiedId"));
    assertThat(p, parse("nestedNameSpecifier template unqualifiedId"));
    assertThat(p, parse(":: foo"));
    assertThat(p, parse(":: operatorFunctionId"));
    assertThat(p, parse(":: literalOperatorId"));
    assertThat(p, parse(":: templateId"));
  }

  @Test
  public void qualifiedId_reallife() {
    p.setRootRule(g.qualifiedId);

    assertThat(p, parse("numeric_limits<char>::is_signed"));
  }

  @Test
  public void nestedNameSpecifier() {
    p.setRootRule(g.nestedNameSpecifier);

    g.typeName.mock();
    g.namespaceName.mock();
    g.decltypeSpecifier.mock();
    g.simpleTemplateId.mock();

    assertThat(p, parse(":: typeName ::"));
    assertThat(p, parse("typeName ::"));
    assertThat(p, parse(":: namespaceName ::"));
    assertThat(p, parse("namespaceName ::"));
    assertThat(p, parse("decltypeSpecifier ::"));
    assertThat(p, parse("typeName :: foo ::"));
    assertThat(p, parse("namespaceName :: simpleTemplateId ::"));
  }

  @Test
  public void postfixExpression() {
    p.setRootRule(g.postfixExpression);

    g.primaryExpression.mock();
    g.simpleTypeSpecifier.mock();
    g.expressionList.mock();
    g.typenameSpecifier.mock();
    g.bracedInitList.mock();
    g.primaryExpression.mock();
    g.expression.mock();
    g.idExpression.mock();
    g.typeId.mock();
    g.deleteExpression.mock();
    g.pseudoDestructorName.mock();

    assertThat(p, parse("primaryExpression"));
    assertThat(p, parse("primaryExpression [ expression ]"));
    assertThat(p, parse("primaryExpression ( expressionList )"));
    assertThat(p, parse("simpleTypeSpecifier ( expressionList )"));
    assertThat(p, parse("typenameSpecifier ( expressionList )"));
    assertThat(p, parse("simpleTypeSpecifier bracedInitList"));
    assertThat(p, parse("typenameSpecifier bracedInitList"));
    assertThat(p, parse("primaryExpression . template idExpression"));
    assertThat(p, parse("primaryExpression -> template idExpression"));

    assertThat(p, parse("primaryExpression . pseudoDestructorName"));
    assertThat(p, parse("primaryExpression -> pseudoDestructorName"));

    assertThat(p, parse("primaryExpression ++"));
    assertThat(p, parse("primaryExpression --"));
    assertThat(p, parse("dynamic_cast < typeId > ( expression )"));
    assertThat(p, parse("static_cast < typeId > ( expression )"));
    assertThat(p, parse("reinterpret_cast < typeId > ( expression )"));
    assertThat(p, parse("const_cast < typeId > ( expression )"));
    assertThat(p, parse("typeid ( expression )"));
    assertThat(p, parse("typeid ( typeId )"));
  }

  @Test
  public void postfixExpression_reallife() {
    p.setRootRule(g.postfixExpression);

    assertThat(p, parse("usedColors[(Color)c]"));
    assertThat(p, parse("foo()->i"));
    assertThat(p, parse("dynamic_cast<Type*>(myop)->op()"));
    assertThat(p, parse("::foo()"));
    assertThat(p, parse("obj.foo<int>()"));
  }

  @Test
  public void expressionList_reallife() {
    p.setRootRule(g.expressionList);

    assertThat(p, parse("(istream_iterator<string>(cin)), istream_iterator<string>()"));
  }

  @Test
  public void pseudoDestructorName() {
    p.setRootRule(g.pseudoDestructorName);

    g.typeName.mock();
    g.nestedNameSpecifier.mock();
    g.simpleTemplateId.mock();
    g.decltypeSpecifier.mock();

    assertThat(p, parse("typeName :: ~ typeName"));
    assertThat(p, parse("nestedNameSpecifier typeName :: ~ typeName"));
    assertThat(p, parse("nestedNameSpecifier template simpleTemplateId :: ~ typeName"));
    assertThat(p, parse("~ typeName"));
    assertThat(p, parse("nestedNameSpecifier ~ typeName"));
    assertThat(p, parse("~ decltypeSpecifier"));
  }

  @Test
  public void unaryExpression() {
    p.setRootRule(g.unaryExpression);

    g.postfixExpression.mock();
    g.castExpression.mock();
    g.unaryOperator.mock();
    g.typeId.mock();
    g.noexceptExpression.mock();
    g.newExpression.mock();
    g.deleteExpression.mock();

    assertThat(p, parse("postfixExpression"));
    assertThat(p, parse("sizeof postfixExpression"));
  }

  @Test
  public void unaryExpression_reallife() {
    p.setRootRule(g.unaryExpression);

    assertThat(p, parse("(istream_iterator<string>(cin))"));
    assertThat(p, parse("~CDB::mask"));
  }

  @Test
  public void newExpression() {
    p.setRootRule(g.newExpression);

    g.newPlacement.mock();
    g.newTypeId.mock();
    g.newInitializer.mock();
    g.typeId.mock();

    assertThat(p, parse(":: new newPlacement newTypeId newInitializer"));
    assertThat(p, parse(":: new newPlacement ( typeId ) newInitializer"));
  }

  @Test
  public void newExpression_reallife() {
    p.setRootRule(g.newExpression);

    assertThat(p, parse("new Table()"));
    assertThat(p, parse("new Table"));
    assertThat(p, parse("new(Table)"));
  }

  @Test
  public void newDeclarator() {
    p.setRootRule(g.newDeclarator);

    g.ptrOperator.mock();
    g.noptrNewDeclarator.mock();

    assertThat(p, parse("ptrOperator ptrOperator noptrNewDeclarator"));
    assertThat(p, parse("ptrOperator ptrOperator"));
    assertThat(p, parse("ptrOperator"));
    assertThat(p, parse("ptrOperator noptrNewDeclarator"));
    assertThat(p, parse("noptrNewDeclarator"));
  }

  @Test
  public void noptrNewDeclarator() {
    p.setRootRule(g.noptrNewDeclarator);

    g.expression.mock();
    g.attributeSpecifierSeq.mock();
    g.constantExpression.mock();

    assertThat(p, parse("[ expression ]"));
    assertThat(p, parse("[ expression ] attributeSpecifierSeq"));
    assertThat(p, parse("[ expression ] attributeSpecifierSeq [ constantExpression ]"));
    assertThat(p, parse("[ expression ] attributeSpecifierSeq [ constantExpression ] attributeSpecifierSeq"));
  }

  @Test
  public void newInitializer() {
    p.setRootRule(g.newInitializer);

    g.expressionList.mock();
    g.bracedInitList.mock();

    assertThat(p, parse("(  )"));
    assertThat(p, parse("( expressionList )"));
    assertThat(p, parse("bracedInitList"));
  }

  @Test
  public void deleteExpression() {
    p.setRootRule(g.deleteExpression);

    g.castExpression.mock();

    assertThat(p, parse(":: delete castExpression"));
    assertThat(p, parse(":: delete [ ] castExpression"));
  }

  @Test
  public void expression() {
    p.setRootRule(g.expression);
    g.assignmentExpression.mock();

    assertThat(p, parse("assignmentExpression"));
    assertThat(p, parse("assignmentExpression, assignmentExpression"));
    assertThat(p, parse("assignmentExpression, assignmentExpression, assignmentExpression"));
  }

  @Test
  public void expression_reallife() {
    p.setRootRule(g.expression);

    assertThat(p, parse("1 + 1"));
    assertThat(p, parse("(1 + 1) * 2"));
    assertThat("array subscript", p, parse("arr[i]"));
    assertThat(p, parse("( y > 4)"));
    assertThat(p, parse("( x== 8) && (c=='U')"));
    assertThat(p, parse("(a > b) ? a : b"));
    assertThat(p, parse("m = 1"));
    assertThat(p, parse("cout << endl"));
    assertThat(p, parse("numeric_limits<char>::is_signed"));
    assertThat(p, parse("cout << numeric_limits<char>::is_signed << endl"));
    assertThat(p, parse("usedColors[(Color)c]"));
    assertThat(p, parse("(Color)c"));
    assertThat(p, parse("foo()->i"));
    assertThat(p, parse("which ^= 1u"));
  }

  @Test
  public void assignmentExpression() {
    p.setRootRule(g.assignmentExpression);
    g.conditionalExpression.mock();
    g.logicalOrExpression.mock();
    g.assignmentOperator.mock();
    g.initializerClause.mock();
    g.throwExpression.mock();

    assertThat(p, parse("conditionalExpression"));
    assertThat(p, parse("logicalOrExpression assignmentOperator initializerClause"));
    assertThat(p, parse("throwExpression"));
  }

  @Test
  public void assignmentExpression_reallife() {
    p.setRootRule(g.assignmentExpression);

    assertThat(p, parse("i=0"));
    assertThat(p, parse("(istream_iterator<string>(cin))"));
    assertThat(p, parse("which ^= 1u"));
  }

  @Test
  public void logicalOrExpression() {
    p.setRootRule(g.logicalOrExpression);
    g.logicalAndExpression.mock();

    assertThat(p, parse("logicalAndExpression"));
    assertThat(p, parse("logicalAndExpression || logicalAndExpression"));
  }

  @Test
  public void logicalOrExpression_reallife() {
    p.setRootRule(g.logicalOrExpression);

    assertThat(p, parse("(istream_iterator<string>(cin))"));
  }

  @Test
  public void conditionalExpression() {
    p.setRootRule(g.conditionalExpression);

    g.logicalOrExpression.mock();
    g.expression.mock();
    g.assignmentExpression.mock();

    assertThat(p, parse("logicalOrExpression"));
    assertThat(p, parse("logicalOrExpression ? expression : assignmentExpression"));
  }

  @Test
  public void constantExpression() {
    p.setRootRule(g.constantExpression);

    g.conditionalExpression.mock();

    assertThat(p, parse("conditionalExpression"));
  }

  @Test
  public void constantExpression_reallife() {
    p.setRootRule(g.constantExpression);

    assertThat(p, parse("__cplusplus"));
  }

  @Test
  public void logicalAndExpression() {
    p.setRootRule(g.logicalAndExpression);
    g.inclusiveOrExpression.mock();

    assertThat(p, parse("inclusiveOrExpression"));
    assertThat(p, parse("inclusiveOrExpression && inclusiveOrExpression"));
  }

  @Test
  public void inclusiveOrExpression() {
    p.setRootRule(g.inclusiveOrExpression);
    g.exclusiveOrExpression.mock();

    assertThat(p, parse("exclusiveOrExpression"));
    assertThat(p, parse("exclusiveOrExpression | exclusiveOrExpression"));
  }

  @Test
  public void exclusiveOrExpression() {
    p.setRootRule(g.exclusiveOrExpression);
    g.andExpression.mock();

    assertThat(p, parse("andExpression"));
    assertThat(p, parse("andExpression ^ andExpression"));
  }

  @Test
  public void andExpression() {
    p.setRootRule(g.andExpression);
    g.equalityExpression.mock();

    assertThat(p, parse("equalityExpression"));
    assertThat(p, parse("equalityExpression & equalityExpression"));
  }

  @Test
  public void equalityExpression() {
    p.setRootRule(g.equalityExpression);
    g.relationalExpression.mock();

    assertThat(p, parse("relationalExpression"));
    assertThat(p, parse("relationalExpression == relationalExpression"));
    assertThat(p, parse("relationalExpression != relationalExpression"));
  }

  @Test
  public void relationalExpression() {
    p.setRootRule(g.relationalExpression);
    g.shiftExpression.mock();

    assertThat(p, parse("shiftExpression"));
    assertThat(p, parse("shiftExpression < shiftExpression"));
    assertThat(p, parse("shiftExpression > shiftExpression"));
    assertThat(p, parse("shiftExpression <= shiftExpression"));
    assertThat(p, parse("shiftExpression >= shiftExpression"));
  }

  @Test
  public void shiftExpression() {
    p.setRootRule(g.shiftExpression);
    g.additiveExpression.mock();

    assertThat(p, parse("additiveExpression"));
    assertThat(p, parse("additiveExpression << additiveExpression"));
    assertThat(p, parse("additiveExpression >> additiveExpression"));
  }

  @Test
  public void additiveExpression() {
    p.setRootRule(g.additiveExpression);
    g.multiplicativeExpression.mock();

    assertThat(p, parse("multiplicativeExpression"));
    assertThat(p, parse("multiplicativeExpression + multiplicativeExpression"));
    assertThat(p, parse("multiplicativeExpression - multiplicativeExpression"));
  }

  @Test
  public void multiplicativeExpression() {
    p.setRootRule(g.multiplicativeExpression);
    g.pmExpression.mock();

    assertThat(p, parse("pmExpression"));
    assertThat(p, parse("pmExpression * pmExpression"));
    assertThat(p, parse("pmExpression / pmExpression"));
    assertThat(p, parse("pmExpression % pmExpression"));
  }

  @Test
  public void multiplicativeExpression_reallive() {
    p.setRootRule(g.multiplicativeExpression);

    assertThat(p, parse("N / 1"));
  }

  @Test
  public void pmExpression() {
    p.setRootRule(g.pmExpression);
    g.castExpression.mock();

    assertThat(p, parse("castExpression"));
    assertThat(p, parse("castExpression .* castExpression"));
    assertThat(p, parse("castExpression ->* castExpression"));
  }

  @Test
  public void castExpression() {
    p.setRootRule(g.pmExpression);
    g.unaryExpression.mock();
    g.typeId.mock();

    assertThat(p, parse("unaryExpression"));
    assertThat(p, parse("(typeId) unaryExpression"));
    assertThat(p, parse("(typeId)(typeId) unaryExpression"));
  }

  @Test
  public void castExpression_reallife() {
    p.setRootRule(g.castExpression);

    assertThat(p, parse("(istream_iterator<string>(cin))"));
    assertThat(p, parse("(Color)c"));
    assertThat(p, parse("CDB::mask"));
  }
}
