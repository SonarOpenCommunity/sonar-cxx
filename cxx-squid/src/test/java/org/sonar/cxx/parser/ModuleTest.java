/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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

import org.junit.jupiter.api.Test;

class ModuleTest extends ParserBaseTestHelper {

  @Test
  void translationUnit() {
    setRootRule(CxxGrammarImpl.translationUnit);

    mockRule(CxxGrammarImpl.globalModuleFragment);
    mockRule(CxxGrammarImpl.moduleDeclaration);
    mockRule(CxxGrammarImpl.declaration);
    mockRule(CxxGrammarImpl.privateModuleFragment);

    assertThatParser()
      .matches("moduleDeclaration")
      .matches("moduleDeclaration declaration")
      .matches("moduleDeclaration declaration declaration") // declarationSeq
      .matches("globalModuleFragment moduleDeclaration")
      .matches("globalModuleFragment moduleDeclaration declaration")
      .matches("moduleDeclaration privateModuleFragment")
      .matches("moduleDeclaration declaration privateModuleFragment")
      .matches("globalModuleFragment moduleDeclaration privateModuleFragment")
      .matches("globalModuleFragment moduleDeclaration declaration privateModuleFragment");
  }

  @Test
  void translationUnit_reallife() {
    setRootRule(CxxGrammarImpl.translationUnit);

    assertThatParser()
      .matches("module math;")
      .matches("module math; import std.core;")
      .matches("module; module math;")
      .matches("module; int a; module math; int b;")
      .matches("module math; module :private;")
      .matches("export module math; int a; module :private; int b;");
  }

  @Test
  void moduleDeclaration() {
    setRootRule(CxxGrammarImpl.moduleDeclaration);

    mockRule(CxxGrammarImpl.moduleName);
    mockRule(CxxGrammarImpl.modulePartition);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThatParser()
      .matches("module moduleName ;")
      .matches("export module moduleName ;")
      .matches("export module moduleName modulePartition ;")
      .matches("export module moduleName modulePartition attributeSpecifierSeq ;");
  }

  @Test
  void moduleDeclaration_reallife() {
    setRootRule(CxxGrammarImpl.moduleDeclaration);

    assertThatParser()
      .matches("module math;")
      .matches("export module math;")
      .matches("export module math:math2;");
  }

  @Test
  void moduleName() {
    setRootRule(CxxGrammarImpl.moduleName);

    mockRule(CxxGrammarImpl.moduleNameQualifier);

    assertThatParser()
      .matches("foo")
      .matches("moduleNameQualifier foo");
  }

  @Test
  void modulePartition() {
    setRootRule(CxxGrammarImpl.modulePartition);

    mockRule(CxxGrammarImpl.moduleNameQualifier);

    assertThatParser()
      .matches(":foo")
      .matches(":moduleNameQualifier foo");
  }

  @Test
  void moduleNameQualifier() {
    setRootRule(CxxGrammarImpl.moduleNameQualifier);

    assertThatParser()
      .matches("foo.")
      .matches("foo.foo.");
  }

  @Test
  void exportDeclaration() {
    setRootRule(CxxGrammarImpl.exportDeclaration);

    mockRule(CxxGrammarImpl.declaration);
    mockRule(CxxGrammarImpl.declarationSeq);
    mockRule(CxxGrammarImpl.moduleImportDeclaration);

    assertThatParser()
      .matches("export declaration")
      .matches("export { }")
      .matches("export { declarationSeq }")
      .matches("export moduleImportDeclaration");
  }

  @Test
  void exportDeclaration_reallife() {
    setRootRule(CxxGrammarImpl.exportDeclaration);

    assertThatParser()
      .matches("export int f() { return 1; }")
      .matches("export { int f() { return 1; } }")
      .matches("export import :math1;");
  }

  @Test
  void moduleImportDeclaration() {
    setRootRule(CxxGrammarImpl.moduleImportDeclaration);

    mockRule(CxxGrammarImpl.moduleName);
    mockRule(CxxGrammarImpl.modulePartition);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThatParser()
      .matches("import moduleName ;")
      .matches("import moduleName attributeSpecifierSeq ;")
      .matches("import modulePartition ;")
      .matches("import modulePartition attributeSpecifierSeq ;");
  }

  @Test
  void moduleImportDeclaration_reallife() {
    setRootRule(CxxGrammarImpl.moduleImportDeclaration);

    assertThatParser()
      .matches("import math;")
      .matches("import std.core;");
  }

  @Test
  void globalModuleFragment() {
    setRootRule(CxxGrammarImpl.globalModuleFragment);

    mockRule(CxxGrammarImpl.declarationSeq);

    assertThatParser()
      .matches("module ;")
      .matches("module ; declarationSeq");
  }

  @Test
  void privateModuleFragment() {
    setRootRule(CxxGrammarImpl.privateModuleFragment);

    mockRule(CxxGrammarImpl.declarationSeq);

    assertThatParser()
      .matches("module :private;")
      .matches("module :private; declarationSeq");
  }
}
