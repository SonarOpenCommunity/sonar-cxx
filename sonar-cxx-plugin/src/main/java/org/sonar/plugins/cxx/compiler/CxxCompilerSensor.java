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
package org.sonar.plugins.cxx.compiler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * compiler for C++ with advanced analysis features (e.g. for VC 2008 team edition or 2010/2012/2013 premium edition)
 *
 * @author Bert
 */
public class CxxCompilerSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.compiler.reportPath";
  public static final String REPORT_REGEX_DEF = "sonar.cxx.compiler.regex";
  public static final String REPORT_CHARSET_DEF = "sonar.cxx.compiler.charset";
  public static final String PARSER_KEY_DEF = "sonar.cxx.compiler.parser";
  public static final String DEFAULT_PARSER_DEF = CxxCompilerVcParser.KEY;
  public static final String BUILD_LOG_KEY = "sonar.cxx.compiler.buildLog";

  private final RulesProfile profile;
  private final Map<String, CompilerParser> parsers = new HashMap<String, CompilerParser>();

  /**
   * {@inheritDoc}
   */
  public CxxCompilerSensor(ResourcePerspectives perspectives, Settings conf, ModuleFileSystem fs, RulesProfile profile) {
    super(perspectives, conf, fs, CxxMetrics.COMPILER);
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
  private CompilerParser getCompilerParser() {
    String parserKey = getStringProperty(PARSER_KEY_DEF, DEFAULT_PARSER_DEF);
    CompilerParser parser = parsers.get(parserKey);
    if (parser == null)
        parser = parsers.get(DEFAULT_PARSER_DEF);
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

  @Override
  protected String defaultReportPath() {
    return getCompilerParser().defaultReportPath();
  }

  /**
   * Get string property from configuration.
   * If the string is not set or empty, return the default value.
   * @param name Name of the property
   * @param def Default value
   * @return Value of the property if set and not empty, else default value.
   */
  public String getParserStringProperty(String name, String def) {
      String s = getStringProperty(name, "");
      if (StringUtils.isEmpty(s))
          return def;
      return s;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
      throws javax.xml.stream.XMLStreamException
  {
    final CompilerParser parser = getCompilerParser();
    final String reportCharset = getParserStringProperty(REPORT_CHARSET_DEF, parser.defaultCharset());
    final String reportRegEx = getParserStringProperty(REPORT_REGEX_DEF, parser.defaultRegexp());
    final List<CompilerParser.Warning> warnings = new LinkedList<CompilerParser.Warning>();

    // Iterate through the lines of the input file
    CxxUtils.LOG.info("Scanner '" + parser.key() + "' initialized with report '{}'" + ", CharSet= '" + reportCharset + "'", report);
    try {
      parser.parseReport(report, reportCharset, reportRegEx, warnings);
      for(CompilerParser.Warning w : warnings) {
        // get filename from file system - e.g. VC writes case insensitive file name to html
        if (isInputValid(w.filename, w.line, w.id, w.msg)) {
          saveUniqueViolation(project, context, parser.rulesRepositoryKey(), w.filename, w.line, w.id, w.msg);
        } else {
          CxxUtils.LOG.warn("C-Compiler warning: {}", w.msg);
        }
      }
    } catch (java.io.FileNotFoundException e) {
      CxxUtils.LOG.error("processReport Exception: " + "report.getName" + " - not processed '{}'", e.toString());
    } catch (java.lang.IllegalArgumentException e1) {
      CxxUtils.LOG.error("processReport Exception: " + "report.getName" + " - not processed '{}'", e1.toString());
    }
  }

  private boolean isInputValid(String file, String line, String id, String msg) {
    return !StringUtils.isEmpty(file) && !StringUtils.isEmpty(line)
      && !StringUtils.isEmpty(id) && !StringUtils.isEmpty(msg);
  }
}
