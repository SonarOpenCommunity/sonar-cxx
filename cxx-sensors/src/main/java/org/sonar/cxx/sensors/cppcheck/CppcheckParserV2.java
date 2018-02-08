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

/**
 * {@inheritDoc}
 */
public class CppcheckParserV2 implements CppcheckParser {

  private static final Logger LOG = Loggers.get(CppcheckParserV2.class);
  private final CxxCppCheckSensor sensor;

  public CppcheckParserV2(CxxCppCheckSensor sensor) {
    this.sensor = sensor;
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
                String id = errorCursor.getAttrValue("id");
                String msg = createMsg(
                  errorCursor.getAttrValue("inconclusive"),
                  errorCursor.getAttrValue("msg")
                );
                String file = null;
                String line = null;

                SMInputCursor locationCursor = errorCursor.childElementCursor("location");
                if (locationCursor.getNext() != null) {
                  file = locationCursor.getAttrValue("file");
                  line = locationCursor.getAttrValue("line");

                  if (file != null) {
                    file = file.replace('\\', '/');
                  }

                  if ("*".equals(file)) {
                    // findings on project level
                    file = null;
                    line = null;
                  }
                }

                if (isInputValid(id, msg)) {
                  sensor.saveUniqueViolation(context, CxxCppCheckRuleRepository.KEY, file, line, id, msg);
                } else {
                  LOG.warn("Skipping invalid violation: '{}'", msg);
                }
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

      private String createMsg(String inconclusive, String msg) {
        if (!msg.isEmpty() && ("true".equals(inconclusive))) {
          return "[inconclusive] " + msg;
        }
        return msg;
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
