/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
package org.sonar.cxx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;

public class CxxLanguageTest {

  /**
   * Default cxx header files suffixes
   */
  public static final String DEFAULT_HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";

  private static final String KEY = "c++";
  private static final String NAME = "c++";
  private static final String PLUGIN_ID = "cxx";
  private static final String SOURCE_SUFFIXES = ".cxx,.cpp,.cc,.c";
  private static final String HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";

  private final MapSettings settings = new MapSettings();

  @Test
  public void testCxxLanguageStringConfiguration() throws Exception {
    CxxLanguage language = new CxxLanguage(settings.asConfig());
    assertThat(language.getKey()).isEqualTo(KEY);
  }

  @Test
  public void testGetSourceFileSuffixes() throws Exception {
    CxxLanguage language = new CxxLanguage(settings.asConfig());
    assertThat(language.getSourceFileSuffixes()).isEqualTo(SOURCE_SUFFIXES.split(","));
  }

  @Test
  public void testGetHeaderFileSuffixes() throws Exception {
    CxxLanguage language = new CxxLanguage(settings.asConfig());
    assertThat(language.getHeaderFileSuffixes()).isEqualTo(HEADER_SUFFIXES.split(","));
  }

  @Test
  public void shouldReturnConfiguredFileSuffixes() {
    settings.setProperty(CxxLanguage.SOURCE_FILE_SUFFIXES_KEY, ".C,.c");
    settings.setProperty(CxxLanguage.HEADER_FILE_SUFFIXES_KEY, ".H,.h");
    CxxLanguage cxx = new CxxLanguage(settings.asConfig());

    String[] expected = {".C", ".c", ".H", ".h"};
    String[] expectedSources = {".C", ".c"};
    String[] expectedHeaders = {".H", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expected));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

  @Test
  public void shouldReturnDefaultFileSuffixes() {
    CxxLanguage cxx = new CxxLanguage(settings.asConfig());

    String[] expectedSources = {".cxx", ".cpp", ".cc", ".c"};
    String[] expectedHeaders = {".hxx", ".hpp", ".hh", ".h"};
    String[] expectedAll = {".cxx", ".cpp", ".cc", ".c", ".hxx", ".hpp", ".hh", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expectedAll));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

  @Test
  public void shouldReturnConfiguredSourceSuffixes() {
    settings.setProperty(CxxLanguage.SOURCE_FILE_SUFFIXES_KEY, ".C,.c");
    CxxLanguage cxx = new CxxLanguage(settings.asConfig());

    String[] expectedSources = {".C", ".c"};
    String[] expectedHeaders = {".hxx", ".hpp", ".hh", ".h"};
    String[] expectedAll = {".C", ".c", ".hxx", ".hpp", ".hh", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expectedAll));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

  @Test
  public void shouldReturnConfiguredHeaderSuffixes() {
    settings.setProperty(CxxLanguage.HEADER_FILE_SUFFIXES_KEY, ".H,.h");
    CxxLanguage cxx = new CxxLanguage(settings.asConfig());

    String[] expectedSources = {".cxx", ".cpp", ".cc", ".c"};
    String[] expectedHeaders = {".H", ".h"};
    String[] expectedAll = {".cxx", ".cpp", ".cc", ".c", ".H", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expectedAll));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

}
