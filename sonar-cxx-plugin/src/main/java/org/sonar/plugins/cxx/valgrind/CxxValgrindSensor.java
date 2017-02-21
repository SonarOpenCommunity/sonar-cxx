/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.sonar.plugins.cxx.valgrind;

import java.io.File;
import java.util.Set;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;

/**
 * {@inheritDoc}
 */
public class CxxValgrindSensor extends CxxReportSensor {
  public static final Logger LOG = Loggers.get(CxxValgrindSensor.class);
  public static final String REPORT_PATH_KEY = "sonar.cxx.valgrind.reportPath";

  /**
   * {@inheritDoc}
   */
  public CxxValgrindSensor(Settings settings) {
    super(settings, CxxMetrics.VALGRIND);
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    LOG.debug("Parsing 'Valgrind' format");
    ValgrindReportParser parser = new ValgrindReportParser();
    saveErrors(context, parser.processReport(context, report));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(CxxLanguage.KEY).name("CxxValgrindSensor");
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
}
