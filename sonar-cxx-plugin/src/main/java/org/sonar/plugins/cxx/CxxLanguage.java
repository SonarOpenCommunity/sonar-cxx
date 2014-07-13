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

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.scan.filesystem.FileQuery;

/**
 * {@inheritDoc}
 */
public class CxxLanguage extends AbstractLanguage {
  public static final String DEFAULT_SOURCE_SUFFIXES = ".cxx,.cpp,.cc,.c";
  public static final String DEFAULT_HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";
  public static final String KEY = "c++";

  private String[] sourceSuffixes;
  private String[] headerSuffixes;
  private String[] fileSuffixes;

  public static FileQuery sourceQuery = FileQuery.onSource().onLanguage(KEY);
  public static FileQuery testQuery = FileQuery.onTest().onLanguage(KEY);

  /**
   * {@inheritDoc}
   */
  public CxxLanguage(Settings config) {
    super(KEY, "c++");
    sourceSuffixes = createStringArray(config.getStringArray(CxxPlugin.SOURCE_FILE_SUFFIXES_KEY), DEFAULT_SOURCE_SUFFIXES);
    headerSuffixes = createStringArray(config.getStringArray(CxxPlugin.HEADER_FILE_SUFFIXES_KEY), DEFAULT_HEADER_SUFFIXES);
    fileSuffixes = mergeArrays(sourceSuffixes, headerSuffixes);
  }

  public CxxLanguage() {
    super(KEY, "c++");
    sourceSuffixes = createStringArray(null, DEFAULT_SOURCE_SUFFIXES);
    headerSuffixes = createStringArray(null, DEFAULT_HEADER_SUFFIXES);
    fileSuffixes = mergeArrays(sourceSuffixes, headerSuffixes);
  }

  public final String[] mergeArrays(String[] array1, String[] array2) {
    String[] result = new String[array1.length + array2.length];
    System.arraycopy(sourceSuffixes, 0, result, 0, array1.length);
    System.arraycopy(headerSuffixes, 0, result, array1.length, array2.length);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public String[] getFileSuffixes() {
    return fileSuffixes;
  }

  /**
   * @return  suffixes for c++ source files
   */
  public String[] getSourceFileSuffixes() {
    return sourceSuffixes;
  }

  /**
   * @return  suffixes for c++ header files
   */
  public String[] getHeaderFileSuffixes() {
    return headerSuffixes;
  }

  private String[] createStringArray(String[] values, String defaultValues) {
    if (values == null || values.length == 0) {
      return StringUtils.split(defaultValues, ",");
    }
    return values;
  }

}
