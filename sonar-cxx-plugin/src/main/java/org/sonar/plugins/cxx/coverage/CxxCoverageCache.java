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
package org.sonar.plugins.cxx.coverage;

import java.util.HashMap;
import java.util.Map;
import org.sonar.api.BatchExtension; //@todo deprecated
import org.sonar.api.measures.CoverageMeasuresBuilder; //@todo deprecated

public class CxxCoverageCache implements BatchExtension {

  private final static Map<String, Map<String, CoverageMeasuresBuilder>> cacheUnit = new HashMap<>(); //@todo deprecated CoverageMeasuresBuilder
  private final static Map<String, Map<String, CoverageMeasuresBuilder>> cacheIntegration = new HashMap<>(); //@todo deprecated CoverageMeasuresBuilder
  private final static Map<String, Map<String, CoverageMeasuresBuilder>> cacheOverall = new HashMap<>(); //@todo deprecated CoverageMeasuresBuilder
  
  public CxxCoverageCache() {
  }

  public Map<String, Map<String, CoverageMeasuresBuilder>> unitCoverageCache() { //@todo deprecated CoverageMeasuresBuilder
    return cacheUnit;
  }

  public Map<String, Map<String, CoverageMeasuresBuilder>> integrationCoverageCache() { //@todo deprecated CoverageMeasuresBuilder
    return cacheIntegration;
  }

  public Map<String, Map<String, CoverageMeasuresBuilder>> overallCoverageCache() { //@todo deprecated CoverageMeasuresBuilder
    return cacheOverall;
  }
}
