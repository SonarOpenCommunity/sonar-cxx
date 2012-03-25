/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.plugins.cxx.CxxLanguage;

public class CxxLanguageTest {
  @Test
  public void shouldReturnConfiguredSuffixes() {
    Configuration config = new BaseConfiguration();
    config.setProperty(CxxPlugin.FILE_SUFFIXES_KEY, "C, c");
    CxxLanguage cxx = new CxxLanguage(config);

    String[] expected = {"C", "c"};
    assertThat(cxx.getFileSuffixes(), is(expected));
  }

  @Test
  public void shouldFallbackToDefaultIfNoSuffixesConfigured() {
    Configuration config = new BaseConfiguration();
    CxxLanguage cxx = new CxxLanguage(config);
    String[] suffixes = cxx.getFileSuffixes();
    assert(suffixes != null);
    assert(suffixes.length > 0);
  }
}
