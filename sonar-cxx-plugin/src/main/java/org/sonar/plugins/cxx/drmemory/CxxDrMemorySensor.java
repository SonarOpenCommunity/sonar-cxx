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
package org.sonar.plugins.cxx.drmemory;

import java.io.File;
import java.io.IOException;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.drmemory.DrMemoryParser.DrMemoryError;
import org.sonar.plugins.cxx.drmemory.DrMemoryParser.DrMemoryError.Location;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * Dr. Memory is a memory monitoring tool capable of identifying memory-related
 * programming errors such as accesses of uninitialized memory, accesses to
 * unaddressable memory (including outside of allocated heap units and heap
 * underflow and overflow), accesses to freed memory, double frees, memory
 * leaks, and (on Windows) handle leaks, GDI API usage errors, and accesses to
 * un-reserved thread local storage slots. See also: http://drmemory.org
 *
 * @author asylvestre
 */
public class CxxDrMemorySensor extends CxxReportSensor {
  public static final Logger LOG = Loggers.get(CxxDrMemorySensor.class);
  public static final String REPORT_PATH_KEY = "sonar.cxx.drmemory.reportPath";

  /**
   * {@inheritDoc}
  */
  public CxxDrMemorySensor(Settings settings) {
    super(settings, CxxMetrics.DRMEMORY);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(CxxLanguage.KEY).name("CxxDrMemorySensor");
  }
  
  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected void processReport(final SensorContext context, File report) {
    LOG.debug("Parsing 'Dr Memory' format");

    try {
      for (DrMemoryError error : DrMemoryParser.parse(report)) {
        if (error.stackTrace.isEmpty()) {
          saveUniqueViolation(context, CxxDrMemoryRuleRepository.KEY,
                  null, null,
                  error.type.getId(), error.message);
        }
        for (Location errorLocation : error.stackTrace) {
          if (isFileInAnalysis(context, errorLocation)) {
            saveUniqueViolation(context, CxxDrMemoryRuleRepository.KEY,
                    errorLocation.file,	errorLocation.line.toString(),
                    error.type.getId(), error.message);
            break;
          }
        }
      }
    } catch (IOException e) {
      String msg = new StringBuilder()
        .append("Cannot feed the data into sonar, details: '")
        .append(e)
        .append("'")
        .toString();
      LOG.error(msg);
      CxxUtils.validateRecovery(e, settings);
    }
  }

  private boolean isFileInAnalysis(SensorContext context, Location errorLocation) {
    String root = context.fileSystem().baseDir().getAbsolutePath();
    String normalPath = CxxUtils.normalizePathFull(errorLocation.file, root);
    InputFile inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().is(new File(normalPath)));
    return inputFile != null;
  }
}
