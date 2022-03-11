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
package org.sonar.plugins.cxx;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;

public class CxxLanguageTest {

  private final MapSettings settings = new MapSettings();

  @Test
  public void testCxxLanguageStringConfiguration() throws Exception {
    var language = new CxxLanguage(settings.asConfig());
    assertThat(language.getKey()).isEqualTo("cxx");
  }

  @Test
  public void shouldReturnConfiguredFileSuffixes() {
    settings.setProperty(CxxLanguage.FILE_SUFFIXES_KEY, ".C,.c,.H,.h");
    var cxx = new CxxLanguage(settings.asConfig());
    String[] expected = {".C", ".c", ".H", ".h"};
    assertThat(cxx.getFileSuffixes()).contains(expected);
  }

  @Test
  public void shouldReturnDefaultFileSuffixes1() {
    var cxx = new CxxLanguage(settings.asConfig());
    String[] expected = {"disabled"};
    assertThat(cxx.getFileSuffixes()).contains(expected);
  }

  @Test
  public void shouldReturnDefaultFileSuffixes2() {
    settings.setProperty(CxxLanguage.FILE_SUFFIXES_KEY, "");
    var cxx = new CxxLanguage(settings.asConfig());
    String[] expected = {"disabled"};
    assertThat(cxx.getFileSuffixes()).contains(expected);
  }

  @Test
  public void shouldBeDisabled() {
    settings.setProperty(CxxLanguage.FILE_SUFFIXES_KEY, "-");
    var cxx = new CxxLanguage(settings.asConfig());
    String[] expected = {"disabled"};
    assertThat(cxx.getFileSuffixes()).contains(expected);
  }

}
