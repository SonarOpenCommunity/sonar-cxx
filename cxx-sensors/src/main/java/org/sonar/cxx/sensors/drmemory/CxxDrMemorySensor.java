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
package org.sonar.cxx.sensors.drmemory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.drmemory.DrMemoryParser.DrMemoryError;
import org.sonar.cxx.sensors.drmemory.DrMemoryParser.DrMemoryError.Location;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.CxxUtils;

/**
 * Dr. Memory is a memory monitoring tool capable of identifying memory-related
 * programming errors such as accesses of uninitialized memory, accesses to
 * un-addressable memory (including outside of allocated heap units and heap
 * underflow and overflow), accesses to freed memory, double frees, memory
 * leaks, and (on Windows) handle leaks, GDI API usage errors, and accesses to
 * un-reserved thread local storage slots. See also: http://drmemory.org
 *
 * @author asylvestre
 */
public class CxxDrMemorySensor extends CxxReportSensor {
  private static final Logger LOG = Loggers.get(CxxDrMemorySensor.class);
  public static final String REPORT_PATH_KEY = "drmemory.reportPath";
  public static final String KEY = "DrMemory";
  public static final String DEFAULT_CHARSET_DEF = StandardCharsets.UTF_8.name();

  /**
   * {@inheritDoc}
  */
  public CxxDrMemorySensor(CxxLanguage language, Settings settings) {
    super(language, settings);
  }

  /**
   * {@inheritDoc}
  */
  public String defaultCharset() {
    return DEFAULT_CHARSET_DEF;
  }
  
  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(this.language.getKey()).name(language.getName() + " DrMemorySensor");
  }

  @Override
  public String getReportPathKey() {
    return this.language.getPluginProperty(REPORT_PATH_KEY);
  }

  @Override
  protected void processReport(final SensorContext context, File report) {
    LOG.debug("Parsing 'Dr Memory' format");

    for (DrMemoryError error : DrMemoryParser.parse(report, defaultCharset())) {
      if (error.getStackTrace().isEmpty()) {
        saveUniqueViolation(context, CxxDrMemoryRuleRepository.KEY,
                null, null,
                error.type.getId(), error.getMessage());
      }
      for (Location errorLocation : error.getStackTrace()) {
        if (isFileInAnalysis(context, errorLocation)) {
          saveUniqueViolation(context, CxxDrMemoryRuleRepository.KEY,
                  errorLocation.getFile(), errorLocation.getLine().toString(),
                  error.type.getId(), error.getMessage());
          break;
        }
      }
    }
  }

  private boolean isFileInAnalysis(SensorContext context, Location errorLocation) {
    String root = context.fileSystem().baseDir().getAbsolutePath();
    String normalPath = CxxUtils.normalizePathFull(errorLocation.getFile(), root);
    InputFile inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().is(new File(normalPath)));
    return inputFile != null;
  }
  
  @Override
  protected String getSensorKey() {
    return KEY;
  }  
}
