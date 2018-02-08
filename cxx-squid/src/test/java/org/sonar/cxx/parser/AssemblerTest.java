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

public class AssemblerTest extends ParserBaseTestHelper {

  @Test
  public void asmIsoStandard() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDefinition));
    assertThat(p).matches("asm(\"mov eax, num\");");
  }

  @Test
  public void asmVcAssemblyInstruction1() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDefinition));
    assertThat(p).matches("asm mov eax, num ;");
  }

  @Test
  public void asmVcAssemblyInstructionList1() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDefinition));
    assertThat(p).matches("asm { mov eax, num }");
  }

  @Test
  public void asmVcAssemblyInstructionList2() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDefinition));
    assertThat(p).matches("asm { mov eax, num };");
  }

  @Test
  public void asmVcAssemblyInstructionList3() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDefinition));
    assertThat(p).matches(
      "asm {\n"
      + "mov eax, num    ; Get first argument\n"
      + "mov ecx, power  ; Get second argument\n"
      + "shl eax, cl     ; EAX = EAX * ( 2 to the power of CL )\n"
      + "}"
    );
  }

}
