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
package org.sonar.cxx.toolkit;

import com.google.common.annotations.VisibleForTesting;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.impl.Parser;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.colorizer.CDocTokenizer;
import org.sonar.colorizer.CppDocTokenizer;
import org.sonar.colorizer.JavadocTokenizer;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.StringTokenizer;
import org.sonar.colorizer.Tokenizer;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.squidbridge.SquidAstVisitorContext;
import org.sonar.squidbridge.SquidAstVisitorContextImpl;
import org.sonar.squidbridge.api.SourceProject;
import org.sonar.sslr.toolkit.AbstractConfigurationModel;
import org.sonar.sslr.toolkit.ConfigurationProperty;
import org.sonar.sslr.toolkit.Validators;

public class CxxConfigurationModel extends AbstractConfigurationModel {

  private static final Logger LOG = Loggers.get(CxxConfigurationModel.class);

  private static final String CHARSET_PROPERTY_KEY = "sonar.sourceEncoding";
  private static final String ERROR_RECOVERY_PROPERTY_KEY = "sonar.cxx.errorRecoveryEnabled";
  private static final String DEFINES_PROPERTY_KEY = "sonar.cxx.defines";
  private static final String INCLUDE_DIRECTORIES_PROPERTY_KEY = "sonar.cxx.includeDirectories";
  private static final String FORCE_INCLUDES_PROPERTY_KEY = "sonar.cxx.forceIncludes";
  private final MapSettings settings = new MapSettings();

  @VisibleForTesting
  ConfigurationProperty charsetProperty = new ConfigurationProperty("Charset", CHARSET_PROPERTY_KEY,
    getPropertyOrDefaultValue(CHARSET_PROPERTY_KEY, "UTF-8"),
    Validators.charsetValidator());

  @VisibleForTesting
  ConfigurationProperty errorRecoveryEnabled = new ConfigurationProperty("Error Recovery", ERROR_RECOVERY_PROPERTY_KEY,
    getPropertyOrDefaultValue(ERROR_RECOVERY_PROPERTY_KEY, "false"),
    Validators.booleanValidator());

  @VisibleForTesting
  ConfigurationProperty defines = new ConfigurationProperty("Defines", DEFINES_PROPERTY_KEY
    + " (use \\n\\ as separator)",
    getPropertyOrDefaultValue(DEFINES_PROPERTY_KEY, ""));

  @VisibleForTesting
  ConfigurationProperty includeDirectories = new ConfigurationProperty("Include Directories",
    INCLUDE_DIRECTORIES_PROPERTY_KEY + " (use , as separator)",
    getPropertyOrDefaultValue(INCLUDE_DIRECTORIES_PROPERTY_KEY, ""));

  @VisibleForTesting
  ConfigurationProperty forceIncludes = new ConfigurationProperty("Force Includes", FORCE_INCLUDES_PROPERTY_KEY
    + " (use , as separator)",
    getPropertyOrDefaultValue(FORCE_INCLUDES_PROPERTY_KEY, ""));

  @Override
  public List<ConfigurationProperty> getProperties() {
    return Arrays.asList(charsetProperty, errorRecoveryEnabled, defines, includeDirectories, forceIncludes);
  }

  @Override
  public Charset getCharset() {
    return Charset.forName(charsetProperty.getValue());
  }

  @Override
  public Parser<? extends Grammar> doGetParser() {
    SquidAstVisitorContext<Grammar> context
      = new SquidAstVisitorContextImpl<>(new SourceProject(""));
    CppLanguage language = new CppLanguage(settings.asConfig());
    return CxxParser.create(context, getConfiguration(language), language);
  }

  @Override
  public List<Tokenizer> doGetTokenizers() {
    return Arrays.asList(
      new StringTokenizer("<span class=\"s\">", "</span>"),
      new CDocTokenizer("<span class=\"cd\">", "</span>"),
      new JavadocTokenizer("<span class=\"cppd\">", "</span>"),
      new CppDocTokenizer("<span class=\"cppd\">", "</span>"),
      new KeywordsTokenizer("<span class=\"k\">", "</span>", CxxKeyword.keywordValues())
    );
  }

  @VisibleForTesting
  CxxConfiguration getConfiguration(CxxLanguage language) {
    CxxConfiguration config = new CxxConfiguration(getCharset());
    config.setErrorRecoveryEnabled("true".equals(errorRecoveryEnabled.getValue()));
    config.setDefines(getStringLines(defines.getValue()));
    config.setIncludeDirectories(getStringArray(includeDirectories.getValue()));
    config.setForceIncludeFiles(getStringArray(forceIncludes.getValue()));
    return config;
  }

  @VisibleForTesting
  static String getPropertyOrDefaultValue(String propertyKey, String defaultValue) {
    String propertyValue = System.getProperty(propertyKey);

    if (propertyValue == null) {
      LOG.info("The property '{}' is not set, using the default value '{}'.", propertyKey, defaultValue);
      return defaultValue;
    } else {
      LOG.info("The property '{}' is set, using its value '{}'.", propertyKey, defaultValue);
      return propertyValue;
    }
  }

  static String[] getStringLines(@Nullable String value) {
    if (value == null || value.isEmpty()) {
      return new String[0];
    }
    return value.split("\\\\n\\\\", -1);
  }

  static String[] getStringArray(@Nullable String value) {
    if (value != null) {
      String[] strings = value.split(",");
      String[] result = new String[strings.length];
      for (int index = 0; index < strings.length; index++) {
        result[index] = strings[index].trim();
      }
      return result;
    }
    return new String[0];
  }

}
