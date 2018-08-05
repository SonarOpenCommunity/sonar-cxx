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
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.StaxParser;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * {@inheritDoc}
 */
public class CppcheckParserV1 implements CppcheckParser {

  private static final Logger LOG = Loggers.get(CppcheckParserV1.class);
  private final CxxCppCheckSensor sensor;

  public CppcheckParserV1(CxxCppCheckSensor sensor) {
    this.sensor = sensor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(final SensorContext context, File report) throws XMLStreamException {
    LOG.debug("Parsing 'Cppcheck V1' format");
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {

        try {
          rootCursor.advance(); // results
        } catch (com.ctc.wstx.exc.WstxEOFException eofExc) {
          throw new EmptyReportException("Cannot read cppcheck report (format V1)", eofExc);
        }

        try {
          SMInputCursor errorCursor = rootCursor.childElementCursor("error"); // error
          while (errorCursor.getNext() != null) {
            String file = errorCursor.getAttrValue("file");
            String line = errorCursor.getAttrValue("line");
            String id = errorCursor.getAttrValue("id");
            String msg = errorCursor.getAttrValue("msg");

            if (file != null) {
              file = file.replace('\\', '/');
            }

            if ("*".equals(file)) {
              // findings on project level
              file = null;
              line = null;
            }

            if (isInputValid(id, msg)) {
              CxxReportIssue issue = new CxxReportIssue(id, file, line, msg);
              sensor.saveUniqueViolation(context, issue);
            } else {
              LOG.warn("Skipping invalid violation: '{}'", msg);
            }
          }
        } catch (RuntimeException e) {
          throw new XMLStreamException("processReport failed", e);
        }
      }

      private boolean isInputValid(String id, String msg) {
        return !id.isEmpty() && msg != null && !msg.isEmpty();
      }
    });

    parser.parse(report);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
