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

public class ExpressionTest {

  Parser<Grammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  Grammar g = p.getGrammar();

  @Test
  public void primaryExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.primaryExpression));
    
    g.rule(CxxGrammarImpl.LITERAL).mock();
    g.rule(CxxGrammarImpl.expression).mock();
    g.rule(CxxGrammarImpl.idExpression).mock();
    g.rule(CxxGrammarImpl.lambdaExpression).mock();
    
    assertThat(p)
      .matches("LITERAL")
      .matches("this")
      .matches("( expression )")
      .matches("idExpression")
      .matches("lambdaExpression");
  }

  @Test
  public void primaryExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.primaryExpression));
    
    assertThat(p).matches("(istream_iterator<string>(cin))");
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

    g.rule(CxxGrammarImpl.operatorFunctionId).mock();
    g.rule(CxxGrammarImpl.conversionFunctionId).mock();
    g.rule(CxxGrammarImpl.literalOperatorId).mock();
    g.rule(CxxGrammarImpl.className).mock();
    g.rule(CxxGrammarImpl.decltypeSpecifier).mock();
    g.rule(CxxGrammarImpl.templateId).mock();

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

    g.rule(CxxGrammarImpl.LITERAL).mock();
    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();
    g.rule(CxxGrammarImpl.unqualifiedId).mock();
    g.rule(CxxGrammarImpl.operatorFunctionId).mock();
    g.rule(CxxGrammarImpl.literalOperatorId).mock();
    g.rule(CxxGrammarImpl.templateId).mock();

    assertThat(p).matches("nestedNameSpecifier unqualifiedId");
    assertThat(p).matches("nestedNameSpecifier template unqualifiedId");
    assertThat(p).matches(":: foo");
    assertThat(p).matches(":: operatorFunctionId");
    assertThat(p).matches(":: literalOperatorId");
    assertThat(p).matches(":: templateId");
  }

  @Test
  public void qualifiedId_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.qualifiedId));

    assertThat(p).matches("numeric_limits<char>::is_signed");
  }

  @Test
  public void nestedNameSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.nestedNameSpecifier));

    g.rule(CxxGrammarImpl.typeName).mock();
    g.rule(CxxGrammarImpl.namespaceName).mock();
    g.rule(CxxGrammarImpl.decltypeSpecifier).mock();
    g.rule(CxxGrammarImpl.simpleTemplateId).mock();

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

    g.rule(CxxGrammarImpl.primaryExpression).mock();
    g.rule(CxxGrammarImpl.simpleTypeSpecifier).mock();
    g.rule(CxxGrammarImpl.expressionList).mock();
    g.rule(CxxGrammarImpl.typenameSpecifier).mock();
    g.rule(CxxGrammarImpl.bracedInitList).mock();
    g.rule(CxxGrammarImpl.primaryExpression).mock();
    g.rule(CxxGrammarImpl.expression).mock();
    g.rule(CxxGrammarImpl.idExpression).mock();
    g.rule(CxxGrammarImpl.typeId).mock();
    g.rule(CxxGrammarImpl.deleteExpression).mock();
    g.rule(CxxGrammarImpl.pseudoDestructorName).mock();

    assertThat(p).matches("primaryExpression");
    assertThat(p).matches("primaryExpression [ expression ]");
    assertThat(p).matches("primaryExpression ( expressionList )");
    assertThat(p).matches("simpleTypeSpecifier ( expressionList )");
    assertThat(p).matches("typenameSpecifier ( expressionList )");
    assertThat(p).matches("simpleTypeSpecifier bracedInitList");
    assertThat(p).matches("typenameSpecifier bracedInitList");
    assertThat(p).matches("primaryExpression . template idExpression");
    assertThat(p).matches("primaryExpression -> template idExpression");

    assertThat(p).matches("primaryExpression . pseudoDestructorName");
    assertThat(p).matches("primaryExpression -> pseudoDestructorName");

    assertThat(p).matches("primaryExpression ++");
    assertThat(p).matches("primaryExpression --");
    assertThat(p).matches("dynamic_cast < typeId > ( expression )");
    assertThat(p).matches("static_cast < typeId > ( expression )");
    assertThat(p).matches("reinterpret_cast < typeId > ( expression )");
    assertThat(p).matches("const_cast < typeId > ( expression )");
    assertThat(p).matches("typeid ( expression )");
    assertThat(p).matches("typeid ( typeId )");
  }

  @Test
  public void postfixExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.postfixExpression));

    assertThat(p).matches("usedColors[(Color)c]");
    assertThat(p).matches("foo()->i");
    assertThat(p).matches("dynamic_cast<Type*>(myop)->op()");
    assertThat(p).matches("::foo()");
    assertThat(p).matches("obj.foo<int>()");
  }

  @Test
  public void expressionList_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.expressionList));

    assertThat(p).matches("(istream_iterator<string>(cin)), istream_iterator<string>()");
  }

  @Test
  public void pseudoDestructorName() {
    p.setRootRule(g.rule(CxxGrammarImpl.pseudoDestructorName));

    g.rule(CxxGrammarImpl.typeName).mock();
    g.rule(CxxGrammarImpl.nestedNameSpecifier).mock();
    g.rule(CxxGrammarImpl.simpleTemplateId).mock();
    g.rule(CxxGrammarImpl.decltypeSpecifier).mock();

    assertThat(p).matches("typeName :: ~ typeName");
    assertThat(p).matches("nestedNameSpecifier typeName :: ~ typeName");
    assertThat(p).matches("nestedNameSpecifier template simpleTemplateId :: ~ typeName");
    assertThat(p).matches("~ typeName");
    assertThat(p).matches("nestedNameSpecifier ~ typeName");
    assertThat(p).matches("~ decltypeSpecifier");
  }

  @Test
  public void unaryExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.unaryExpression));

    g.rule(CxxGrammarImpl.postfixExpression).mock();
    g.rule(CxxGrammarImpl.castExpression).mock();
    g.rule(CxxGrammarImpl.unaryOperator).mock();
    g.rule(CxxGrammarImpl.typeId).mock();
    g.rule(CxxGrammarImpl.noexceptExpression).mock();
    g.rule(CxxGrammarImpl.newExpression).mock();
    g.rule(CxxGrammarImpl.deleteExpression).mock();

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

    g.rule(CxxGrammarImpl.newPlacement).mock();
    g.rule(CxxGrammarImpl.newTypeId).mock();
    g.rule(CxxGrammarImpl.newInitializer).mock();
    g.rule(CxxGrammarImpl.typeId).mock();

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

    g.rule(CxxGrammarImpl.ptrOperator).mock();
    g.rule(CxxGrammarImpl.noptrNewDeclarator).mock();

    assertThat(p).matches("ptrOperator ptrOperator noptrNewDeclarator");
    assertThat(p).matches("ptrOperator ptrOperator");
    assertThat(p).matches("ptrOperator");
    assertThat(p).matches("ptrOperator noptrNewDeclarator");
    assertThat(p).matches("noptrNewDeclarator");
  }

  @Test
  public void noptrNewDeclarator() {
    p.setRootRule(g.rule(CxxGrammarImpl.noptrNewDeclarator));

    g.rule(CxxGrammarImpl.expression).mock();
    g.rule(CxxGrammarImpl.attributeSpecifierSeq).mock();
    g.rule(CxxGrammarImpl.constantExpression).mock();

    assertThat(p).matches("[ expression ]");
    assertThat(p).matches("[ expression ] attributeSpecifierSeq");
    assertThat(p).matches("[ expression ] attributeSpecifierSeq [ constantExpression ]");
    assertThat(p).matches("[ expression ] attributeSpecifierSeq [ constantExpression ] attributeSpecifierSeq");
  }

  @Test
  public void newInitializer() {
    p.setRootRule(g.rule(CxxGrammarImpl.newInitializer));

    g.rule(CxxGrammarImpl.expressionList).mock();
    g.rule(CxxGrammarImpl.bracedInitList).mock();

    assertThat(p).matches("(  )");
    assertThat(p).matches("( expressionList )");
    assertThat(p).matches("bracedInitList");
  }

  @Test
  public void deleteExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.deleteExpression));

    g.rule(CxxGrammarImpl.castExpression).mock();

    assertThat(p).matches(":: delete castExpression");
    assertThat(p).matches(":: delete [ ] castExpression");
  }

  @Test
  public void expression() {
    p.setRootRule(g.rule(CxxGrammarImpl.expression));
    g.rule(CxxGrammarImpl.assignmentExpression).mock();

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
    g.rule(CxxGrammarImpl.conditionalExpression).mock();
    g.rule(CxxGrammarImpl.logicalOrExpression).mock();
    g.rule(CxxGrammarImpl.assignmentOperator).mock();
    g.rule(CxxGrammarImpl.initializerClause).mock();
    g.rule(CxxGrammarImpl.throwExpression).mock();

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
    g.rule(CxxGrammarImpl.logicalAndExpression).mock();

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

    g.rule(CxxGrammarImpl.logicalOrExpression).mock();
    g.rule(CxxGrammarImpl.expression).mock();
    g.rule(CxxGrammarImpl.assignmentExpression).mock();

    assertThat(p).matches("logicalOrExpression");
    assertThat(p).matches("logicalOrExpression ? expression : assignmentExpression");
  }

  @Test
  public void constantExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.constantExpression));

    g.rule(CxxGrammarImpl.conditionalExpression).mock();

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
    g.rule(CxxGrammarImpl.inclusiveOrExpression).mock();

    assertThat(p).matches("inclusiveOrExpression");
    assertThat(p).matches("inclusiveOrExpression && inclusiveOrExpression");
  }

  @Test
  public void inclusiveOrExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.inclusiveOrExpression));
    g.rule(CxxGrammarImpl.exclusiveOrExpression).mock();

    assertThat(p).matches("exclusiveOrExpression");
    assertThat(p).matches("exclusiveOrExpression | exclusiveOrExpression");
  }

  @Test
  public void exclusiveOrExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.exclusiveOrExpression));
    g.rule(CxxGrammarImpl.andExpression).mock();

    assertThat(p).matches("andExpression");
    assertThat(p).matches("andExpression ^ andExpression");
  }

  @Test
  public void andExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.andExpression));
    g.rule(CxxGrammarImpl.equalityExpression).mock();

    assertThat(p).matches("equalityExpression");
    assertThat(p).matches("equalityExpression & equalityExpression");
  }

  @Test
  public void equalityExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.equalityExpression));
    g.rule(CxxGrammarImpl.relationalExpression).mock();

    assertThat(p).matches("relationalExpression");
    assertThat(p).matches("relationalExpression == relationalExpression");
    assertThat(p).matches("relationalExpression != relationalExpression");
  }

  @Test
  public void relationalExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.relationalExpression));
    g.rule(CxxGrammarImpl.shiftExpression).mock();

    assertThat(p).matches("shiftExpression");
    assertThat(p).matches("shiftExpression < shiftExpression");
    assertThat(p).matches("shiftExpression > shiftExpression");
    assertThat(p).matches("shiftExpression <= shiftExpression");
    assertThat(p).matches("shiftExpression >= shiftExpression");
  }

  @Test
  public void shiftExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.shiftExpression));
    g.rule(CxxGrammarImpl.additiveExpression).mock();

    assertThat(p).matches("additiveExpression");
    assertThat(p).matches("additiveExpression << additiveExpression");
    assertThat(p).matches("additiveExpression >> additiveExpression");
  }

  @Test
  public void additiveExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.additiveExpression));
    g.rule(CxxGrammarImpl.multiplicativeExpression).mock();

    assertThat(p).matches("multiplicativeExpression");
    assertThat(p).matches("multiplicativeExpression + multiplicativeExpression");
    assertThat(p).matches("multiplicativeExpression - multiplicativeExpression");
  }

  @Test
  public void multiplicativeExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.multiplicativeExpression));
    g.rule(CxxGrammarImpl.pmExpression).mock();

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
    g.rule(CxxGrammarImpl.castExpression).mock();

    assertThat(p).matches("castExpression");
    assertThat(p).matches("castExpression .* castExpression");
    assertThat(p).matches("castExpression ->* castExpression");
  }

  @Test
  public void castExpression() {
    p.setRootRule(g.rule(CxxGrammarImpl.pmExpression));
    g.rule(CxxGrammarImpl.unaryExpression).mock();
    g.rule(CxxGrammarImpl.typeId).mock();

    assertThat(p).matches("unaryExpression");
    assertThat(p).matches("(typeId) unaryExpression");
    assertThat(p).matches("(typeId)(typeId) unaryExpression");
  }

  @Test
  public void castExpression_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.castExpression));

    assertThat(p).matches("(istream_iterator<string>(cin))");
    assertThat(p).matches("(Color)c");
    assertThat(p).matches("CDB::mask");
  }
}
