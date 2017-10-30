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
package org.sonar.cxx.sensors.compiler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.CxxReportSensor;

/**
 * compiler for C++ with advanced analysis features (e.g. for VC 2008 team
 * edition or 2010/2012/2013/2015/2017 premium edition)
 *
 * @author Bert
 */
public class CxxCompilerSensor extends CxxReportSensor {
  private static final Logger LOG = Loggers.get(CxxCompilerSensor.class);
  public static final String REPORT_PATH_KEY = "compiler.reportPath";
  public static final String REPORT_REGEX_DEF = "compiler.regex";
  public static final String REPORT_CHARSET_DEF = "compiler.charset";
  public static final String PARSER_KEY_DEF = "compiler.parser";
  public static final String DEFAULT_PARSER_DEF = CxxCompilerVcParser.COMPILER_KEY;
  public static final String DEFAULT_CHARSET_DEF = "UTF-8";
  public static final String COMPILER_KEY = "Compiler";

  private final Map<String, CompilerParser> parsers = new HashMap<>();

  /**
   * {@inheritDoc}
   * @param settings for report sensor
   */
  public CxxCompilerSensor(CxxLanguage language, Settings settings) {
    super(language, settings);
  
    addCompilerParser(new CxxCompilerVcParser());
    addCompilerParser(new CxxCompilerGccParser());
  }

  /**
   * Add a compiler parser.
   */
  private void addCompilerParser(CompilerParser parser) {
    parsers.put(parser.key(), parser);
  }

  /**
   * Get the compiler parser to use.
   * @return CompilerParser
   */
  protected CompilerParser getCompilerParser() {
    String parserValue = this.settings.getString(this.language.getPluginProperty(PARSER_KEY_DEF));
    CompilerParser parser = parsers.get(parserValue);
    if (parser == null) {
      parserValue = language.getPluginProperty(language.getPluginProperty(DEFAULT_PARSER_DEF));
      parser = parsers.get(parserValue);
    }
    LOG.info("C-Compiler parser: '{}'", parserValue);
    return parser;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(this.language.getKey()).name(language.getName() + " CompilerSensor");
  }
  
  @Override
  public String getReportPathKey() {
    return language.getPluginProperty(REPORT_PATH_KEY);
  }

  /**
   * Get string property from configuration. If the string is not set or empty,
   * return the default value.
   *
   * @param name Name of the property
   * @param def Default value
   * @return Value of the property if set and not empty, else default value.
   */
  public String getParserStringProperty(String name, String def) {
    String s = this.settings.getString(name);
    if (s == null || s.isEmpty()) {
      return def;
    }
    return s;
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    final CompilerParser parser = getCompilerParser();
    final String reportCharset = getParserStringProperty(this.language.getPluginProperty(REPORT_CHARSET_DEF), parser.defaultCharset());
    final String reportRegEx = getParserStringProperty(this.language.getPluginProperty(REPORT_REGEX_DEF), parser.defaultRegexp());
    final List<CompilerParser.Warning> warnings = new LinkedList<>();

    // Iterate through the lines of the input file
    LOG.info("Scanner '{}' initialized with report '{}', CharSet= '{}'", parser.key(), report, reportCharset);
    try {
      parser.processReport(context, report, reportCharset, reportRegEx, warnings);
      for (CompilerParser.Warning w : warnings) {
        if (isInputValid(w)) {
          saveUniqueViolation(context, parser.rulesRepositoryKey(), w.filename, w.line, w.id, w.msg);
        } else {
          LOG.warn("C-Compiler warning: '{}''{}'", w.id, w.msg);
        }
      }
    } catch (java.io.FileNotFoundException|java.lang.IllegalArgumentException e) {
      LOG.error("processReport Exception: {} - not processed '{}'", report, e);
    }
  }

  private static boolean isInputValid(CompilerParser.Warning warning) {
    return !warning.toString().isEmpty();
  }
  
  @Override
  protected String getSensorKey() {
    return COMPILER_KEY;
  }  
}
