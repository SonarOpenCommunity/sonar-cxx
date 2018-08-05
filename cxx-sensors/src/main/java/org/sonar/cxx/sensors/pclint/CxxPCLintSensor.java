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
package org.sonar.cxx.sensors.pclint;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.StaxParser;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * PC-lint is an equivalent to pmd but for C++ The first version of the tool was release 1985 and the tool analyzes
 * C/C++ source code from many compiler vendors. PC-lint is the version for Windows and FlexLint for Unix, VMS, OS-9,
 * etc See also: http://www.gimpel.com/html/index.htm
 *
 * @author Bert
 */
public class CxxPCLintSensor extends CxxIssuesReportSensor {

  private static final Logger LOG = Loggers.get(CxxPCLintSensor.class);
  public static final String REPORT_PATH_KEY = "pclint.reportPath";
  public static final Pattern misraRulePattern = Pattern.compile(
      // Rule nn.nn -or- Rule nn-nn-nn
      "Rule\\x20(\\d{1,2}.\\d{1,2}|\\d{1,2}-\\d{1,2}-\\d{1,2})(,|\\])");

  /**
   * CxxPCLintSensor for PC-lint Sensor
   *
   * @param language defines settings C or C++
   */
  public CxxPCLintSensor(CxxLanguage language) {
    super(language, REPORT_PATH_KEY, CxxPCLintRuleRepository.getRepositoryKey(language));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(getLanguage().getName() + " PCLintSensor")
      .onlyOnLanguage(getLanguage().getKey())
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    LOG.debug("Parsing 'PC-Lint' format");

    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        try {
          rootCursor.advance();
        } catch (com.ctc.wstx.exc.WstxEOFException eofExc) {
          throw new EmptyReportException("Cannot read PClint report", eofExc);
        }

        SMInputCursor errorCursor = rootCursor.childElementCursor("issue");
        try {
          while (errorCursor.getNext() != null) {
            String file = errorCursor.getAttrValue("file");
            String line = errorCursor.getAttrValue("line");
            String id = errorCursor.getAttrValue("number");
            String msg = errorCursor.getAttrValue("desc");

            if (isInputValid(file, line, id, msg)) {
              if (msg.contains("MISRA")) {
                //remap MISRA IDs. Only Unique rules for MISRA C 2004 and MISRA C/C++ 2008
                // have been created in the rule repository
                if (msg.contains("MISRA 2004") || msg.contains("MISRA 2008")
                  || msg.contains("MISRA C++ 2008") || msg.contains("MISRA C++ Rule")) {
                  id = mapMisraRulesToUniqueSonarRules(msg, Boolean.FALSE);
                } else if (msg.contains("MISRA 2012 Rule")) {
                  id = mapMisraRulesToUniqueSonarRules(msg, Boolean.TRUE);
                }
              }

              CxxReportIssue issue = new CxxReportIssue(id, file, line, msg);
              saveUniqueViolation(context, issue);
            } else {
              LOG.warn("PC-lint warning ignored: {}", msg);
              if (LOG.isDebugEnabled()) {
                LOG.debug("File: {}, Line: {}, ID: {}, msg: {}", file, line, id, msg);
              }
            }
          }
        } catch (com.ctc.wstx.exc.WstxUnexpectedCharException
          | com.ctc.wstx.exc.WstxEOFException
          | com.ctc.wstx.exc.WstxIOException e) {
          LOG.error("Ignore XML error from PC-lint '{}'", CxxUtils.getStackTrace(e));
        }
      }

      private boolean isInputValid(@Nullable String file, @Nullable String line,
        @Nullable String id, @Nullable String msg) {
        try {
          if (file == null || file.isEmpty() || (Integer.parseInt(line) == 0)) {
            // issue for project or file level
            return id != null && !id.isEmpty() && msg != null && !msg.isEmpty();
          }
          return !file.isEmpty() && id != null && !id.isEmpty() && msg != null && !msg.isEmpty();
        } catch (java.lang.NumberFormatException e) {
          LOG.error("Ignore number error from PC-lint report '{}'", CxxUtils.getStackTrace(e));
        }
        return false;
      }

      /**
       * Concatenate M with the MISRA rule number to get the new rule id to save the violation to.
       */
      private String mapMisraRulesToUniqueSonarRules(String msg, Boolean isMisra2012) {
        Matcher matcher = misraRulePattern.matcher(msg);
        if (matcher.find()) {
          String misraRule = matcher.group(1);
          String newKey;
          if (isMisra2012) {
            newKey = "M2012-" + misraRule;
          } else {
            newKey = "M" + misraRule;
          }
          if (LOG.isDebugEnabled()) {
            LOG.debug("Remap MISRA rule {} to key {}", misraRule, newKey);
          }
          return newKey;
        }
        return "";
      }
    });

    parser.parse(report);
  }

  @Override
  protected CxxMetricsFactory.Key getMetricKey() {
    return CxxMetricsFactory.Key.PCLINT_SENSOR_ISSUES_KEY;
  }
}
