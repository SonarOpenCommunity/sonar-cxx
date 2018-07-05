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
package org.sonar.cxx.sensors.compiler;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.CxxReportIssue;
import org.sonar.cxx.sensors.utils.CxxReportSensor;

/**
 * compiler for C++ with advanced analysis features (e.g. for VC 2008 team edition or 2010/2012/2013/2015/2017 premium
 * edition)
 *
 * @author Bert
 */
public class CxxCompilerSensor extends CxxReportSensor {

  private static final Logger LOG = Loggers.get(CxxCompilerSensor.class);
  public static final String REPORT_PATH_KEY = "compiler.reportPath";
  public static final String REPORT_REGEX_DEF = "compiler.regex";
  public static final String REPORT_CHARSET_DEF = "compiler.charset";
  public static final String PARSER_KEY_DEF = "compiler.parser";
  public static final String DEFAULT_PARSER_DEF = CxxCompilerVcParser.KEY_VC;
  public static final String DEFAULT_CHARSET_DEF = "UTF-8";
  public static final String KEY = "Compiler";

  private final Map<String, CompilerParser> parsers = new HashMap<>();

  /**
   * CxxCompilerSensor for Visual Studio C++ Compiler Sensor
   *
   * @param language defines settings C or C++
   */
  public CxxCompilerSensor(CxxLanguage language) {
    super(language);

    addCompilerParser(new CxxCompilerVcParser());
    addCompilerParser(new CxxCompilerGccParser());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(language.getName() + " CompilerSensor")
      .onlyOnLanguage(this.language.getKey())
      .createIssuesForRuleRepositories(CxxCompilerGccRuleRepository.KEY, CxxCompilerVcRuleRepository.KEY)
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  public String getReportPathKey() {
    return language.getPluginProperty(REPORT_PATH_KEY);
  }

  /**
   * Add a compiler parser.
   */
  private void addCompilerParser(CompilerParser parser) {
    parsers.put(parser.key(), parser);
  }

  /**
   * Get the compiler parser to use.
   *
   * @return CompilerParser
   */
  protected CompilerParser getCompilerParser(SensorContext context) {
    Optional<String> parserValue = context.config().get(this.language.getPluginProperty(PARSER_KEY_DEF));
    String parserDefault = this.language.getPluginProperty(this.language.getPluginProperty(DEFAULT_PARSER_DEF));
    CompilerParser parser = parsers.get(parserDefault);
    if (parserValue.isPresent()) {
      parser = parsers.get(parserValue.get());
    }
    LOG.info("C-Compiler parser: '{}'", parserValue);
    return parser;
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    final CompilerParser parser = getCompilerParser(context);
    final String reportCharset = getContextStringProperty(context,
      this.language.getPluginProperty(REPORT_CHARSET_DEF), parser.defaultCharset());
    final String reportRegEx = getContextStringProperty(context,
      this.language.getPluginProperty(REPORT_REGEX_DEF), parser.defaultRegexp());
    final List<CompilerParser.Warning> warnings = new LinkedList<>();

    // Iterate through the lines of the input file
    LOG.info("Scanner '{}' initialized with report '{}', CharSet= '{}'", parser.key(), report, reportCharset);
    try {
      parser.processReport(context, report, reportCharset, reportRegEx, warnings);
      for (CompilerParser.Warning w : warnings) {
        if (isInputValid(w)) {
          CxxReportIssue issue = new CxxReportIssue(parser.rulesRepositoryKey(), w.id, w.filename, w.line, w.msg);
          saveUniqueViolation(context, issue);
        } else {
          LOG.warn("C-Compiler warning: '{}''{}'", w.id, w.msg);
        }
      }
    } catch (java.io.FileNotFoundException | java.lang.IllegalArgumentException e) {
      LOG.error("processReport Exception: {} - not processed '{}'", report, e);
    }
  }

  private static boolean isInputValid(CompilerParser.Warning warning) {
    return !warning.toString().isEmpty();
  }

  @Override
  protected String getSensorKey() {
    return KEY;
  }

  @Override
  protected Optional<CxxMetricsFactory.Key> getMetricKey() {
    return Optional.of(CxxMetricsFactory.Key.COMPILER_SENSOR_ISSUES_KEY);
  }
}
