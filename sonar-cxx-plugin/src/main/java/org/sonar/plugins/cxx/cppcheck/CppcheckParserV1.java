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
package org.sonar.plugins.cxx.cppcheck;

import java.io.File;

import javax.xml.stream.XMLStreamException;
import org.sonar.api.batch.sensor.SensorContext;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.plugins.cxx.utils.EmptyReportException;
import org.sonar.plugins.cxx.utils.StaxParser;

/**
 * {@inheritDoc}
 */
public class CppcheckParserV1 implements CppcheckParser {

  private final CxxCppCheckSensor sensor;

  public CppcheckParserV1(CxxCppCheckSensor sensor) {
    this.sensor = sensor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    CxxUtils.LOG.debug("Parsing 'Cppcheck V1' format");
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {

        try {
          rootCursor.advance(); // results
        } catch (com.ctc.wstx.exc.WstxEOFException eofExc) {
          throw new EmptyReportException();
        }

        try {
          SMInputCursor errorCursor = rootCursor.childElementCursor("error"); // error
          while (errorCursor.getNext() != null) {
            String file = errorCursor.getAttrValue("file");
            String line = errorCursor.getAttrValue("line");
            String id = errorCursor.getAttrValue("id");
            String msg = errorCursor.getAttrValue("msg");

            if ("*".equals(file)) { // findings on project level
              file = null;
              line = null;
            }

            if (isInputValid(file, line, id, msg)) {
              sensor.saveUniqueViolation(context, CxxCppCheckRuleRepository.KEY, file, line, id, msg);
            } else {
              CxxUtils.LOG.warn("Skipping invalid violation: '{}'", msg);
            }
          }
        } catch (RuntimeException e) {
          throw new XMLStreamException();
        }
      }

      private boolean isInputValid(String file, String line, String id, String msg) {
        return id != null && !id.isEmpty() && msg != null && !msg.isEmpty();
      }
    });

    parser.parse(report);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
