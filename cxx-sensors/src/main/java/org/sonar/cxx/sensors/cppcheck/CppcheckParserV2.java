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
package org.sonar.cxx.sensors.cppcheck;

import java.io.File;
import java.nio.file.Paths;

import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.StaxParser;
import org.sonar.cxx.utils.CxxReportIssue;
import org.sonar.cxx.utils.CxxReportLocation;

/**
 * {@inheritDoc}
 */
public class CppcheckParserV2 implements CppcheckParser {

  private static final Logger LOG = Loggers.get(CppcheckParserV2.class);
  private final CxxCppCheckSensor sensor;

  public CppcheckParserV2(CxxCppCheckSensor sensor) {
    this.sensor = sensor;
  }

  private static String requireAttributeSet(@Nullable String attributeValue, String errorMsg) {
    if (attributeValue == null || attributeValue.isEmpty()) {
      throw new IllegalArgumentException(errorMsg);
    }
    return attributeValue;
  }

  private static String createIssueText(String msg, boolean isInconclusive) {
    if (isInconclusive) {
      return "[inconclusive] " + msg;
    }
    return msg;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    LOG.debug("Parsing 'Cppcheck V2' format");
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        boolean parsed = false;

        try {
          rootCursor.advance();
        } catch (com.ctc.wstx.exc.WstxEOFException eofExc) {
          throw new EmptyReportException("Cannot read cppcheck report (format V2)", eofExc);
        }

        try {
          String version = rootCursor.getAttrValue("version");
          if ("2".equals(version)) {
            SMInputCursor errorsCursor = rootCursor.childElementCursor("errors");
            if (errorsCursor.getNext() != null) {
              parsed = true;
              SMInputCursor errorCursor = errorsCursor.childElementCursor("error");

              while (errorCursor.getNext() != null) {
                processErrorTag(context, errorCursor);
              }
            }
          }
        } catch (RuntimeException e) {
          throw new XMLStreamException("processReport failed", e);
        }

        if (!parsed) {
          throw new XMLStreamException();
        }
      }

      private void processErrorTag(final SensorContext context, SMInputCursor errorCursor) throws XMLStreamException {
        String id = requireAttributeSet(errorCursor.getAttrValue("id"),
            "Missing mandatory attribute /results/errors/error[@id]");
        String msg = requireAttributeSet(errorCursor.getAttrValue("msg"),
            "Missing mandatory attribute /results/errors/error[@msg]");
        boolean isInconclusive = "true".equals(errorCursor.getAttrValue("inconclusive"));
        String issueText = createIssueText(msg, isInconclusive);
        CxxReportIssue issue = null;

        SMInputCursor locationCursor = errorCursor.childElementCursor("location");
        while (locationCursor.getNext() != null) {
          String file = locationCursor.getAttrValue("file");
          String line = locationCursor.getAttrValue("line");
          String info = locationCursor.getAttrValue("info");

          if (file != null) {
            file = file.replace('\\', '/');
          }

          if ("*".equals(file)) {
            // findings on project level
            file = null;
            line = null;
            info = null;
          }

          final boolean isLocationInProject = isLocationInProject(file, context);
          if (issue == null) {
            // primary location
            // if primary location cannot be found in the current project (in
            // the current module) we are not interested in this <error>
            if (!isLocationInProject) {
              LOG.warn("Cannot find the file '{}', skipping violations", file);
              return;
            }

            issue = new CxxReportIssue(id, file, line, issueText);
            // add the same <file>:<line> second time if there is additional
            // information about the flow/analysis
            if (info != null && !msg.equals(info)) {
              issue.addLocation(file, line, info);
            }
          } else if (info != null) {
            // secondary location
            // secondary location cannot reference a file, which is missing in
            // the current project (in the current module). If such case occurs
            // we'll use a primary location and move the affected path to the
            // info
            if (isLocationInProject) {
              issue.addLocation(file, line, info);
            } else {
              CxxReportLocation primaryLocation = issue.getLocations().get(0);
              String primaryFile = primaryLocation.getFile();
              String primaryLine = primaryLocation.getLine();

              StringBuilder extendedInfo = new StringBuilder();
              extendedInfo.append(makeRelativePath(file, primaryFile)).append(":").append(line).append(" ")
                  .append(info);
              issue.addLocation(primaryFile, primaryLine, extendedInfo.toString());
            }
          }
        }

        // no <location> tags: issue raised on the whole module/project
        if (issue == null) {
          issue = new CxxReportIssue(id, null, null, issueText);
        }
        sensor.saveUniqueViolation(context, issue);
      }

      private String makeRelativePath(String path, String basePath) {
        try {
          return Paths.get(basePath).relativize(Paths.get(path)).toString();
        } catch (IllegalArgumentException e) {
          return path;
        }
      }

      private boolean isLocationInProject(String file, final SensorContext context) {
        // file == null means that we are dealing with a warning for the whole
        // project/module
        return (file == null) || (sensor.getInputFileIfInProject(context, file) != null);
      }
    });

    parser.parse(report);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
