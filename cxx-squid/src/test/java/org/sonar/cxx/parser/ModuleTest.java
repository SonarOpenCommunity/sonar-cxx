/*
 * Sonar C++ Plugin (Community)
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
import static org.sonar.sslr.tests.Assertions.assertThat;

public class ModuleTest extends ParserBaseTestHelper {

  @Test
  public void translationUnit() {
    p.setRootRule(g.rule(CxxGrammarImpl.translationUnit));

    mockRule(CxxGrammarImpl.globalModuleFragment);
    mockRule(CxxGrammarImpl.moduleDeclaration);
    mockRule(CxxGrammarImpl.declarationSeq);
    mockRule(CxxGrammarImpl.privateModuleFragment);

    assertThat(p).matches("moduleDeclaration");
    assertThat(p).matches("moduleDeclaration declarationSeq");
    assertThat(p).matches("globalModuleFragment moduleDeclaration");
    assertThat(p).matches("globalModuleFragment moduleDeclaration declarationSeq");
    assertThat(p).matches("moduleDeclaration privateModuleFragment");
    assertThat(p).matches("moduleDeclaration declarationSeq privateModuleFragment");
    assertThat(p).matches("globalModuleFragment moduleDeclaration privateModuleFragment");
    assertThat(p).matches("globalModuleFragment moduleDeclaration declarationSeq privateModuleFragment");
  }

  @Test
  public void translationUnit_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.translationUnit));

    assertThat(p).matches("module math;");
    assertThat(p).matches("module math; import std.core;");
    assertThat(p).matches("module; module math;");
    assertThat(p).matches("module; int a; module math; int b;");
    assertThat(p).matches("module math; module :private;");
    assertThat(p).matches("export module math; int a; module :private; int b;");
  }

  @Test
  public void moduleDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.moduleDeclaration));

    mockRule(CxxGrammarImpl.moduleName);
    mockRule(CxxGrammarImpl.modulePartition);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThat(p).matches("module moduleName ;");
    assertThat(p).matches("export module moduleName ;");
    assertThat(p).matches("export module moduleName modulePartition ;");
    assertThat(p).matches("export module moduleName modulePartition attributeSpecifierSeq ;");
  }

  @Test
  public void moduleDeclaration_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.moduleDeclaration));

    assertThat(p).matches("module math;");
    assertThat(p).matches("export module math;");
    assertThat(p).matches("export module math:math2;");
  }

  @Test
  public void moduleName() {
    p.setRootRule(g.rule(CxxGrammarImpl.moduleName));

    mockRule(CxxGrammarImpl.moduleNameQualifier);

    assertThat(p).matches("foo");
    assertThat(p).matches("moduleNameQualifier foo");
  }

  @Test
  public void modulePartition() {
    p.setRootRule(g.rule(CxxGrammarImpl.modulePartition));

    mockRule(CxxGrammarImpl.moduleNameQualifier);

    assertThat(p).matches(":foo");
    assertThat(p).matches(":moduleNameQualifier foo");
  }

  @Test
  public void moduleNameQualifier() {
    p.setRootRule(g.rule(CxxGrammarImpl.moduleNameQualifier));

    assertThat(p).matches("foo.");
    assertThat(p).matches("foo.foo.");
  }

  @Test
  public void exportDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.exportDeclaration));

    mockRule(CxxGrammarImpl.declaration);
    mockRule(CxxGrammarImpl.declarationSeq);
    mockRule(CxxGrammarImpl.moduleImportDeclaration);

    assertThat(p).matches("export declaration");
    assertThat(p).matches("export { }");
    assertThat(p).matches("export { declarationSeq }");
    assertThat(p).matches("export moduleImportDeclaration");
  }

  @Test
  public void exportDeclaration_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.exportDeclaration));

    assertThat(p).matches("export int f() { return 1; }");
    assertThat(p).matches("export { int f() { return 1; } }");
    assertThat(p).matches("export import :math1;");
  }

  @Test
  public void moduleImportDeclaration() {
    p.setRootRule(g.rule(CxxGrammarImpl.moduleImportDeclaration));

    mockRule(CxxGrammarImpl.moduleName);
    mockRule(CxxGrammarImpl.modulePartition);
    mockRule(CxxGrammarImpl.attributeSpecifierSeq);

    assertThat(p).matches("import moduleName ;");
    assertThat(p).matches("import moduleName attributeSpecifierSeq ;");
    assertThat(p).matches("import modulePartition ;");
    assertThat(p).matches("import modulePartition attributeSpecifierSeq ;");
  }

  @Test
  public void moduleImportDeclaration_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.moduleImportDeclaration));

    assertThat(p).matches("import math;");
    assertThat(p).matches("import std.core;");
  }

  @Test
  public void globalModuleFragment() {
    p.setRootRule(g.rule(CxxGrammarImpl.globalModuleFragment));

    mockRule(CxxGrammarImpl.declarationSeq);

    assertThat(p).matches("module ;");
    assertThat(p).matches("module ; declarationSeq");
  }

  @Test
  public void privateModuleFragment() {
    p.setRootRule(g.rule(CxxGrammarImpl.privateModuleFragment));

    mockRule(CxxGrammarImpl.declarationSeq);

    assertThat(p).matches("module :private;");
    assertThat(p).matches("module :private; declarationSeq");
  }
}
