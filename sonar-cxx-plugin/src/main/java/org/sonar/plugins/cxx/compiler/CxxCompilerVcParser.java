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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;

import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class CxxCompilerVcParser implements CompilerParser {

  public static final String KEY = "Visual C++";
  // search for single line with compiler warning message VS2008 - order for groups: 1 = file, 2 = line, 3 = ID, 4=message
  public static final String DEFAULT_REGEX_DEF = "^.*[\\\\,/](.*)\\((\\d+)\\)\\x20:\\x20warning\\x20(C\\d+):(.*)$";
  // ToDo: as long as java 7 API is not used the support of named groups for regular expression is not possible
  // sample regex for VS2012/2013: "^.*>(?<filename>.*)\\((?<line>\\d+)\\):\\x20warning\\x20(?<id>C\\d+):(?<message>.*)$";
  // get value with e.g. scanner.match().group("filename");
  public static final String DEFAULT_CHARSET_DEF = "UTF-8"; // use "UTF-16" for VS2010 build log or TFS Team build log file

  /**
   * {@inheritDoc}
   */
  @Override
  public String key() {
    return KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String rulesRepositoryKey() {
    return CxxCompilerVcRuleRepository.KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String defaultRegexp() {
    return DEFAULT_REGEX_DEF;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String defaultCharset() {
    return DEFAULT_CHARSET_DEF;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(final Project project, final SensorContext context, File report, String charset, String reportRegEx, List<Warning> warnings) throws java.io.FileNotFoundException {
    CxxUtils.LOG.info("Parsing 'Visual C++' format ({})", charset);

    Pattern p = Pattern.compile(reportRegEx, Pattern.LITERAL);
    CxxUtils.LOG.info("Using pattern : '{}'", p);
    try (BufferedReader br = new BufferedReader(new FileReader(report))) {
      String line;
      while ((line = br.readLine()) != null) {
        
              
        if (line.contains(": warning") || line.contains(": error")) {
            try
            {
                CxxUtils.LOG.debug("Recover issue from following line : {}", line);
                // 7:5>e:\buildagent1\work\dd624fc8274443d9\solids\libsolid_kernel\euler.cpp(879): warning C4706: assignment within conditional expression
                String[] elements = line.split("\\):");
                String filename = elements[0].substring(elements[0].indexOf(">") + 1, elements[0].indexOf("(")).trim();
                String lineid = elements[0].substring(elements[0].indexOf("(") + 1);
                String remdata = elements[1].trim();
                String id = remdata.substring(remdata.indexOf(" ") + 1, remdata.indexOf(":"));
                String msg = remdata.substring(remdata.indexOf(":") + 1).trim();
                CxxUtils.LOG.debug("Scanner-matches file='{}' line='{}' id='{}' msg={}",
                  new Object[]{filename, lineid, id, msg});
                warnings.add(new Warning(filename, lineid, id, msg));                
            } catch (Exception ex) {
                CxxUtils.LOG.info("Failed to parse line : '{}'", ex.getMessage());
            }

          }        
        }       
    } catch (Exception ex){ CxxUtils.LOG.info("Failed to parse buildlog : '{}'", ex.getMessage()); }
    
  }


  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
