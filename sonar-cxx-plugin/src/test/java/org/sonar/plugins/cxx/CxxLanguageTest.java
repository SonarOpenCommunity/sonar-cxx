/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CxxLanguageTest {

  private Settings config;

  @Before
  public void setup() {
    config = new Settings();
  }

  @Test
  public void shouldReturnConfiguredFileSuffixes() {
    config.setProperty(CxxPlugin.SOURCE_FILE_SUFFIXES_KEY, ".C,.c");
    config.setProperty(CxxPlugin.HEADER_FILE_SUFFIXES_KEY, ".H,.h");
    CxxLanguage cxx = new CxxLanguage(config);

    String[] expected = {".C", ".c", ".H", ".h"};
    String[] expectedSources = {".C", ".c"};
    String[] expectedHeaders = {".H", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expected));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

  @Test
  public void shouldReturnDefaultFileSuffixes() {
    CxxLanguage cxx = new CxxLanguage(config);

    String[] expectedSources = {".cxx", ".cpp", ".cc", ".c"};
    String[] expectedHeaders = {".hxx", ".hpp", ".hh", ".h"};
    String[] expectedAll = {".cxx", ".cpp", ".cc", ".c", ".hxx", ".hpp", ".hh", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expectedAll));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

  @Test
  public void shouldReturnConfiguredSourceSuffixes() {
    config.setProperty(CxxPlugin.SOURCE_FILE_SUFFIXES_KEY, ".C,.c");
    CxxLanguage cxx = new CxxLanguage(config);

    String[] expectedSources = {".C", ".c"};
    String[] expectedHeaders = {".hxx", ".hpp", ".hh", ".h"};
    String[] expectedAll = {".C", ".c", ".hxx", ".hpp", ".hh", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expectedAll));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

  @Test
  public void shouldReturnConfiguredHeaderSuffixes() {
    config.setProperty(CxxPlugin.HEADER_FILE_SUFFIXES_KEY, ".H,.h");
    CxxLanguage cxx = new CxxLanguage(config);

    String[] expectedSources = {".cxx", ".cpp", ".cc", ".c"};
    String[] expectedHeaders = {".H", ".h"};
    String[] expectedAll = {".cxx", ".cpp", ".cc", ".c", ".H", ".h"};

    assertThat(cxx.getFileSuffixes(), is(expectedAll));
    assertThat(cxx.getSourceFileSuffixes(), is(expectedSources));
    assertThat(cxx.getHeaderFileSuffixes(), is(expectedHeaders));
  }

}
