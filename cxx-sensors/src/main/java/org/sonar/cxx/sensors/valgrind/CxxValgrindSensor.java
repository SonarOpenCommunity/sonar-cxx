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
package org.sonar.cxx.sensors.valgrind;

import java.io.File;
import java.util.Set;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.CxxReportSensor;

/**
 * {@inheritDoc}
 */
public class CxxValgrindSensor extends CxxReportSensor {

  private static final Logger LOG = Loggers.get(CxxValgrindSensor.class);
  public static final String REPORT_PATH_KEY = "valgrind.reportPath";
  public static final String KEY = "Valgrind";

  /**
   * CxxValgrindSensor for Valgrind Sensor
   *
   * @param language defines settings C or C++
   */
  public CxxValgrindSensor(CxxLanguage language) {
    super(language);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(language.getName() + " ValgrindSensor")
      .onlyOnLanguage(this.language.getKey())
      .createIssuesForRuleRepository(CxxValgrindRuleRepository.KEY)
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  public String getReportPathKey() {
    return this.language.getPluginProperty(REPORT_PATH_KEY);
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    LOG.debug("Parsing 'Valgrind' format");
    ValgrindReportParser parser = new ValgrindReportParser();
    saveErrors(context, parser.processReport(report));
  }

  void saveErrors(SensorContext context, Set<ValgrindError> valgrindErrors) {
    for (ValgrindError error : valgrindErrors) {
      ValgrindFrame frame = error.getLastOwnFrame(context.fileSystem().baseDir().getPath());
      if (frame != null) {
        saveUniqueViolation(context, CxxValgrindRuleRepository.KEY,
          frame.getPath(), frame.getLine(), error.getKind(), error.toString());
      } else {
        LOG.warn("Cannot find a project file to assign the valgrind error '{}' to", error);
      }
    }
  }

  @Override
  protected String getSensorKey() {
    return KEY;
  }
}
