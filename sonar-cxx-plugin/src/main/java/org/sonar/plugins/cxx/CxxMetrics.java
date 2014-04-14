/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.ArrayList;
import java.util.List;

public class CxxMetrics implements Metrics {

  public static final Metric COMPILER = new Metric.Builder("CXX-COMPILER", "C++ compiler Warnings", Metric.ValueType.INT)
        .setDirection(Metric.DIRECTION_WORST)
        .setQualitative(true)
        .setDomain("C++")
        .create();
  public static final Metric CPPCHECK = new Metric.Builder("CXX-CPPCHECK", "CppCheck Errors", Metric.ValueType.INT)
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(true)
      .setDomain("C++")
      .create();
  public static final Metric EXTERNAL = new Metric.Builder("CXX-EXTERNAL", "External C++ rules violations", Metric.ValueType.INT)
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(true)
      .setDomain("C++")
      .create();
  public static final Metric PCLINT = new Metric.Builder("CXX-PCLINT", "PC-Lint errors", Metric.ValueType.INT)
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(true)
      .setDomain("C++")
      .create();
  public static final Metric RATS = new Metric.Builder("CXX-RATS", "RATS issues", Metric.ValueType.INT)
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(true)
      .setDomain("C++")
      .create();
  public static final Metric SQUID = new Metric.Builder("CXX-SQUID", "C++ checks", Metric.ValueType.INT)
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(true)
      .setDomain("C++")
      .create();
  public static final Metric VALGRIND = new Metric.Builder("CXX-VALGRIND", "Valgrind errors", Metric.ValueType.INT)
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(true)
      .setDomain("C++")
      .create();
  public static final Metric VERAXX = new Metric.Builder("CXX-VERAXX", "Vera++ rule violations", Metric.ValueType.INT)
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(true)
      .setDomain("C++")
      .create();

  public List<Metric> getMetrics() {
    List<Metric> list = new ArrayList<Metric>();
    list.add(COMPILER);
    list.add(CPPCHECK);
    list.add(EXTERNAL);
    list.add(PCLINT);
    list.add(RATS);
    list.add(SQUID);
    list.add(VALGRIND);
    list.add(VERAXX);
    return list;
  }
}
