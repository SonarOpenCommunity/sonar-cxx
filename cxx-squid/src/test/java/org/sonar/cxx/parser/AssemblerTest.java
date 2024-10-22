/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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

class AssemblerTest extends ParserBaseTestHelper {

  @Test
  void asmIsoStandard() {
    setRootRule(CxxGrammarImpl.asmDeclaration);
    assertThatParser()
      .matches("asm(\"mov eax, num\");");
  }

  @Test
  void asmGcc() {
    setRootRule(CxxGrammarImpl.asmDeclaration);
    assertThatParser()
      .matches("asm(\"mov eax, num\");")
      .matches("__asm__(\"mov eax, num\");")
      .matches("asm virtual(\"mov eax, num\");")
      .matches("asm inline(\"mov eax, num\");")
      .matches("__asm__ __virtual__(\"mov eax, num\");");
  }

  @Test
  void asmVcAssemblyInstruction1() {
    setRootRule(CxxGrammarImpl.asmDeclaration);
    assertThatParser()
      .matches("__asm mov eax, num ;")
      .matches("asm mov eax, num ;");
  }

  @Test
  void asmVcAssemblyInstructionList1() {
    setRootRule(CxxGrammarImpl.asmDeclaration);
    assertThatParser()
      .matches("__asm { mov eax, num }")
      .matches("asm { mov eax, num }");
  }

  @Test
  void asmVcAssemblyInstructionList2() {
    setRootRule(CxxGrammarImpl.asmDeclaration);
    assertThatParser()
      .matches("__asm { mov eax, num };")
      .matches("asm { mov eax, num };");
  }

  @Test
  void asmVcAssemblyInstructionList3() {
    setRootRule(CxxGrammarImpl.asmDeclaration);
    assertThatParser()
      .matches("""
               __asm {
               mov eax, num    ; Get first argument
               mov ecx, power  ; Get second argument
               shl eax, cl     ; EAX = EAX * ( 2 to the power of CL )
               }
               """)
      .matches("""
               asm {
               mov eax, num    ; Get first argument
               mov ecx, power  ; Get second argument
               shl eax, cl     ; EAX = EAX * ( 2 to the power of CL )
               }
               """);
  }

  @Test
  void asmGccLabel() {
    setRootRule(CxxGrammarImpl.asmLabel);
    assertThatParser()
      .matches("asm (\"myfoo\")")
      .matches("__asm__ (\"myfoo\")");
  }

  @Test
  void asmGccLabelReallife() {
    setRootRule(CxxGrammarImpl.simpleDeclaration);

    assertThatParser()
      .matches("extern const char cert_start[] asm(\"_binary_firmware_pho_by_crt_start\");")
      .matches("int func (int x, int y) asm (\"MYFUNC\");")
      .matches("int foo asm (\"myfoo\") = 2;")
      .matches("extern const char cert_start[] __asm__(\"_binary_firmware_pho_by_crt_start\");")
      .matches("int func (int x, int y) __asm__ (\"MYFUNC\");")
      .matches("int foo __asm__ (\"myfoo\") = 2;");
  }

}
