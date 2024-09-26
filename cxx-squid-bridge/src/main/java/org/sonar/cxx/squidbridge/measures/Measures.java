/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2024 SonarOpenCommunity
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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.measures;

import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.CheckForNull;

public class Measures {

  private final Map<MetricDef, Measure> measures = new IdentityHashMap<>();

  public double getValue(MetricDef metric) {
    var measure = measures.get(metric);
    if (measure == null) {
      return 0;
    }
    return measure.getValue();
  }

  @CheckForNull
  public Object getData(MetricDef metric) {
    var measure = measures.get(metric);
    if (measure == null) {
      return null;
    }
    return measure.getData();
  }

  public void setValue(MetricDef metric, double measure) {
    getMeasureOrCreateIt(metric).setValue(measure);
  }

  public void setData(MetricDef metric, Object data) {
    getMeasureOrCreateIt(metric).setData(data);
  }

  private Measure getMeasureOrCreateIt(MetricDef metric) {
    var measure = measures.get(metric);
    if (measure == null) {
      measure = new Measure(0);
      measures.put(metric, measure);
    }
    return measure;
  }

  public void removeMeasure(MetricDef metric) {
    measures.remove(metric);
  }

  private static final class Measure {

    private double value;
    private Object data;

    private Measure(double value) {
      this.value = value;
    }

    private double getValue() {
      return value;
    }

    private void setValue(double value) {
      this.value = value;
    }

    private Object getData() {
      return data;
    }

    private void setData(Object data) {
      this.data = data;
    }
  }

}
