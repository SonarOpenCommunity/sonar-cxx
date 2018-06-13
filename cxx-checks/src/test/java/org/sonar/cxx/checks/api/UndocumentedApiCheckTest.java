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
package org.sonar.cxx.checks;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;

public class UndocumentedApiCheckTest {

  @Rule
  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  @SuppressWarnings("squid:S2699")
  @Test
  public void detected() throws UnsupportedEncodingException, IOException {
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/UndocumentedApiCheck/no_doc.h", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), new UndocumentedApiCheck());

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(6) // class
      .next().atLine(11) // public method
      .next().atLine(13) // enum
      .next().atLine(14) // enumerator
      .next().atLine(17) // enum value
      .next().atLine(19) // member variable
      .next().atLine(21) // member variable
      .next().atLine(23) // public inline method
      .next().atLine(25) // public template method
      .next().atLine(29) // protected method
      .next().atLine(51) // public member variable
      .next().atLine(55) // struct
      .next().atLine(56) // struct member
      .next().atLine(59) // forward declaration
      .next().atLine(61) // fuction declaration
      .next().atLine(63) // global emptyEnum
      .next().atLine(66) // global testEnum
      .next().atLine(68) // global enum value
      .next().atLine(71) // global testEnumWithType
      .next().atLine(73) // global enum value
      .next().atLine(76) // global testScopedEnum
      .next().atLine(78) // global enum value
      .next().atLine(81) // global union
      .next().atLine(87) // template
      .next().atLine(90) // function definition
      .next().atLine(94) // typedef
      .next().atLine(96) // typedef struct
      .next().atLine(98) // struct member
      .next().atLine(99) // struct member
      .next().atLine(102) // typedef class
      .next().atLine(105) // class member
      .next().atLine(106) // class member
      .next().atLine(109) // typedef union
      .next().atLine(111) // union member
      .next().atLine(112) // union member
      .next().atLine(115) // typedef enum
      .next().atLine(117) // enum member
      .next().atLine(118) // enum member
      .next().atLine(121) // typedef enum class
      .next().atLine(123) // enum member
      .next().atLine(124) // enum member
      .next().atLine(127) // class OverrideInClassTest
      .next().atLine(138) // struct OverrideInStructTest      
      .next().atLine(143) // struct ComplexOverrideInStruct
      .next().atLine(148) // struct ComplexOverrideInClass      
      .next().atLine(154) // aliasDeclaration1
      .next().atLine(156) // aliasDeclaration2
      .next().atLine(161); // class ClassWithFriend

    for (CheckMessage msg : file.getCheckMessages()) {
      assertThat(msg.formatDefaultMessage()).isNotEmpty();
    }
  }

  @Test
  public void docStyle1() throws UnsupportedEncodingException, IOException {
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/UndocumentedApiCheck/doc_style1.h", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), new UndocumentedApiCheck());

    StringBuilder errors = new StringBuilder();
    for (CheckMessage msg : file.getCheckMessages()) {
      errors.append("Line: ");
      errors.append(msg.getLine());
      errors.append("; ");
      errors.append(msg.formatDefaultMessage());
      errors.append("\r\n");
    }
    assertThat(errors.length()).isEqualTo(0);
  }

  @Test
  public void docStyle2() throws UnsupportedEncodingException, IOException {
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/checks/UndocumentedApiCheck/doc_style2.h", ".");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, CxxFileTesterHelper.mockCxxLanguage(), new UndocumentedApiCheck());

    StringBuilder errors = new StringBuilder();
    for (CheckMessage msg : file.getCheckMessages()) {
      errors.append("Line: ");
      errors.append(msg.getLine());
      errors.append("; ");
      errors.append(msg.formatDefaultMessage());
      errors.append("\r\n");
    }
    assertThat(errors.length()).isEqualTo(0);
  }

}
