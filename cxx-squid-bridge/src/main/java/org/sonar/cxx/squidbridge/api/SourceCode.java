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
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.cxx.squidbridge.measures.Measurable;
import org.sonar.cxx.squidbridge.measures.Measures;
import org.sonar.cxx.squidbridge.measures.MetricDef;

/**
 * During the scan process, a tree is created with all the artifacts read in. The base class is SourceCode, from which
 * more specific node types are derived. SourceProject is typically the root node, under which the other artifacts read
 * in are then attached.
 *
 * Each SourceCode object is indexed and can store further information such as measures, messages and the position in
 * the source file. After scanning, the information can then be read out and transferred to SonarQube.
 *
 * Typical structure of the tree:<br>
 * <pre>
 * SourceProject
 * |- 0..n SourceFile
 * |  |- 0..n SourceClass
 * |     |- 0..n SourceFunction
 * |- 0..n SourceFunction
 * </pre>
 *
 * @see SourceProject
 * @see SourceFile
 * @see SourceClass
 * @see SourceFunction
 */
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

  protected SourceCode(String key, @Nullable String name) {
    this.key = key;
    this.name = name;
  }

  protected SourceCode(@Nullable SourceCode parentKey, String key, @Nullable String name, int startAtLine) {
    var sb = new StringBuilder();
    if (parentKey != null) {
      sb.append(parentKey.getKey()).append('@');
    }
    sb.append(key).append(':').append(startAtLine);
    this.key = sb.toString();
    this.name = name;
    setStartAtLine(startAtLine);
  }

  /**
   * Get the key of the SourceCode object.
   *
   * The key must be unique within the SourceCode and is used by {@link #compareTo(SourceCode)} to compare two
   * SourceCode objects.
   *
   * @return key of the SourceCode object
   */
  public String getKey() {
    return key;
  }

  /**
   * Compare two SourceCode objects. They are equal if the two keys are equal.
   *
   * @param resource the {@code SourceCode} to be compared
   * @return the value {@code 0} if the argument SourceCode key is equal to this SourceCode key; a value less than
   * {@code 0} if this SourceCode key is lexicographically less than the argument SourceCode key; and a value greater
   * than {@code 0} if this SourceCode key is lexicographically greater than the argument SourceCode key.
   */
  @Override
  public int compareTo(SourceCode resource) {
    return key.compareTo(resource.getKey());
  }

  /**
   * Get the name of the SourceCode object.
   *
   * @return name of the SourceCode object
   */
  public String getName() {
    return name;
  }

  /**
   * Define the source code indexer to use.
   *
   * @param indexer source code indexer to use
   *
   * @see SourceCodeIndexer
   */
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
    return Objects.hash(key);
  }

  @Override
  public String toString() {
    return getKey();
  }

  /**
   * Compare resource type.
   *
   * @param resourceType to compare with
   * @return true, if same resource type
   */
  public boolean isType(Class<? extends SourceCode> resourceType) {
    return this.getClass() == resourceType;
  }

  /**
   * Get metric value as integer.
   *
   * @param metric to read value
   * @return metric value as int
   */
  @Override
  public int getInt(MetricDef metric) {
    return (int) getMeasure(metric);
  }

  /**
   * Get metric value as double.
   *
   * @param metric to read value
   * @return metric value as double
   */
  @Override
  public double getDouble(MetricDef metric) {
    return getMeasure(metric);
  }

  /**
   * Copy a metric value from a child to this SourceCode object.
   *
   * @param metric value to copy
   * @param child to copy value from
   */
  public void add(MetricDef metric, SourceCode child) {
    add(metric, child.getMeasure(metric));
  }

  /**
   * Add a new metric value to this SourceCode object.
   *
   * @param metric to add a value
   * @param value to add
   */
  public void add(MetricDef metric, double value) {
    setMeasure(metric, getMeasure(metric) + value);
  }

  /**
   * Add a new metric value to this SourceCode object.
   *
   * @param metric to add an object
   * @param data to add
   */
  public void addData(MetricDef metric, Object data) {
    measures.setData(metric, data);
  }

  /**
   * Get the value from a metric of this SourceCode object.
   *
   * @param metric to read
   * @return value of the metric as Object
   */
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
   * Add a new metric value to this SourceCode object.
   *
   * @param metric to add value
   * @param measure value to add
   */
  @Override
  public void setMeasure(MetricDef metric, double measure) {
    if (metric.isCalculatedMetric()) {
      throw new IllegalStateException("It's not allowed to set the value of a calculated metric : " + metric.getName());
    }
    measures.setValue(metric, measure);
  }

  /**
   * Add a new metric value to this SourceCode object.
   *
   * @param metric to add value
   * @param measure value to add
   */
  @Override
  public void setMeasure(MetricDef metric, int measure) {
    setMeasure(metric, (double) measure);
  }

  /**
   * Remove a metric from this SourceCode object.
   *
   * @param metric to remove
   */
  public void removeMeasure(MetricDef metric) {
    measures.removeMeasure(metric);
  }

  /**
   * Set start position of this SourceCode object.
   *
   * @param startAtLine line the SourceCode object starts
   */
  public void setStartAtLine(int startAtLine) {
    this.startAtLine = startAtLine;
    this.endAtLine = startAtLine;
  }

  /**
   * Set end position of this SourceCode object.
   *
   * @param endAtLine line the SourceCode object ends
   */
  public void setEndAtLine(int endAtLine) {
    this.endAtLine = endAtLine;
  }

  /**
   * Get start position of this SourceCode object.
   *
   * @return line this SourceCode object begins
   */
  public int getStartAtLine() {
    return startAtLine;
  }

  /**
   * Get end position of this SourceCode object.
   *
   * @return line this SourceCode object ends
   */
  public int getEndAtLine() {
    return endAtLine;
  }

  /**
   * Add a child to this SourceCode object and index it.
   *
   * @param sourceCode object to add
   * @return this SourceCode object
   */
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

  /**
   * Search for a parent with the defined SourceCode class in the tree.
   *
   * @param <S> class to search for
   * @param sourceCode class object
   * @return parent with the defined SourceCode class or null
   */
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

  /**
   * Search for an ancestor with the defined SourceCode class in the tree.
   *
   * @param <S> class to search for
   * @param withClass class object
   * @return ancestor with given type or null
   */
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

  /**
   * Add a message to this SourceCode object.
   *
   * @param message to add
   *
   * @see CheckMessage
   */
  public void log(CheckMessage message) {
    message.setSourceCode(this);
    getCheckMessages().add(message);
  }

  /**
   * Get all messages assigned to this SourceCode object.
   *
   * @return collection of messages assigned to this SourceCode object
   */
  public Set<CheckMessage> getCheckMessages() {
    if (messages == null) {
      messages = new HashSet<>();
    }
    return messages;
  }

  /**
   * Check if messages assigned to this SourceCode object.
   *
   * @return true if there is at least one message assigned to this SourceCode object
   */
  public boolean hasCheckMessages() {
    return messages != null && !messages.isEmpty();
  }

  /**
   * Get first child of this SourceCode object.
   *
   * @return first child or null if no child exists
   */
  public SourceCode getFirstChild() {
    return !children.isEmpty() ? children.first() : null;
  }

  /**
   * Get last child of this SourceCode object.
   *
   * @return last child or null if no child exists
   */
  public SourceCode getLastChild() {
    return !children.isEmpty() ? children.last() : null;
  }

  private void setParent(SourceCode parent) {
    this.parent = parent;
  }

  /**
   * Get direct parent of this SourceCode object.
   *
   * @return parent SourceCode object
   */
  public SourceCode getParent() {
    return parent;
  }

  /**
   * Get direct children of this SourceCode object.
   *
   * @return collection of SourceCode objects who are a direct child of this one.
   */
  public Set<SourceCode> getChildren() {
    return children;
  }

  /**
   * Search for a child in the tree starting from this SourceCode object.
   *
   * @param squidUnit child to serach for
   * @return true if squidUnit is a child of this SourceCode object
   */
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

  /**
   * Check if this SourceCode object has children.
   *
   * @return true if this SourceCode object has at least one child
   */
  public boolean hasChildren() {
    return children != null && !children.isEmpty();
  }

  /**
   * Search for a parent in the tree starting with this SourceCode object.
   *
   * @param expectedParent parent to search for
   * @return true if expectedParent is a parent of this SourceCode object
   */
  public boolean hasAmongParents(SourceCode expectedParent) {
    if (parent == null) {
      return false;
    }
    return parent.equals(expectedParent) || parent.hasAmongParents(expectedParent);
  }
}
