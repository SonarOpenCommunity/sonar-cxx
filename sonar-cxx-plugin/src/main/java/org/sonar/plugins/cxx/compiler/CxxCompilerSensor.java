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

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * compiler for C++ with advanced analysis features (e.g. for VC 2008 team edition or 2010/2012/2013 premium edition)
 *
 * @author Bert
 */
public class CxxCompilerSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.compiler.reportPath";
  private static final String DEFAULT_REPORT_PATH = "compiler-reports/BuildLog.htm";
  public static final String REPORT_REGEX_DEF = "sonar.cxx.compiler.regex";
  // search for single line with compiler warning message - order for groups: 1 = file, 2 = line, 3 = ID, 4=message
  public static final String DEFAULT_REGEX_DEF = "^.*[\\\\,/](.*)\\(([0-9]+)\\)\\x20:\\x20warning\\x20(C\\d\\d\\d\\d):(.*)$";
  // ToDo: as long as java 7 API is not used the support of named groups for regular expression is not possible
  // sample regex: "^.*[\\\\,/](?<filename>.*)\\((?<line>[0-9]+)\\)\\x20:\\x20warning\\x20(?<id>C\\d\\d\\d\\d):(?<message>.*)$";
  // get value with e.g. scanner.match().group("filename");
  public static final String REPORT_CHARSET_DEF = "sonar.cxx.compiler.charset";
  public static final String DEFAULT_CHARSET_DEF = "UTF-16";
  private RulesProfile profile;
  private HashSet<String> uniqueIssues = new HashSet<String>();

  /**
   * {@inheritDoc}
   */
  public CxxCompilerSensor(RuleFinder ruleFinder, Settings conf, ModuleFileSystem fs, RulesProfile profile) {
    super(ruleFinder, conf, fs);
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxCompilerRuleRepository.KEY).isEmpty();
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
      throws javax.xml.stream.XMLStreamException
  {
    int countViolations = 0;
    String reportCharset = getStringProperty(REPORT_CHARSET_DEF, DEFAULT_CHARSET_DEF);
    String reportRegEx = getStringProperty(REPORT_REGEX_DEF, DEFAULT_REGEX_DEF);
    // Iterate through the lines of the input file
    try {
      CxxUtils.LOG.debug("Scanner initialized with report '{}'" + ", CharSet= '" + reportCharset + "'", report);
      Scanner scanner = new Scanner(report, reportCharset);
      Pattern p = Pattern.compile(reportRegEx, Pattern.MULTILINE);
      CxxUtils.LOG.debug("Using pattern : '" + p.toString() + "'");
      while (scanner.findWithinHorizon(p, 0) != null)
      {
        String filename = scanner.match().group(1);
        String line = scanner.match().group(2);
        String id = scanner.match().group(3);
        String msg = scanner.match().group(4);
        // get filename from file system - e.g. VC writes case insensitive file name to html
        filename = getCaseSensitiveFileName(filename, fs.sourceDirs());
        CxxUtils.LOG.debug("Scanner-matches file='" + filename + "' line='" + line + "' id='" + id + "' msg=" + msg);
        if (isInputValid(filename, line, id, msg)) {
            if (uniqueIssues.add(filename + line + id + msg))
            {
            saveViolation(project, context, CxxCompilerRuleRepository.KEY, filename, line, id, msg);
            countViolations++;
            }
        } else {
          CxxUtils.LOG.warn("C-Compiler warning: {}", msg);
        }
      }
      scanner.close();
      CxxUtils.LOG.info("C-Compiler warnings processed = " + countViolations);
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

  /**
   *  Supports full path and relative path in report.xml file.
   */     
  private String getCaseSensitiveFileName(String file, List<java.io.File> sourceDirs) {
    // check whether the report file uses absolute path
    File targetfile = new java.io.File(file);
    if (targetfile.exists()) {
      file = getRealFileName(targetfile);
    } else {
      Iterator<java.io.File> iterator = sourceDirs.iterator();
      while (iterator.hasNext()) {              
           targetfile = new java.io.File(iterator.next().getPath() + java.io.File.separatorChar + file);
           if (targetfile.exists()) {
               file = getRealFileName(targetfile);
               break;
           }
      }
    }
    return file;      
  }
     
  /**
   * Find the case sensitive file name - tools might use different naming schema
   * e.g. VC HTML or build log report uses case insensitive file name (lower case on windows)
   */      
  private String getRealFileName( File filename){
     try {
         return filename.getCanonicalFile().getAbsolutePath();
     } catch (java.io.IOException e) {
       CxxUtils.LOG.error("SaveViolation GetRealFileName failed '{}'", e.toString());
       }
     return filename.getName();
  }

  
}
