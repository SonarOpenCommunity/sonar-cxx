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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.internal.google.common.base.Splitter;
import org.sonar.api.internal.google.common.collect.Iterables;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.resources.Qualifiers;

/**
 * {@inheritDoc}
 */
public class CxxLanguage extends AbstractLanguage {

  /**
   * cxx language key
   */
  public static final String KEY = "c++";

  /**
   * cxx language name
   */
  public static final String NAME = "CXX";

  /**
   * Key of the file suffix parameter
   */
  public static final String FILE_SUFFIXES_KEY = "sonar.cxx.file.suffixes";

  /**
   * Default cxx files knows suffixes
   */
  public static final String DEFAULT_FILE_SUFFIXES = ".cxx,.cpp,.cc,.c,.hxx,.hpp,.hh,.h";

  /**
   * Settings of the plugin.
   */
  private final Configuration config;

  public CxxLanguage(Configuration config) {
    super(KEY, NAME);
    this.config = config;
  }

  public static List<PropertyDefinition> properties() {
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .defaultValue(DEFAULT_FILE_SUFFIXES)
        .name("File suffixes")
        .multiValues(true)
        .description("Comma-separated list of suffixes for files to analyze. To turn off the CXX language,"
                       + " set the value to 'sonar.cxx.file.suffixes=-'"
                       + " (in the user interface set the first entry to '-').")
        .category("CXX")
        .subCategory("(1) General")
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
  }

  /**
   * {@inheritDoc}
   *
   * @see org.sonar.api.resources.AbstractLanguage#getFileSuffixes()
   */
  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = Arrays.stream(config.getStringArray(FILE_SUFFIXES_KEY))
      .filter(s -> s != null && !s.trim().isEmpty()).toArray(String[]::new);
    if (suffixes.length == 0) {
      suffixes = Iterables.toArray(Splitter.on(',').split(DEFAULT_FILE_SUFFIXES), String.class);
    } else if ("-".equals(suffixes[0])) {
      suffixes = new String[]{"disabled"};
    }
    return suffixes;
  }

}
