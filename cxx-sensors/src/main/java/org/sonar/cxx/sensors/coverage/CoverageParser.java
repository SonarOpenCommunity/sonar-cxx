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
package org.sonar.cxx.sensors.coverage;

import java.io.File;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

/**
 * The interface a coverage report parser has to implement in order to be used
 * by CxxCoverageSensor
 */
public interface CoverageParser {

  /**
   * Parses the given report and stores the results in the according builder
   *
   * @param context
   *          of sensor
   * @param report
   *          with coverage data
   * @param coverageData
   *          A Map mapping source file names to coverage measures. Has to be
   *          used to store the results into. Source file names might be
   *          relative. In such case they will be resolved against the base
   *          directory of SonarQube module/project.<br>
   *
   *          <b>ATTENTION!</b> This map is shared between modules in
   *          multi-module projects. Don't try to resolve paths against some
   *          specific module!
   * @throws XMLStreamException
   *           javax.xml.stream.XMLStreamException
   */
  void processReport(File report, Map<String, CoverageMeasures> coverageData) throws XMLStreamException;
}
