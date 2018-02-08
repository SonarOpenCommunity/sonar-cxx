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
package org.sonar.cxx.sensors.tests.dotnet;

// origin https://github.com/SonarSource/sonar-dotnet-tests-library/
// SonarQube .NET Tests Library
// Copyright (C) 2014-2017 SonarSource SA
// mailto:info AT sonarsource DOT com

import java.io.File;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class XmlParserHelperTest {

  private static final String REPORT_PATH = "src/test/resources/org/sonar/cxx/sensors/reports-project/MSTest-reports/";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void invalid_prolog() {
    thrown.expectMessage("Error while parsing the XML file: ");
    thrown.expectMessage("invalid_prolog.txt");

    new XmlParserHelper(new File(REPORT_PATH + "invalid_prolog.txt")).nextStartTag();
  }

  @Test
  public void nextStartOrEndTag() {
    XmlParserHelper xml = new XmlParserHelper(new File(REPORT_PATH + "valid.xml"));
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
  public void getDoubleAttribute() {
    XmlParserHelper xml = new XmlParserHelper(new File(REPORT_PATH + "valid.xml"));
    xml.nextStartTag();
    assertThat(xml.getDoubleAttribute("myDouble")).isEqualTo(0.123);
    assertThat(xml.getDoubleAttribute("myCommaDouble")).isEqualTo(1.234);
    assertThat(xml.getDoubleAttribute("nonExisting")).isNull();

    thrown.expectMessage("valid.xml");
    thrown.expectMessage("Expected an double instead of \"hello\" for the attribute \"myString\"");
    xml.getDoubleAttribute("myString");
    try {
      xml.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
