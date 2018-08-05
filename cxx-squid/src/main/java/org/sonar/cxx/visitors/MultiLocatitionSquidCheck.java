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
package org.sonar.cxx.visitors;

import java.util.HashSet;
import java.util.Set;

import org.sonar.api.utils.AnnotationUtils;
import org.sonar.cxx.utils.CxxReportIssue;
import org.sonar.squidbridge.SquidAstVisitorContext;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.squidbridge.measures.CalculatedMetricFormula;
import org.sonar.squidbridge.measures.MetricDef;

import com.sonar.sslr.api.Grammar;

/**
 * Derivation of {@link SquidCheck}, which can create issues with multiple
 * locations (1 primary location, arbitrary number of secondary locations
 *
 * See also org.sonar.squidbridge.SquidAstVisitorContext.createLineViolation
 *
 * @param <G>
 */
public class MultiLocatitionSquidCheck<G extends Grammar> extends SquidCheck<G> {

  private static enum DataKey implements MetricDef {
    FILE_VIOLATIONS_WITH_MULTIPLE_LOCATIONS;

    @Override
    public String getName() {
      return FILE_VIOLATIONS_WITH_MULTIPLE_LOCATIONS.getName();
    }

    @Override
    public boolean isCalculatedMetric() {
      return false;
    }

    @Override
    public boolean aggregateIfThereIsAlreadyAValue() {
      return false;
    }

    @Override
    public boolean isThereAggregationFormula() {
      return false;
    }

    @Override
    public CalculatedMetricFormula getCalculatedMetricFormula() {
      return null;
    }
  }

  /**
   * @return the rule key of this check visitor
   * @see org.sonar.check.Rule
   */
  protected String getRuleKey() {
    org.sonar.check.Rule ruleAnnotation = AnnotationUtils.getAnnotation(this, org.sonar.check.Rule.class);
    if (ruleAnnotation != null && ruleAnnotation.key() != null) {
      return ruleAnnotation.key();
    }
    throw new IllegalStateException("Check must be annotated with @Rule( key = <key> )");
  }

  private SourceFile getSourceFile() {
    final SquidAstVisitorContext<G> c = getContext();
    if (c.peekSourceCode() instanceof SourceFile) {
      return (SourceFile) c.peekSourceCode();
    } else if (c.peekSourceCode().getParent(SourceFile.class) != null) {
      return c.peekSourceCode().getParent(SourceFile.class);
    } else {
      throw new IllegalStateException("Unable to get SourceFile on source code '"
          + (c.peekSourceCode() == null ? "[NULL]" : c.peekSourceCode().getKey()) + "'");
    }
  }

  /**
   * Add the given message to the current SourceFile object
   * @see SquidAstVisitorContext<G extends Grammar>.createLineViolation() for simple violations
   */
  protected void createMultiLocationViolation(CxxReportIssue message) {
    SourceFile sourceFile = getSourceFile();
    Set<CxxReportIssue> messages = getMultiLocationCheckMessages(sourceFile);
    if (messages == null) {
      messages = new HashSet<>();
    }
    messages.add(message);
    setMultiLocationViolation(sourceFile, messages);
  }

  /**
   * @return set of multi-location issues, raised on the given file; might be
   *         <code>null</code>
   * @see SourceFile.getCheckMessages() for simple violations
   */
  @SuppressWarnings("unchecked")
  public static Set<CxxReportIssue> getMultiLocationCheckMessages(SourceFile sourceFile) {
    return (Set<CxxReportIssue>) sourceFile.getData(DataKey.FILE_VIOLATIONS_WITH_MULTIPLE_LOCATIONS);
  }

  /**
   * @return true if the given file has mult-location issues
   * @see SourceFile.hasCheckMessages() for simple violations
   */
  public static boolean hasMultiLocationCheckMessages(SourceFile sourceFile) {
    Set<CxxReportIssue> issues = getMultiLocationCheckMessages(sourceFile);
    return issues != null && !issues.isEmpty();
  }

  private static void setMultiLocationViolation(SourceFile sourceFile, Set<CxxReportIssue> messages) {
    sourceFile.addData(DataKey.FILE_VIOLATIONS_WITH_MULTIPLE_LOCATIONS, messages);
  }

  public static void eraseMultilineCheckMessages(SourceFile sourceFile) {
    setMultiLocationViolation(sourceFile, null);
  }
}
