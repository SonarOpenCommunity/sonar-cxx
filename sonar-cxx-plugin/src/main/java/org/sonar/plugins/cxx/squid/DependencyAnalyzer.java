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
package org.sonar.plugins.cxx.squid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.cxx.checks.CycleBetweenPackagesCheck;
import org.sonar.cxx.checks.DuplicatedIncludeCheck;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.graph.Cycle;
import org.sonar.graph.DirectedGraph;
import org.sonar.graph.Dsm;
import org.sonar.graph.DsmTopologicalSorter;
import org.sonar.graph.Edge;
import org.sonar.graph.IncrementalCyclesAndFESSolver;
import org.sonar.graph.MinimumFeedbackEdgeSetSolver;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputDir;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;

public class DependencyAnalyzer {

  private final SensorContext context;
  private int violationsCount;
  private final ActiveRule duplicateIncludeRule;
  private final ActiveRule cycleBetweenPackagesRule;

  private final DirectedGraph<InputFile, FileEdge> filesGraph = new DirectedGraph<>();
  private final DirectedGraph<InputDir, DirectoryEdge> packagesGraph = new DirectedGraph<>();
  private final Multimap<InputDir, InputFile> directoryFiles = HashMultimap.create();
  private final FileSystem fs;

  public DependencyAnalyzer(SensorContext context, ActiveRules rules) {
    this.context = context;
    this.fs = context.fileSystem();
    this.violationsCount = 0;
    this.duplicateIncludeRule = DuplicatedIncludeCheck.getActiveRule(rules);
    this.cycleBetweenPackagesRule = CycleBetweenPackagesCheck.getActiveRule(rules);
  }

  public void addFile(InputFile sonarFile, Collection<CxxPreprocessor.Include> includedFiles, SensorContext sensorContext) {
    //Store the directory and file
    InputDir sonarDir = this.fs.inputDir(sonarFile.path().getParent().toFile());
    packagesGraph.addVertex(sonarDir);
    directoryFiles.put(sonarDir, sonarFile);

    //Build the dependency graph
    Map<String, Integer> firstIncludeLine = new HashMap<>();
    for (CxxPreprocessor.Include include : includedFiles) {
      InputFile includedFile = fs.inputFile(fs.predicates().hasPath(include.getPath()));
      String includedFilePath = includedFile != null ? includedFile.absolutePath() : include.getPath();
      Integer prevIncludeLine = firstIncludeLine.put(includedFilePath, include.getLine());
      if (prevIncludeLine != null && duplicateIncludeRule != null) {        
        try
        {
            NewIssue newIssue = sensorContext.newIssue().forRule(duplicateIncludeRule.ruleKey());
            NewIssueLocation location = newIssue.newLocation()
              .on(sonarFile)
              .at(sonarFile.selectLine(include.getLine() > 0 ? include.getLine() : 1))
              .message("Remove duplicated include, \"" + includedFilePath + "\" is already included at line " + prevIncludeLine + ".");

            newIssue.at(location);
            newIssue.save();

            violationsCount++;
          
        } catch (Exception ex) {
            CxxUtils.LOG.debug("Rule {}", duplicateIncludeRule);
            CxxUtils.LOG.debug("Sensor {}", sensorContext);
            CxxUtils.LOG.debug("Include {}", include);
            CxxUtils.LOG.debug(ex.getMessage());
            throw ex;
        }
      } else if (includedFile == null) {
        // dont warn about missing files
      } else if (context.fileSystem().hasFiles(fs.predicates().hasPath(sonarFile.absolutePath()))) {
        //Add the dependency in the files graph
        FileEdge fileEdge = new FileEdge(sonarFile, includedFile, include.getLine());

        if (!filesGraph.hasEdge(sonarFile, includedFile)) {
          filesGraph.addEdge(fileEdge);
        }        

        //Add the dependency in the packages graph, if the directories are different
        InputDir includedDir = this.fs.inputDir(includedFile.path().getParent().toFile());
        if (!sonarDir.equals(includedDir)) {
          DirectoryEdge edge = packagesGraph.getEdge(sonarDir, includedDir);
          if (edge == null) {
            edge = new DirectoryEdge(sonarDir, includedDir);
            if (packagesGraph.hasEdge(sonarDir, includedDir)) {
              packagesGraph.addEdge(edge);
            }            
          }
          edge.addRootEdge(fileEdge);
        }
      } else {
        if (CxxUtils.LOG.isDebugEnabled()) {
          CxxUtils.LOG.debug("Skipping dependency to file '{}', because it is'nt part of this project", includedFile.path());
        }
      }
    }
  }

  /**
   * Perform the analysis and save the results.
   */
  public void save(SensorContext sensorContext) {
    final Collection<InputDir> packages = packagesGraph.getVertices();


    IncrementalCyclesAndFESSolver<InputDir> cycleDetector = new IncrementalCyclesAndFESSolver<>(packagesGraph, packages);
    Set<Cycle> cycles = cycleDetector.getCycles();
    Set<Edge> feedbackEdges = cycleDetector.getFeedbackEdgeSet();
    int tangles = cycleDetector.getWeightOfFeedbackEdgeSet();

    CxxUtils.LOG.info("Project '{}' Cycles:{} Feedback cycles:{} Tangles:{} Weight:{}",
      new Object[]{context.module().key(), cycles.size(), feedbackEdges.size(), tangles, getEdgesWeight(packagesGraph.getEdges(packages))});

    saveViolations(feedbackEdges, packagesGraph, sensorContext);
  }

  private void saveViolations(Set<Edge> feedbackEdges, DirectedGraph<InputDir, DirectoryEdge> packagesGraph, SensorContext sensorContext) {
    if (cycleBetweenPackagesRule != null) {
      for (Edge feedbackEdge : feedbackEdges) {
        InputDir fromPackage = (InputDir) feedbackEdge.getFrom();
        InputDir toPackage = (InputDir) feedbackEdge.getTo();
        DirectoryEdge edge = packagesGraph.getEdge(fromPackage, toPackage);
        for (FileEdge subEdge : edge.getRootEdges()) {
          InputFile fromFile = subEdge.getFrom();
          InputFile toFile = subEdge.getTo();
          
          NewIssue newIssue = sensorContext.newIssue().forRule(duplicateIncludeRule.ruleKey());
          NewIssueLocation location = newIssue.newLocation()
            .on(fromFile)
            .at(fromFile.selectLine(1))
            .message("Remove the dependency from file \"" + fromFile + "\" to file \"" + toFile + "\" to break a package cycle.");
                  
          newIssue.at(location);
          newIssue.save();

          violationsCount++;          
        }
      }
    }
  }

  private double getEdgesWeight(Collection<? extends Edge> edges) {
    double total = 0.0;
    for (Edge edge : edges) {
      total += edge.getWeight();
    }
    return total;
  }
}
