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

public class TemplatesTest extends ParserBaseTestHelper {

  @Test
  public void templateDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.templateDeclaration));

    mockRule(CxxGrammarImpl.templateParameterList);
    mockRule(CxxGrammarImpl.declaration);

    assertThat(p).matches("template < templateParameterList > declaration");
  }

  @Test
  public void templateDeclaration_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.templateDeclaration));

    assertThat(p).matches("template <class T> ostream& operator<<();");
    assertThat(p).matches("template <class T> ostream& operator<<(ostream& strm, const int& i);");

    assertThat(p).matches("template <class T> ostream& operator<< (ostream& strm);");
    assertThat(p).matches("template <class T> ostream& operator<< (const auto_ptr<T>& p);");
    assertThat(p).matches("template <class T> ostream& operator<< (ostream& strm, const auto_ptr<T>& p);");
    assertThat(p).matches("template<bool (A::*bar)(void)> void foo();");
    assertThat(p).matches("template<class T> auto mul(T a, T b) -> decltype(a*b) {return a*b;}");
  }

  @Test
  public void templateParameterList() {
    p.setRootRule(g.rule(CxxGrammarImpl.templateParameterList));

    mockRule(CxxGrammarImpl.templateParameter);

    assertThat(p).matches("templateParameter");
    assertThat(p).matches("templateParameter , templateParameter");
  }

  @Test
  public void typeParameter() {
    p.setRootRule(g.rule(CxxGrammarImpl.typeParameter));

    mockRule(CxxGrammarImpl.typeId);
    mockRule(CxxGrammarImpl.templateParameterList);
    mockRule(CxxGrammarImpl.idExpression);

    assertThat(p).matches("class");
    assertThat(p).matches("class T");
    assertThat(p).matches("class ... foo");

    assertThat(p).matches("class = typeId");
    assertThat(p).matches("class foo = typeId");

    assertThat(p).matches("typename");
    assertThat(p).matches("typename ... foo");

    assertThat(p).matches("typename = typeId");
    assertThat(p).matches("typename foo = typeId");

    assertThat(p).matches("template < templateParameterList > class");
    assertThat(p).matches("template < templateParameterList > class ... foo");

    assertThat(p).matches("template < templateParameterList > class = idExpression");
    assertThat(p).matches("template < templateParameterList > class foo = idExpression");
  }

  @Test
  public void simpleTemplateId_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.simpleTemplateId));

    assertThat(p).matches("sometype<int>");
    assertThat(p).matches("vector<Person*>");
    // assertThat(p).matches("sometype<N/2>");
    // try{
    // p.parse("vector<Person*>");
    // } catch(Exception e){}
    // ExtendedStackTraceStream.print(stackTrace, System.out);
  }

  @Test
  public void templateId() {
    p.setRootRule(g.rule(CxxGrammarImpl.templateId));

    mockRule(CxxGrammarImpl.simpleTemplateId);
    mockRule(CxxGrammarImpl.operatorFunctionId);
    mockRule(CxxGrammarImpl.templateArgumentList);
    mockRule(CxxGrammarImpl.literalOperatorId);

    assertThat(p).matches("simpleTemplateId");
    assertThat(p).matches("operatorFunctionId < >");
    assertThat(p).matches("operatorFunctionId < templateArgumentList >");
    assertThat(p).matches("literalOperatorId < >");
    assertThat(p).matches("literalOperatorId < templateArgumentList >");
  }

  @Test
  public void templateId_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.templateId));
    assertThat(p).matches("foo<int>");
    assertThat(p).matches("operator==<B>");
  }

  @Test
  public void templateArgumentList() {
    p.setRootRule(g.rule(CxxGrammarImpl.templateArgumentList));

    mockRule(CxxGrammarImpl.templateArgument);

    assertThat(p).matches("templateArgument");
    assertThat(p).matches("templateArgument ...");
    assertThat(p).matches("templateArgument , templateArgument");
    assertThat(p).matches("templateArgument , templateArgument ...");
  }

  @Test
  public void typenameSpecifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.typenameSpecifier));

    mockRule(CxxGrammarImpl.nestedNameSpecifier);
    mockRule(CxxGrammarImpl.simpleTemplateId);

    assertThat(p).matches("typename nestedNameSpecifier IDENTIFIER");
    assertThat(p).matches("typename nestedNameSpecifier simpleTemplateId");
    assertThat(p).matches("typename nestedNameSpecifier template simpleTemplateId");
    assertThat(p).matches("typename IDENTIFIER");
    assertThat(p).matches("IDENTIFIER");
  }

  @Test
  public void deductionGuide() {
    p.setRootRule(g.rule(CxxGrammarImpl.deductionGuide));

    mockRule(CxxGrammarImpl.templateName);
    mockRule(CxxGrammarImpl.parameterDeclarationClause);
    mockRule(CxxGrammarImpl.simpleTemplateId);

    assertThat(p).matches("templateName ( parameterDeclarationClause ) -> simpleTemplateId ;");
    assertThat(p).matches("extern templateName ( parameterDeclarationClause ) -> simpleTemplateId ;");
  }

}
