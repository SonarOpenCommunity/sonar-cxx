/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.checks;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultiset;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.api.CheckMessage;

public class ViolationCounterCheck<G extends Grammar> extends SquidAstVisitor<G> {

  private final ViolationCounter violationCounter;
  private final String projectsDirCanonicalPath;

  public static class ViolationCounter {

    private final Map<String, Map<String, TreeMultiset<Integer>>> violationsByFileAndRule;

    public ViolationCounter() {
      this.violationsByFileAndRule = new HashMap<String, Map<String, TreeMultiset<Integer>>>();
    }

    private ViolationCounter(Map<String, Map<String, TreeMultiset<Integer>>> violationsByFileAndRule) {
      this.violationsByFileAndRule = violationsByFileAndRule;
    }

    public void increment(String fileRelativePath, String rule, int line) {
      if (!violationsByFileAndRule.containsKey(fileRelativePath)) {
        violationsByFileAndRule.put(fileRelativePath, new HashMap<String, TreeMultiset<Integer>>());
      }
      Map<String, TreeMultiset<Integer>> violationsByRule = violationsByFileAndRule.get(fileRelativePath);

      if (!violationsByRule.containsKey(rule)) {
        violationsByRule.put(rule, TreeMultiset.<Integer>create());
      }
      TreeMultiset<Integer> violations = violationsByRule.get(rule);

      violations.add(line);
    }

    public void saveToFile(String destinationFilePath) {
      FileOutputStream fos = null;
      ObjectOutputStream oos = null;
      try {
        fos = new FileOutputStream(destinationFilePath);
        oos = new ObjectOutputStream(fos);
        oos.writeObject(this.violationsByFileAndRule);
      } catch (Exception e) {
        throw Throwables.propagate(e);
      } finally {
        IOUtils.closeQuietly(fos);
        IOUtils.closeQuietly(oos);
      }
    }

    public static ViolationCounter loadFromFile(File sourceFile) {
      if (!sourceFile.exists() || sourceFile.length() == 0) {
        return new ViolationCounter();
      } else {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
          fis = new FileInputStream(sourceFile);
          ois = new ObjectInputStream(fis);
          return new ViolationCounter((Map<String, Map<String, TreeMultiset<Integer>>>) ois.readObject());
        } catch (Exception e) {
          throw Throwables.propagate(e);
        } finally {
          IOUtils.closeQuietly(fis);
          IOUtils.closeQuietly(ois);
        }
      }
    }
  }

  public static class ViolationDifferenceAnalyzer {

    private final ViolationCounter expected;
    private final ViolationCounter actual;
    private boolean hasDifferences = false;

    public ViolationDifferenceAnalyzer(ViolationCounter expected, ViolationCounter actual) {
      this.expected = expected;
      this.actual = actual;
    }

    private static void println() {
      System.out.println();
    }

    private static void println(String msg) {
      System.out.println(msg);
    }

    public void printReport() {
      println();
      println();
      println("********************************");
      println("* Violation differences report *");
      println("********************************");
      println();
      println();
      printDifferencesByFile();
      println();
      println();
      printDifferencesByRule();
      println();
      println();
      println("*****************");
      println("* End of report *");
      println("*****************");
      println();
      println();
    }

    private void printDifferencesByFile() {
      println("Differences by file:");

      Set<Map.Entry<String, String>> handledFilesRules = Sets.newHashSet();

      for (var file : expected.violationsByFileAndRule.keySet()) {
        var shouldPrintHeader = true;
        for (var rule : expected.violationsByFileAndRule.get(file).keySet()) {
          handledFilesRules.add(Maps.immutableEntry(file, rule));
          shouldPrintHeader = printDifferencesByFileAndRule(shouldPrintHeader, file, rule);
        }
      }

      for (var file : actual.violationsByFileAndRule.keySet()) {
        var shouldPrintHeader = true;
        for (var rule : actual.violationsByFileAndRule.get(file).keySet()) {
          if (handledFilesRules.contains(Maps.immutableEntry(file, rule))) {
            continue;
          }
          shouldPrintHeader = printDifferencesByFileAndRule(shouldPrintHeader, file, rule);
        }
      }

      println("End of differences by file.");
    }

    private static void printDifferencesByFileHeader(String file) {
      println("  File " + file + ":");
    }

    private boolean printDifferencesByFileAndRule(boolean shouldPrintHeader, String file, String rule) {

      TreeMultiset<Integer> linesExpected = getLines(expected, file, rule);
      TreeMultiset<Integer> linesActual = getLines(actual, file, rule);

      if (!linesExpected.equals(linesActual)) {
        hasDifferences = true;

        if (shouldPrintHeader) {
          printDifferencesByFileHeader(file);
        }

        println("    " + rule + ", (difference only) expected ("
                  + StringUtils.join(setDifference(linesExpected, linesActual), ",") + "), actual ("
                  + StringUtils.join(setDifference(linesActual, linesExpected), ",") + ").");

        return false;
      } else {
        return shouldPrintHeader;
      }

    }

    private static TreeMultiset<Integer> getLines(ViolationCounter counter, String file, String rule) {
      if (!counter.violationsByFileAndRule.containsKey(file)
            || !counter.violationsByFileAndRule.get(file).containsKey(rule)) {
        return TreeMultiset.create();
      } else {
        return counter.violationsByFileAndRule.get(file).get(rule);
      }
    }

    private static TreeMultiset<Integer> setDifference(TreeMultiset<Integer> a, TreeMultiset<Integer> b) {
      TreeMultiset<Integer> aMinusB = TreeMultiset.create(a);
      aMinusB.removeAll(b);
      return aMinusB;
    }

    private void printDifferencesByRule() {
      println("Differences by rule:");

      for (var rule : getRules()) {
        int expectedViolations = getViolationsByRule(expected, rule);
        int actualViolations = getViolationsByRule(actual, rule);

        println("  " + rule + " expected: " + expectedViolations + ", actual: " + actualViolations + ": "
                  + (expectedViolations == actualViolations ? "OK" : "*** FAILURE ***"));
      }

      println("End of differences by rule.");
    }

    private Set<String> getRules() {
      Set<String> rules = new HashSet<String>();

      for (var file : expected.violationsByFileAndRule.keySet()) {
        rules.addAll(expected.violationsByFileAndRule.get(file).keySet());
      }

      for (var file : actual.violationsByFileAndRule.keySet()) {
        rules.addAll(actual.violationsByFileAndRule.get(file).keySet());
      }

      return rules;
    }

    private static int getViolationsByRule(ViolationCounter counter, String rule) {
      var violations = 0;

      for (var file : counter.violationsByFileAndRule.keySet()) {
        if (counter.violationsByFileAndRule.get(file).containsKey(rule)) {
          violations += counter.violationsByFileAndRule.get(file).get(rule).size();
        }
      }

      return violations;
    }

    public boolean hasDifferences() {
      return hasDifferences;
    }

  }

  public ViolationCounterCheck(String projectsDir, ViolationCounter violationCounter) {
    try {
      this.projectsDirCanonicalPath = new File(projectsDir).getCanonicalPath();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

    this.violationCounter = violationCounter;
  }

  @Override
  public void leaveFile(AstNode node) {
    Set<CheckMessage> violationsOnCurrentFile = new HashSet<CheckMessage>(getContext().peekSourceCode()
      .getCheckMessages());
    for (var violation : violationsOnCurrentFile) {
      violationCounter.increment(getRelativePath(getContext().getFile()), violation.getChecker().getClass()
                                 .getSimpleName(),
                                 violation.getLine() == null ? -1
                                   : violation.getLine());
    }
  }

  private String getRelativePath(File file) {
    if (!file.exists()) {
      throw new IllegalArgumentException("The file located at \"" + file.getAbsolutePath() + "\" does not exist.");
    }

    String canonicalPath;
    try {
      canonicalPath = file.getCanonicalPath();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

    if (!canonicalPath.startsWith(projectsDirCanonicalPath)) {
      throw new IllegalArgumentException("The file located at \"" + canonicalPath + "\" is not within projectsDir (\""
                                           + projectsDirCanonicalPath + "\").");
    }

    return canonicalPath.substring(projectsDirCanonicalPath.length()).replace('\\', '/');
  }

}
