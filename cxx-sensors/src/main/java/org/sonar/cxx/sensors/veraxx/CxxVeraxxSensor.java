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
package org.sonar.cxx.sensors.veraxx;

import java.io.File;
import java.util.Optional;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.CxxReportIssue;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.StaxParser;

/**
 * {@inheritDoc}
 */
public class CxxVeraxxSensor extends CxxReportSensor {

  private static final Logger LOG = Loggers.get(CxxVeraxxSensor.class);
  public static final String REPORT_PATH_KEY = "vera.reportPath";
  public static final String KEY = "Vera++";

  /**
   * CxxVeraxxSensor for C++ Vera Sensor
   *
   * @param language defines settings C or C++
   */
  public CxxVeraxxSensor(CxxLanguage language) {
    super(language);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(language.getName() + " VeraxxSensor")
      .onlyOnLanguage(this.language.getKey())
      .createIssuesForRuleRepository(CxxVeraxxRuleRepository.KEY)
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  public String getReportPathKey() {
    return this.language.getPluginProperty(REPORT_PATH_KEY);
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    LOG.debug("Parsing 'Vera++' format");
    try {
      StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void stream(SMHierarchicCursor rootCursor) throws javax.xml.stream.XMLStreamException {
          try {
            rootCursor.advance();
          } catch (com.ctc.wstx.exc.WstxEOFException eofExc) {
            throw new EmptyReportException("Cannot read vera++ report ", eofExc);
          }

          SMInputCursor fileCursor = rootCursor.childElementCursor("file");
          while (fileCursor.getNext() != null) {
            String name = fileCursor.getAttrValue("name");

            SMInputCursor errorCursor = fileCursor.childElementCursor("error");
            while (errorCursor.getNext() != null) {
              if (!"error".equals(name)) {
                String line = errorCursor.getAttrValue("line");
                String message = errorCursor.getAttrValue("message");
                String source = errorCursor.getAttrValue("source");

                CxxReportIssue issue = new CxxReportIssue(CxxVeraxxRuleRepository.KEY, source, name, line, message);
                saveUniqueViolation(context, issue);
              } else {
                if (LOG.isDebugEnabled()) {
                  LOG.debug("Error in file '{}', with message '{}'",
                    name + "(" + errorCursor.getAttrValue("line") + ")",
                    errorCursor.getAttrValue("message"));
                }
              }
            }
          }
        }
      });

      parser.parse(report);
    } catch (com.ctc.wstx.exc.WstxUnexpectedCharException e) {
      LOG.error("Ignore XML error from Veraxx '{}'", CxxUtils.getStackTrace(e));
    }
  }

  @Override
  protected String getSensorKey() {
    return KEY;
  }

  @Override
  protected Optional<CxxMetricsFactory.Key> getMetricKey() {
    return Optional.of(CxxMetricsFactory.Key.VERAXX_SENSOR_KEY);
  }
}
