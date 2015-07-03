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
package org.sonar.plugins.cxx.pclint;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.plugins.cxx.utils.EmptyReportException;
import org.sonar.api.batch.bootstrap.ProjectReactor;

/**
 * PC-lint is an equivalent to pmd but for C++ The first version of the tool was
 * release 1985 and the tool analyzes C/C++ source code from many compiler
 * vendors. PC-lint is the version for Windows and FlexLint for Unix, VMS, OS-9,
 * etc See also: http://www.gimpel.com/html/index.htm
 *
 * @author Bert
 */
public class CxxPCLintSensor extends CxxReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.pclint.reportPath";
  private final RulesProfile profile;

  /**
   * {@inheritDoc}
   */
  public CxxPCLintSensor(ResourcePerspectives perspectives, Settings conf, FileSystem fs, RulesProfile profile, ProjectReactor reactor) {
    super(perspectives, conf, fs, reactor, CxxMetrics.PCLINT);
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxPCLintRuleRepository.KEY).isEmpty();
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    CxxUtils.LOG.info("Parsing 'PC-Lint' format");

    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        try {
          rootCursor.advance();
        } catch (com.ctc.wstx.exc.WstxEOFException eofExc) {
          throw new EmptyReportException();
        }

        SMInputCursor errorCursor = rootCursor.childElementCursor("issue");
        try {
          while (errorCursor.getNext() != null) {
            String file = errorCursor.getAttrValue("file");
            String line = errorCursor.getAttrValue("line");
            String id = errorCursor.getAttrValue("number");
            String msg = errorCursor.getAttrValue("desc");
            
            if (isInputValid(file, line, id, msg)) {
              //remap MISRA IDs. Only Unique rules for MISRA 2004 and 2008 has been created in the rule repository
              if (msg.contains("MISRA 2004") || msg.contains("MISRA 2008")) {
                id = mapMisraRulesToUniqueSonarRules(msg);
              }
              saveUniqueViolation(project, context, CxxPCLintRuleRepository.KEY,
                file, line, id, msg);
            } else {
              CxxUtils.LOG.warn("PC-lint warning ignored: {}", msg);
              CxxUtils.LOG.debug("File: " + file + ", Line: " + line + ", ID: "
                + id + ", msg: " + msg);
            }
          }
        } catch (com.ctc.wstx.exc.WstxUnexpectedCharException e) {
          CxxUtils.LOG.error("Ignore XML error from PC-lint '{}'", e.toString());
        } catch (com.ctc.wstx.exc.WstxEOFException e) {
          CxxUtils.LOG.error("Ignore XML error from PC-lint '{}'", e.toString());
        }
      }

      private boolean isInputValid(String file, String line, String id, String msg) {
        if (StringUtils.isEmpty(file) || (Integer.valueOf(line) == 0)) {
          // issue for project or file level
          return !StringUtils.isEmpty(id) && !StringUtils.isEmpty(msg);
        }
        return !StringUtils.isEmpty(file) && !StringUtils.isEmpty(id) && !StringUtils.isEmpty(msg);
      }

      /**
       * Concatenate M with the MISRA rule number to get the new rule id to save
       * the violation to.
       */
      private String mapMisraRulesToUniqueSonarRules(String msg) {
        Pattern pattern = Pattern.compile(
          // Rule nn.nn -or- Rule nn-nn-nn
          "Rule\\x20(\\d{1,2}.\\d{1,2}|\\d{1,2}-\\d{1,2}-\\d{1,2})(,|\\])"
        );
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
          String misraRule = matcher.group(1);
          String newKey = "M" + misraRule;
          String debugText = "Remap MISRA rule " + misraRule + " to key " + newKey;
          CxxUtils.LOG.debug(debugText);
          return newKey;
        }
        return "";
      }
    });

    parser.parse(report);
  }
}
