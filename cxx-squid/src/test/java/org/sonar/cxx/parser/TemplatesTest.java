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
import com.sonar.sslr.impl.events.ExtendedStackTrace;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class TemplatesTest {

  ExtendedStackTrace stackTrace = new ExtendedStackTrace();
  Parser<CxxGrammar> p = CxxParser.createDebugParser(mock(SquidAstVisitorContext.class), stackTrace);
  CxxGrammar g = p.getGrammar();

  @Test
  public void templateDeclaration() {
    p.setRootRule(g.templateDeclaration);

    g.templateParameterList.mock();
    g.declaration.mock();

    assertThat(p, parse("template < templateParameterList > declaration"));
  }

  @Test
  public void templateDeclaration_reallife() {
    p.setRootRule(g.templateDeclaration);

    assertThat(p, parse("template <class T> ostream& operator<<();"));
    assertThat(p, parse("template <class T> ostream& operator<<(ostream& strm, const int& i);"));

    assertThat(p, parse("template <class T> ostream& operator<< (ostream& strm);"));
    assertThat(p, parse("template <class T> ostream& operator<< (const auto_ptr<T>& p);"));
    assertThat(p, parse("template <class T> ostream& operator<< (ostream& strm, const auto_ptr<T>& p);"));
    assertThat(p, parse("template<bool (A::*bar)(void)> void foo();"));
  }

  @Test
  public void templateParameterList() {
    p.setRootRule(g.templateParameterList);

    g.templateParameter.mock();

    assertThat(p, parse("templateParameter"));
    assertThat(p, parse("templateParameter , templateParameter"));
  }

  @Test
  public void typeParameter() {
    p.setRootRule(g.typeParameter);

    g.typeId.mock();
    g.templateParameterList.mock();
    g.idExpression.mock();

    assertThat(p, parse("class"));
    assertThat(p, parse("class T"));
    assertThat(p, parse("class ... foo"));

    assertThat(p, parse("class = typeId"));
    assertThat(p, parse("class foo = typeId"));

    assertThat(p, parse("typename"));
    assertThat(p, parse("typename ... foo"));

    assertThat(p, parse("typename = typeId"));
    assertThat(p, parse("typename foo = typeId"));

    assertThat(p, parse("template < templateParameterList > class"));
    assertThat(p, parse("template < templateParameterList > class ... foo"));

    assertThat(p, parse("template < templateParameterList > class = idExpression"));
    assertThat(p, parse("template < templateParameterList > class foo = idExpression"));
  }

  @Test
  public void simpleTemplateId_reallife() {
    p.setRootRule(g.simpleTemplateId);

    assertThat(p, parse("sometype<int>"));
    assertThat(p, parse("vector<Person*>"));
    // assertThat(p, parse("sometype<N/2>"));
    // try{
    // p.parse("vector<Person*>");
    // } catch(Exception e){}
    // ExtendedStackTraceStream.print(stackTrace, System.out);
  }

  @Test
  public void templateId() {
    p.setRootRule(g.templateId);

    g.simpleTemplateId.mock();
    g.operatorFunctionId.mock();
    g.templateArgumentList.mock();
    g.literalOperatorId.mock();

    assertThat(p, parse("simpleTemplateId"));
    assertThat(p, parse("operatorFunctionId < >"));
    assertThat(p, parse("operatorFunctionId < templateArgumentList >"));
    assertThat(p, parse("literalOperatorId < >"));
    assertThat(p, parse("literalOperatorId < templateArgumentList >"));
  }

  @Test
  public void templateId_reallife() {
    p.setRootRule(g.templateId);
    assertThat(p, parse("foo<int>"));
    assertThat(p, parse("operator==<B>"));
  }

  @Test
  public void templateArgumentList() {
    p.setRootRule(g.templateArgumentList);

    g.templateArgument.mock();

    assertThat(p, parse("templateArgument"));
    assertThat(p, parse("templateArgument ..."));
    assertThat(p, parse("templateArgument , templateArgument"));
    assertThat(p, parse("templateArgument , templateArgument ..."));
  }

  @Test
  public void typenameSpecifier() {
    p.setRootRule(g.typenameSpecifier);

    g.nestedNameSpecifier.mock();
    g.simpleTemplateId.mock();

    assertThat(p, parse("typename nestedNameSpecifier foo"));

    assertThat(p, parse("typename nestedNameSpecifier simpleTemplateId"));
    assertThat(p, parse("typename nestedNameSpecifier template simpleTemplateId"));
  }
}
