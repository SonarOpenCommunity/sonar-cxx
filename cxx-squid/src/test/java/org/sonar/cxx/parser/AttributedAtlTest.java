/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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

public class AttributedAtlTest extends ParserBaseTestHelper {

  @Test
  public void vcAtlDeclaration() {
    setRootRule(CxxGrammarImpl.declaration);

    assertThatParser()
      .matches("[x];");
  }

  @Test
  public void vcAtlEnum() {
    setRootRule(CxxGrammarImpl.enumSpecifier);

    assertThatParser()
      .matches("[x] enum X {}");
  }

  @Test
  public void vcAtlClass() {
    setRootRule(CxxGrammarImpl.classSpecifier);

    assertThatParser()
      .matches("[x] class X {}")
      .matches("[x] struct X {}");
  }

  @Test
  public void vcAtlMember() {
    setRootRule(CxxGrammarImpl.memberSpecification);

    assertThatParser()
      .matches("[x] int m([y] int p);");
  }

  @Test
  public void vcAtlRealWorldExample() {
    setRootRule(CxxGrammarImpl.translationUnit);

    assertThatParser()
      .matches(
        "  [module(name=\"MyModule\")];"
          + "[emitidl(false)];"
          + "[export, helpstring(\"description\")] enum MyEnum {};"
          + "["
          + "  dispinterface,"
          + "  nonextensible,"
          + "  hidden,"
          + "  uuid(\"0815\"),"
          + "  helpstring(\"description\")"
          + "]"
          + "struct IMyInterface"
          + "{"
          + "  [id(1), helpstring(\"description\")] HRESULT M1(int p1);"
          + "  [propget, id(DISPID_VALUE), helpstring(\"description\")] HRESULT M2([in] VARIANT p1, [out, retval] MyService** p2);"
        + "};"
      );
  }

}
