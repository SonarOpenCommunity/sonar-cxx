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
package org.sonar.cxx.utils;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CxxReportLocationTest {

  @Test
  void testConstructor() {
    var loc0 = new CxxReportLocation(null, null, null, "info");
    assertThat(loc0.getFile()).isNull();
    assertThat(loc0.getLine()).isNull();
    assertThat(loc0.getColumn()).isNull();
    assertThat(loc0.getInfo()).isEqualTo("info");
    assertThat(loc0).hasToString(
      "CxxReportLocation [file=null, line=null, column=null, infoPrefix=null, info=info]"
    );

    var loc1 = new CxxReportLocation("file", "line", "column", "info");
    assertThat(loc1.getFile()).isEqualTo("file");
    assertThat(loc1.getLine()).isEqualTo("line");
    assertThat(loc1.getColumn()).isEqualTo("column");
    assertThat(loc1.getInfo()).isEqualTo("info");
    assertThat(loc1).hasToString(
      "CxxReportLocation [file=file, line=line, column=column, infoPrefix=null, info=info]"
    );
  }

  @Test
  void testInfoPrefix() {
    var loc0 = new CxxReportLocation(null, null, null, "info");
    var loc1 = new CxxReportLocation(null, null, null, "info");
    assertThat(loc0.getInfo()).isEqualTo("info");
    loc0.setInfoPrefix("prefix:");
    assertThat(loc0.getInfo()).isEqualTo("prefix:info");
    assertThat(loc0)
      .isEqualTo(loc1)
      .hasSameHashCodeAs(loc1);
  }

  @Test
  void testPathSanitize() {
    var loc = new CxxReportLocation("/dir/File", null, null, "");
    assertThat(loc.getFile()).isEqualTo("/dir/File");
    loc = new CxxReportLocation("/dir/./File", null, null, "");
    assertThat(loc.getFile()).isEqualTo("/dir/File");
    loc = new CxxReportLocation("/dir/../File", null, null, "");
    assertThat(loc.getFile()).isEqualTo("/File");
    loc = new CxxReportLocation("dir/File", null, null, "");
    assertThat(loc.getFile()).isEqualTo("dir/File");
    loc = new CxxReportLocation("./dir/File", null, null, "");
    assertThat(loc.getFile()).isEqualTo("dir/File");
    loc = new CxxReportLocation("../dir/File", null, null, "");
    assertThat(loc.getFile()).isEqualTo("File");
    loc = new CxxReportLocation("../../File", null, null, "");
    assertThat(loc.getFile()).isEqualTo("File");

    loc = new CxxReportLocation("c:\\dir\\file.ext", null, null, "");
    assertThat(loc.getFile()).isEqualTo("c:/dir/file.ext");
    loc = new CxxReportLocation("C:\\dir\\.\\file.ext", null, null, "");
    assertThat(loc.getFile()).isEqualTo("C:/dir/file.ext");
    loc = new CxxReportLocation("c:\\dir\\..\\file.ext", null, null, "");
    assertThat(loc.getFile()).isEqualTo("c:/file.ext");
    loc = new CxxReportLocation("dir\\file.ext", null, null, "");
    assertThat(loc.getFile()).isEqualTo("dir/file.ext");
    loc = new CxxReportLocation(".\\dir\\file.ext", null, null, "");
    assertThat(loc.getFile()).isEqualTo("dir/file.ext");
    loc = new CxxReportLocation("..\\dir\\file.ext", null, null, "");
    assertThat(loc.getFile()).isEqualTo("file.ext");
    loc = new CxxReportLocation("..\\..\\file.ext", null, null, "");
    assertThat(loc.getFile()).isEqualTo("file.ext");
  }
}
