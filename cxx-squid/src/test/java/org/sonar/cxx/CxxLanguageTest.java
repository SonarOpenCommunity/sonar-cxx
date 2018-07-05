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
package org.sonar.cxx;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;

public class CxxLanguageTest {

  private MapSettings settings;

  private static final String KEY = "c++";
  private static final String NAME = "c++";
  private static final String PLUGIN_ID = "cxx";
  private static final String SOURCE_SUFFIXES = ".cxx,.cpp,.cc,.c";
  private static final String HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";

  /**
   * Default cxx header files suffixes
   */
  public static final String DEFAULT_HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";

  @Before
  public void setUp() {
    settings = new MapSettings();
  }

  private class CppLanguage extends CxxLanguage {

    private final String[] sourceSuffixes;
    private final String[] headerSuffixes;
    private final String[] fileSuffixes;

    public CppLanguage(Configuration settings) {
      super(KEY, NAME, PLUGIN_ID, settings);

      sourceSuffixes = createStringArray(settings.getStringArray("sonar.cxx.suffixes.sources"), SOURCE_SUFFIXES);
      headerSuffixes = createStringArray(settings.getStringArray("sonar.cxx.suffixes.headers"), HEADER_SUFFIXES);
      fileSuffixes = mergeArrays(sourceSuffixes, headerSuffixes);
    }

    @Override
    public String[] getFileSuffixes() {
      return fileSuffixes.clone();
    }

    @Override
    public String[] getSourceFileSuffixes() {
      return sourceSuffixes.clone();
    }

    @Override
    public String[] getHeaderFileSuffixes() {
      return headerSuffixes.clone();
    }

    @Override
    public List<Class> getChecks() {
      return new ArrayList<>();
    }

    private String[] createStringArray(String[] values, String defaultValues) {
      if (values.length == 0) {
        return defaultValues.split(",");
      }
      return values;
    }

    private String[] mergeArrays(String[] array1, String[] array2) {
      String[] result = new String[array1.length + array2.length];
      System.arraycopy(sourceSuffixes, 0, result, 0, array1.length);
      System.arraycopy(headerSuffixes, 0, result, array1.length, array2.length);
      return result;
    }

    @Override
    public String getRepositoryKey() {
      return PLUGIN_ID;

    }
  }

  @Test
  public void testCxxLanguageStringConfiguration() throws Exception {
    CppLanguage language = new CppLanguage(settings.asConfig());
    assertThat(language.getKey()).isEqualTo(KEY);
  }

  @Test
  public void testGetSourceFileSuffixes() throws Exception {
    CppLanguage language = new CppLanguage(settings.asConfig());
    assertThat(language.getSourceFileSuffixes()).isEqualTo(SOURCE_SUFFIXES.split(","));
  }

  @Test
  public void testGetHeaderFileSuffixes() throws Exception {
    CppLanguage language = new CppLanguage(settings.asConfig());
    assertThat(language.getHeaderFileSuffixes()).isEqualTo(HEADER_SUFFIXES.split(","));
  }

  @Test
  public void testGetPropertiesKey() throws Exception {
    CppLanguage language = new CppLanguage(settings.asConfig());
    assertThat(language.getPropertiesKey()).isEqualTo(PLUGIN_ID);
  }

  @Test
  public void testGetChecks() throws Exception {
    CppLanguage language = new CppLanguage(settings.asConfig());
    assertThat(language.getChecks()).isEmpty();
  }

  @Test
  public void testGetRepositoryKey() throws Exception {
    CppLanguage language = new CppLanguage(settings.asConfig());
    assertThat(language.getPropertiesKey()).isEqualTo(PLUGIN_ID);
  }

  @Test
  public void testGetRepositorySuffix() throws Exception {
    CppLanguage language = new CppLanguage(settings.asConfig());
    assertThat(language.getRepositorySuffix()).isEqualTo("");
  }

  @Test
  public void testGetPluginProperty() throws Exception {
    CppLanguage language = new CppLanguage(settings.asConfig());
    assertThat(language.getPropertiesKey()).isEqualTo(PLUGIN_ID);
  }

  @Test
  public void testIsRecoveryEnabled() throws Exception {
    CppLanguage language = new CppLanguage(settings.asConfig());
    assertThat(language.getBooleanOption("sonar.cxx.errorRecoveryEnabled")).isEqualTo(Optional.empty());
  }

  @Test
  public void testHasKey() throws Exception {
    CppLanguage language = new CppLanguage(settings.asConfig());
    assertThat(language.hasKey("sonar.cxx.errorRecoveryEnabled")).isEqualTo(Boolean.FALSE);
  }

}
