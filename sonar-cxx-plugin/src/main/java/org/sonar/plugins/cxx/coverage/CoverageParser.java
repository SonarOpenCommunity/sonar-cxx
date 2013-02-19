/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.coverage;

import java.io.File;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sonar.api.measures.CoverageMeasuresBuilder;

  /**
   * The interface a coverage report parser has to implement in order to be used
   * by CxxCoverageSensor
   */
public interface CoverageParser {
  /**
   * Parses the given report and stores the results in the according builder
   * @param xmlFile The report to parse
   * @param coverageData A Map mapping source file names to coverage measures. Has
   *        to be used to store the results into. 
   */
  void parseReport(File xmlFile, Map<String, CoverageMeasuresBuilder> coverageData)
    throws XMLStreamException;
}
