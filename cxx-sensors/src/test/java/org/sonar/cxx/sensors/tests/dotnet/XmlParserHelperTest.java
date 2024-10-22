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
package org.sonar.cxx.sensors.tests.dotnet;

// origin https://github.com/SonarSource/sonar-dotnet-tests-library/
// SonarQube .NET Tests Library
// Copyright (C) 2014-2017 SonarSource SA
// mailto:info AT sonarsource DOT com
import java.io.File;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class XmlParserHelperTest {

  private static final String REPORT_PATH = "src/test/resources/org/sonar/cxx/sensors/reports-project/MSTest-reports/";

  @Test
  void invalidProlog() {
    IllegalStateException e = catchThrowableOfType(IllegalStateException.class, () -> {
      try (var helper = new XmlParserHelper(new File(REPORT_PATH + "invalid_prolog.txt"))) {
        helper.nextStartTag();
      }
    });
    assertThat(e).hasMessageContaining("Error while parsing the XML file: "
      + new File(REPORT_PATH + "invalid_prolog.txt").getAbsolutePath());
  }

  @Test
  void nextStartOrEndTag() {
    var xml = new XmlParserHelper(new File(REPORT_PATH + "valid.xml"));
    assertThat(xml.nextStartOrEndTag()).isEqualTo("<foo>");
    assertThat(xml.nextStartOrEndTag()).isEqualTo("<bar>");
    assertThat(xml.nextStartOrEndTag()).isEqualTo("</bar>");
    assertThat(xml.nextStartOrEndTag()).isEqualTo("</foo>");
    assertThat(xml.nextStartOrEndTag()).isNull();
    try {
      xml.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  void getDoubleAttribute() {
    var xml = new XmlParserHelper(new File(REPORT_PATH + "valid.xml"));
    xml.nextStartTag();
    assertThat(xml.getDoubleAttribute("myDouble")).isEqualTo(0.123);
    assertThat(xml.getDoubleAttribute("myCommaDouble")).isEqualTo(1.234);
    assertThat(xml.getDoubleAttribute("nonExisting")).isNull();

    ParseErrorException e = catchThrowableOfType(ParseErrorException.class, () -> {
      xml.getDoubleAttribute("myString");
    });
    assertThat(e).hasMessageContaining("Expected an double instead of \"hello\" for the attribute \"myString\" in "
      + new File(REPORT_PATH + "valid.xml").getAbsolutePath());
  }

}
