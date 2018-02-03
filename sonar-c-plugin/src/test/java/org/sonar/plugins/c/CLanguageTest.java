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
package org.sonar.plugins.c;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;

public class CLanguageTest {

  private MapSettings settings = new MapSettings();

  @Test
  public void shouldReturnConfiguredFileSuffixes() {
    settings.setProperty(CPlugin.SOURCE_FILE_SUFFIXES_KEY, ".C,.c");
    settings.setProperty(CPlugin.HEADER_FILE_SUFFIXES_KEY, ".H,.h");
    CLanguage cxx = new CLanguage(settings.asConfig());

    String[] expected = {".C", ".c", ".H", ".h"};
    String[] expectedSources = {".C", ".c"};
    String[] expectedHeaders = {".H", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expected));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

  @Test
  public void shouldReturnDefaultFileSuffixes() {
    CLanguage cxx = new CLanguage(settings.asConfig());

    String[] expectedSources = {".c"};
    String[] expectedHeaders = {".h"};
    String[] expectedAll = {".c", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expectedAll));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

  @Test
  public void shouldReturnConfiguredSourceSuffixes() {
    settings.setProperty(CPlugin.SOURCE_FILE_SUFFIXES_KEY, ".C,.c");
    CLanguage cxx = new CLanguage(settings.asConfig());

    String[] expectedSources = {".C", ".c"};
    String[] expectedHeaders = {".h"};
    String[] expectedAll = {".C", ".c", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expectedAll));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

  @Test
  public void shouldReturnConfiguredHeaderSuffixes() {
    settings.setProperty(CPlugin.HEADER_FILE_SUFFIXES_KEY, ".H,.h");
    CLanguage cxx = new CLanguage(settings.asConfig());

    String[] expectedSources = {".c"};
    String[] expectedHeaders = {".H", ".h"};
    String[] expectedAll = {".c", ".H", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expectedAll));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

}
