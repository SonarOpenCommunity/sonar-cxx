/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.sensors.infer;

import com.google.gson.Gson;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class InferParserTest {

  @Test
  void shouldParseImportantInformation() {
    var expected = new InferParser.InferIssue();
    expected.setBugType("TotoType");
    expected.setFile("path/to/toto.c");
    expected.setLine(11);
    expected.setQualifier("Toto should not be toto.");

    var json = "{'bug_type':'TotoType','qualifier':'Toto should not be toto.',"
             + "'line':11,'file':'path/to/toto.c'}";
    var gson = new Gson();
    InferParser.InferIssue value = gson.fromJson(json, InferParser.InferIssue.class);

    assertThat(value).hasToString(expected.toString());
  }

}
