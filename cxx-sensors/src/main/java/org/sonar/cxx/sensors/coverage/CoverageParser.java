/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx.sensors.coverage;

import java.io.File;
import java.util.Map;
import org.sonar.cxx.sensors.utils.ReportException;

/**
 * The interface a coverage report parser has to implement in order to be used by CxxCoverageSensor
 */
public interface CoverageParser {

  /**
   * Parses the given report and stores the results in the according builder
   *
   * @param report with coverage data
   * @return coverageData A Map mapping source file names to coverage measures. Has to be used to store the results
   * into. Source file names might be relative. In such case they will be resolved against the base directory of
   * SonarQube project.<br>
   *
   * @throws ReportException EmptyReportException or InvalidReportException
   */
  Map<String, CoverageMeasures> parse(File report);

}
