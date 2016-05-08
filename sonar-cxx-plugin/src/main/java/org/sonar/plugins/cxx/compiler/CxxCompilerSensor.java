/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.compiler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * compiler for C++ with advanced analysis features (e.g. for VC 2008 team
 * edition or 2010/2012/2013 premium edition)
 *
 * @author Bert
 */
public class CxxCompilerSensor extends CxxReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.compiler.reportPath";
  public static final String REPORT_REGEX_DEF = "sonar.cxx.compiler.regex";
  public static final String REPORT_CHARSET_DEF = "sonar.cxx.compiler.charset";
  public static final String PARSER_KEY_DEF = "sonar.cxx.compiler.parser";
  public static final String DEFAULT_PARSER_DEF = CxxCompilerVcParser.KEY;
  public static final String DEFAULT_CHARSET_DEF = "UTF-8";

  private final RulesProfile profile;
  private final Map<String, CompilerParser> parsers = new HashMap<>();

  /**
   * {@inheritDoc}
   */
  public CxxCompilerSensor(ResourcePerspectives perspectives, Settings settings, FileSystem fs, RulesProfile profile) {
    super(perspectives, settings, fs, CxxMetrics.COMPILER);
    this.profile = profile;

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
   */
  protected CompilerParser getCompilerParser() {
    String parserKey = getStringProperty(PARSER_KEY_DEF, DEFAULT_PARSER_DEF);
    CompilerParser parser = parsers.get(parserKey);
    if (parser == null) {
      parser = parsers.get(DEFAULT_PARSER_DEF);
    }
    return parser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(getCompilerParser().rulesRepositoryKey()).isEmpty();
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
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
    String s = getStringProperty(name, "");
    if (s == null || s.isEmpty()) {
      return def;
    }
    return s;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    final CompilerParser parser = getCompilerParser();
    final String reportCharset = getParserStringProperty(REPORT_CHARSET_DEF, parser.defaultCharset());
    final String reportRegEx = getParserStringProperty(REPORT_REGEX_DEF, parser.defaultRegexp());
    final List<CompilerParser.Warning> warnings = new LinkedList<>();

    // Iterate through the lines of the input file
    CxxUtils.LOG.info("Scanner '{}' initialized with report '{}', CharSet= '{}'",
      new Object[]{parser.key(), report, reportCharset});
    try {
      parser.processReport(project, context, report, reportCharset, reportRegEx, warnings);
      for (CompilerParser.Warning w : warnings) {
        if (isInputValid(w)) {
          saveUniqueViolation(project, context, parser.rulesRepositoryKey(), w.filename, w.line, w.id, w.msg);
        } else {
          CxxUtils.LOG.warn("C-Compiler warning: '{}''{}'", w.id, w.msg);
        }
      }
    } catch (java.io.FileNotFoundException|java.lang.IllegalArgumentException e) {
      CxxUtils.LOG.error("processReport Exception: {} - not processed '{}'", report, e);
    }
  }

  private boolean isInputValid(CompilerParser.Warning warning) {
    return warning != null && !warning.toString().isEmpty();
  }
}
