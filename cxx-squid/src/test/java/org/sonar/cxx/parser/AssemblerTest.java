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

public class AssemblerTest extends ParserBaseTestHelper {

  @Test
  public void asmIsoStandard() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDeclaration));
    assertThat(p).matches("asm(\"mov eax, num\");");
  }

  @Test
  public void asmGcc() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDeclaration));
    assertThat(p).matches("asm(\"mov eax, num\");");
    assertThat(p).matches("__asm__(\"mov eax, num\");");
    assertThat(p).matches("asm virtual(\"mov eax, num\");");
    assertThat(p).matches("asm inline(\"mov eax, num\");");
    assertThat(p).matches("__asm__ __virtual__(\"mov eax, num\");");
  }

  @Test
  public void asmVcAssemblyInstruction1() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDeclaration));
    assertThat(p).matches("__asm mov eax, num ;");
    assertThat(p).matches("asm mov eax, num ;");
  }

  @Test
  public void asmVcAssemblyInstructionList1() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDeclaration));
    assertThat(p).matches("__asm { mov eax, num }");
    assertThat(p).matches("asm { mov eax, num }");
  }

  @Test
  public void asmVcAssemblyInstructionList2() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDeclaration));
    assertThat(p).matches("__asm { mov eax, num };");
    assertThat(p).matches("asm { mov eax, num };");
  }

  @Test
  public void asmVcAssemblyInstructionList3() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmDeclaration));
    assertThat(p).matches(
      "__asm {\n"
        + "mov eax, num    ; Get first argument\n"
        + "mov ecx, power  ; Get second argument\n"
        + "shl eax, cl     ; EAX = EAX * ( 2 to the power of CL )\n"
        + "}"
    );
    assertThat(p).matches(
      "asm {\n"
        + "mov eax, num    ; Get first argument\n"
        + "mov ecx, power  ; Get second argument\n"
        + "shl eax, cl     ; EAX = EAX * ( 2 to the power of CL )\n"
        + "}"
    );
  }

  @Test
  public void asmGccLabel() {
    p.setRootRule(g.rule(CxxGrammarImpl.asmLabel));
    assertThat(p).matches("asm (\"myfoo\")");
    assertThat(p).matches("__asm__ (\"myfoo\")");
  }

  @Test
  public void asmGccLabel_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.simpleDeclaration));

    assertThat(p).matches("extern const char cert_start[] asm(\"_binary_firmware_pho_by_crt_start\");");
    assertThat(p).matches("int func (int x, int y) asm (\"MYFUNC\");");
    assertThat(p).matches("int foo asm (\"myfoo\") = 2;");

    assertThat(p).matches("extern const char cert_start[] __asm__(\"_binary_firmware_pho_by_crt_start\");");
    assertThat(p).matches("int func (int x, int y) __asm__ (\"MYFUNC\");");
    assertThat(p).matches("int foo __asm__ (\"myfoo\") = 2;");
  }

}
