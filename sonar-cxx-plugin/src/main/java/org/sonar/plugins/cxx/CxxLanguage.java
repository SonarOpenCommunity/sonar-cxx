/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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

import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.config.Settings;

/**
 * {@inheritDoc}
 */
public class CxxLanguage extends AbstractLanguage {

  public static final String DEFAULT_SOURCE_SUFFIXES = ".cxx,.cpp,.cc,.c";
  public static final String DEFAULT_HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";
  public static final String DEFAULT_C_FILES = "*.c,*.C";
  public static final String KEY = "c++";

  private final String[] sourceSuffixes;
  private final String[] headerSuffixes;
  private final String[] fileSuffixes;

  /**
   * {@inheritDoc}
   */
  public CxxLanguage(Settings settings) {
    super(KEY, "c++");
    sourceSuffixes = createStringArray(settings.getStringArray(CxxPlugin.SOURCE_FILE_SUFFIXES_KEY), DEFAULT_SOURCE_SUFFIXES);
    headerSuffixes = createStringArray(settings.getStringArray(CxxPlugin.HEADER_FILE_SUFFIXES_KEY), DEFAULT_HEADER_SUFFIXES);
    fileSuffixes = mergeArrays(sourceSuffixes, headerSuffixes);
  }

  public CxxLanguage() {
    super(KEY, "c++");
    sourceSuffixes = createStringArray(null, DEFAULT_SOURCE_SUFFIXES);
    headerSuffixes = createStringArray(null, DEFAULT_HEADER_SUFFIXES);
    fileSuffixes = mergeArrays(sourceSuffixes, headerSuffixes);
  }

  private String[] mergeArrays(String[] array1, String[] array2) {
    String[] result = new String[array1.length + array2.length];
    System.arraycopy(sourceSuffixes, 0, result, 0, array1.length);
    System.arraycopy(headerSuffixes, 0, result, array1.length, array2.length);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getFileSuffixes() {
    return fileSuffixes;
  }

  /**
   * @return suffixes for c++ source files
   */
  public String[] getSourceFileSuffixes() {
    return sourceSuffixes;
  }

  /**
   * @return suffixes for c++ header files
   */
  public String[] getHeaderFileSuffixes() {
    return headerSuffixes;
  }

  private String[] createStringArray(String[] values, String defaultValues) {
    if (values == null || values.length == 0) {
      return defaultValues.split(",");
    }
    return values;
  }

}
