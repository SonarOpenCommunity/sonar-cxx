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
package org.sonar.cxx.squidbridge.api; // cxx: in use

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.cxx.squidbridge.measures.Measurable;
import org.sonar.cxx.squidbridge.measures.Measures;
import org.sonar.cxx.squidbridge.measures.MetricDef;

public abstract class SourceCode implements Measurable, Comparable<SourceCode> {

  private final String name;
  private final Measures measures = new Measures();
  private final String key;
  private int startAtLine = -1;
  private int endAtLine = -1;
  private SourceCode parent;
  private SortedSet<SourceCode> children;
  private SourceCodeIndexer indexer;
  private Set<CheckMessage> messages;

  protected SourceCode(String key) {
    this(key, null);
  }

  protected SourceCode(String key, @Nullable String name) {
    this.key = key;
    this.name = name;
  }

  public String getKey() {
    return key;
  }

  @Override
  public int compareTo(SourceCode resource) {
    return key.compareTo(resource.getKey());
  }

  public String getName() {
    return name;
  }

  public final void setSourceCodeIndexer(SourceCodeIndexer indexer) {
    this.indexer = indexer;
  }

  private void index(SourceCode sourceCode) {
    if (indexer != null) {
      indexer.index(sourceCode);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }

    var other = (SourceCode) obj;
    return key.equals(other.key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public String toString() {
    return getKey();
  }

  public boolean isType(Class<? extends SourceCode> resourceType) {
    return this.getClass() == resourceType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getInt(MetricDef metric) {
    return (int) getMeasure(metric);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getDouble(MetricDef metric) {
    return getMeasure(metric);
  }

  public void add(MetricDef metric, SourceCode child) {
    add(metric, child.getMeasure(metric));
  }

  public void add(MetricDef metric, double value) {
    setMeasure(metric, getMeasure(metric) + value);
  }

  public void addData(MetricDef metric, Object data) {
    measures.setData(metric, data);
  }

  public Object getData(MetricDef metric) {
    return measures.getData(metric);
  }

  private double getMeasure(MetricDef metric) {
    if (metric.isCalculatedMetric()) {
      return metric.getCalculatedMetricFormula().calculate(this);
    }
    return measures.getValue(metric);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setMeasure(MetricDef metric, double measure) {
    if (metric.isCalculatedMetric()) {
      throw new IllegalStateException("It's not allowed to set the value of a calculated metric : " + metric.getName());
    }
    measures.setValue(metric, measure);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setMeasure(MetricDef metric, int measure) {
    setMeasure(metric, (double) measure);
  }

  public void removeMeasure(MetricDef metric) {
    measures.removeMeasure(metric);
  }

  public void setStartAtLine(int startAtLine) {
    this.startAtLine = startAtLine;
    this.endAtLine = startAtLine;
  }

  public void setEndAtLine(int endAtLine) {
    this.endAtLine = endAtLine;
  }

  public int getStartAtLine() {
    return startAtLine;
  }

  public int getEndAtLine() {
    return endAtLine;
  }

  public SourceCode addChild(SourceCode sourceCode) {
    if (children == null) {
      children = new TreeSet<>();
    }
    sourceCode.setParent(this);
    if (!children.contains(sourceCode)) {
      children.add(sourceCode);
      index(sourceCode);
    }
    return this;
  }

  @CheckForNull
  public <S extends SourceCode> S getParent(Class<S> sourceCode) {
    if (parent == null) {
      return null;
    }
    if (parent.getClass().equals(sourceCode)) {
      return (S) parent;
    }
    return parent.getParent(sourceCode);
  }

  public <S extends SourceCode> S getAncestor(Class<S> withClass) {
    var ancestor = getParent(withClass);
    if (ancestor != null) {
      var parentAncestor = ancestor.getAncestor(withClass);
      if (parentAncestor != null) {
        ancestor = parentAncestor;
      }
    }
    return ancestor;
  }

  public void log(CheckMessage message) {
    message.setSourceCode(this);
    getCheckMessages().add(message);
  }

  public Set<CheckMessage> getCheckMessages() {
    if (messages == null) {
      messages = new HashSet<>();
    }
    return messages;
  }

  public boolean hasCheckMessages() {
    return messages != null && !messages.isEmpty();
  }

  public SourceCode getFirstChild() {
    return !children.isEmpty() ? children.first() : null;
  }

  public SourceCode getLastChild() {
    return !children.isEmpty() ? children.last() : null;
  }

  private void setParent(SourceCode parent) {
    this.parent = parent;
  }

  public SourceCode getParent() {
    return parent;
  }

  public Set<SourceCode> getChildren() {
    return children;
  }

  public boolean hasChild(SourceCode squidUnit) {
    if (!hasChildren()) {
      return false;
    }
    if (children.contains(squidUnit)) {
      return true;
    }
    for (var child : children) {
      if (child.hasChild(squidUnit)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasChildren() {
    return children != null && !children.isEmpty();
  }

  public boolean hasAmongParents(SourceCode expectedParent) {
    if (parent == null) {
      return false;
    }
    return parent.equals(expectedParent) || parent.hasAmongParents(expectedParent);
  }
}
